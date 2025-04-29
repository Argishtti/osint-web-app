package com.ptbox.osint.service.impl

import com.ptbox.osint.dto.ScanResponseDto
import com.ptbox.osint.dto.ScanResultResponseDto
import com.ptbox.osint.dto.ScanStatus
import com.ptbox.osint.dto.ScanStatusResponseDto
import com.ptbox.osint.exception.ScanProcessingException
import com.ptbox.osint.model.Scan
import com.ptbox.osint.model.ScanResult
import com.ptbox.osint.repository.ScanRepository
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ScanServiceImpl(
        private val scanRepository: ScanRepository
) : ScanService {

    companion object {
        private val logger = LoggerFactory.getLogger(ScanServiceImpl::class.java)
        private const val WORKER_COUNT = 5
    }

    private val activeScans = ConcurrentHashMap<String, MutableSet<String>>()
    private val scanChannel = Channel<ScanRequest>(Channel.UNLIMITED)
    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Value("\${project.root.path}")
    private lateinit var projectRootPath: String


    init {
        repeat(WORKER_COUNT) { workerId ->
            scanScope.launch {
                logger.info("Starting scan worker #$workerId")
                for (request in scanChannel) {
                    processScanRequest(request)
                }
            }
        }
    }

    override fun startScan(sessionId: String, domain: String): ScanStatusResponseDto {
        logger.info("Requesting to scan domain: $domain (sessionId: $sessionId)")

        if (isAlreadyScanning(sessionId, domain)) {
            logger.info("Skipping scan for $domain (already in progress)")
            return ScanStatusResponseDto(ScanStatus.IN_PROGRESS, "Scan already in progress for $domain")
        }

        activeScans.computeIfAbsent(sessionId) { Collections.synchronizedSet(mutableSetOf()) }.add(domain)

        scanScope.launch {
            scanChannel.send(ScanRequest(sessionId, domain))
        }

        return ScanStatusResponseDto(ScanStatus.ACCEPTED, "Scanning $domain")
    }

    override fun getScannedDomains(): List<ScanResponseDto> {
        return scanRepository.findAll().map { scan ->
            ScanResponseDto(
                    id = scan.id!!,
                    domain = scan.domain,
                    result = scan.scanResult.map { scanResult ->
                        ScanResultResponseDto(
                                id = scanResult.id!!,
                                value = scanResult.value,
                                startedAt = scanResult.startedAt,
                                finishedAt = scanResult.finishedAt!!
                        )
                    }
            )
        }
    }

    private suspend fun processScanRequest(request: ScanRequest) {
        val (sessionId, domain) = request
        try {
            logger.debug("Starting scan for domain: $domain")

            val startedAt = LocalDateTime.now()
            val scanResult = runAmassScan(domain, sessionId)
            logger.debug("Scan result for $domain: ${scanResult.take(200)}...") // limit log size

            if (scanResult.isNotBlank()) {
                val finishedAt = LocalDateTime.now()
                val existingScan = withContext(Dispatchers.IO) {
                    scanRepository.findByDomainContainingIgnoreCase(domain)
                }

                if (existingScan.isPresent) {
                    addResultToDomain(existingScan.get(), scanResult, startedAt, finishedAt)
                } else {
                    persistNewScan(domain, scanResult, startedAt, finishedAt)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to scan domain: $domain", e)
        } finally {
            activeScans[sessionId]?.remove(domain)
        }
    }

    private suspend fun runAmassScan(domain: String, sessionId: String): String {
        val filePath = "osint/amass/config/$domain.txt"
        val outputFile = File(filePath)

        try {
            val process = withContext(Dispatchers.IO) {
                ProcessBuilder(
                        "docker", "run", "--rm",
                        "-v", "/config/amass:/config/amass",
                        "caffix/amass",
                        "enum",
                        "-config", "/config/amass/amass_config.yaml",
                        "-d", domain,
                        "-o", "/config/amass/$domain.txt"
                ).redirectErrorStream(true).start()
            }

            val logs = process.inputStream.bufferedReader().readText()
            val exitCode = withContext(Dispatchers.IO) {
                process.waitFor()
            }

            if (exitCode != 0) {
                throw ScanProcessingException("Amass scan failed (exit $exitCode)\nLogs:\n$logs")
            }

            if (waitForFileStable(filePath)) {
                return outputFile.readText()
            } else {
                throw ScanProcessingException("Amass output file never stabilized.\nLogs:\n$logs")
            }

        } catch (e: Exception) {
            logger.error("Error running amass scan for domain $domain", e)
            throw ScanProcessingException("Error running amass scan: ${e.message}")
        } finally {
            activeScans[sessionId]?.remove(domain)
        }
    }

    private fun waitForFileStable(filePath: String, stableDurationMs: Long = 3000L, timeoutMs: Long = 60000L): Boolean {
        val file = File(filePath)
        val checkIntervalMs = 1000L
        var waited = 0L
        var lastModifiedTime: Long? = null
        var stableTime = 0L

        while (waited < timeoutMs) {
            if (!file.exists()) return false

            val currentModified = file.lastModified()

            if (lastModifiedTime == null || currentModified != lastModifiedTime) {
                lastModifiedTime = currentModified
                stableTime = 0L
            } else {
                stableTime += checkIntervalMs
                if (stableTime >= stableDurationMs) {
                    return true
                }
            }

            Thread.sleep(checkIntervalMs)
            waited += checkIntervalMs
        }

        return false
    }

    private fun persistNewScan(domain: String, result: String, startedAt: LocalDateTime, finishedAt: LocalDateTime) {
        logger.debug("Persisting new scan result for $domain")
        val scanResult = ScanResult(
                value = result,
                startedAt = startedAt,
                finishedAt = finishedAt
        )
        val scan = Scan(
                domain = domain,
                scanResult = mutableListOf(scanResult)
        )
        scanResult.scan = scan
        scanRepository.save(scan)
    }

    private fun addResultToDomain(scan: Scan, result: String, startedAt: LocalDateTime, finishedAt: LocalDateTime) {
        logger.debug("Adding scan result to existing domain: ${scan.domain}")
        val scanResult = ScanResult(
                value = result,
                startedAt = startedAt,
                finishedAt = finishedAt,
                scan = scan
        )
        scan.scanResult.add(scanResult)
        scanRepository.save(scan)
    }

    private fun isAlreadyScanning(sessionId: String, domain: String): Boolean {
        return activeScans[sessionId]?.contains(domain) == true
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down ScanServiceImpl...")
        scanScope.cancel()
        scanChannel.close()
    }
}

private data class ScanRequest(val sessionId: String, val domain: String)
