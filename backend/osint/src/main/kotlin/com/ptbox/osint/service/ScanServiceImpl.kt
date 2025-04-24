package com.ptbox.osint.service

import com.ptbox.osint.dto.ScanResponseDto
import com.ptbox.osint.dto.ScanResultResponseDto
import com.ptbox.osint.dto.ScanStatus
import com.ptbox.osint.dto.ScanStatusResponseDto
import com.ptbox.osint.exception.ScanProcessingException
import com.ptbox.osint.model.Scan
import com.ptbox.osint.model.ScanResult
import com.ptbox.osint.repository.ScanRepository
import com.ptbox.osint.service.impl.ScanService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    }

    private val activeScans = ConcurrentHashMap<String, MutableSet<String>>() // sessionId -> set of domains

    override fun startScan(sessionId: String, domain: String): ScanStatusResponseDto {
        logger.info("Requesting to scan $domain domain with sessionId $sessionId")
        if (isAlreadyScanning(sessionId, domain)) {
            logger.info("Skipping scan for domain $domain (already in progress)")
            return ScanStatusResponseDto(ScanStatus.IN_PROGRESS, "Scan already in progress for $domain")
        }

        activeScans.computeIfAbsent(sessionId) { Collections.synchronizedSet(mutableSetOf()) }.add(domain)

        CoroutineScope(Dispatchers.IO).launch {
            logger.debug("Running scan for $domain...")
            val startedAt = LocalDateTime.now()
            val scanResult = runAmassScan(domain, sessionId)
            logger.debug("Scan result: $scanResult")
            if (scanResult.isNotBlank()) {

                val finishedAt = LocalDateTime.now()
                val scanOptional = scanRepository.findByDomainContainingIgnoreCase(domain)
                if (scanOptional.isPresent) {
                    addResultToDomain(scanOptional.get(), scanResult, startedAt, finishedAt)
                } else {
                    persistResult(scanResult, domain, startedAt, finishedAt)
                }
            }
            activeScans[sessionId]?.remove(domain)
        }

        return ScanStatusResponseDto(ScanStatus.ACCEPTED, "Scanning $domain")
    }

    override fun getScannedDomains(): List<ScanResponseDto> {
        return scanRepository.findAll()
                .map {
                    ScanResponseDto(
                            id = it.id!!,
                            domain = it.domain,
                            result = it.scanResult.map { scanResult ->
                                ScanResultResponseDto(
                                        scanResult.id!!, scanResult.value, scanResult.startedAt, scanResult.finishedAt!!
                                )
                            }
                    )
                }
    }

    fun runAmassScan(domain: String, sessionId: String): String {
        val filePath = "osint/amass/config/$domain.txt"
        val outputFile = File(filePath)

        try {
            val process = ProcessBuilder(
                    "docker", "exec", "amass", "amass", "enum",
                    "-config", "/config/amass/amass_config.yaml",
                    "-d", domain,
                    "-o", "/config/amass/$domain.txt"
            ).redirectErrorStream(true).start()

            val logs = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                activeScans[sessionId]?.remove(domain)
                logger.error("Scan failed (exit $exitCode)\nLogs:\n$logs")
                throw ScanProcessingException("Scan failed (exit $exitCode)\nLogs:\n$logs")
            }

            if (waitForFileStable(filePath)) {
                return outputFile.readText()
            } else {
                logger.error("Output file never became stable.\nLogs:\n$logs")
                activeScans[sessionId]?.remove(domain)
                logger.error("Output file never became stable.\nLogs:\n$logs")
                throw ScanProcessingException("Output file never became stable.\nLogs:\n$logs")
            }

        } catch (e: Exception) {
            logger.error("Error during scan: ${e.message}")
            activeScans[sessionId]?.remove(domain)
            throw ScanProcessingException("Error during scan: ${e.message}")
        }
    }


    private fun persistResult(result: String, domain: String, startedAt: LocalDateTime, finishedAt: LocalDateTime) {
        logger.debug("Requesting to save result of domain $domain")
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
        logger.debug("Requesting to add scan result to existing domain ${scan.domain}")
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

    private fun waitForFileStable(filePath: String, stableDurationMs: Long = 3000L, timeoutMs: Long = 60000L): Boolean {
        val file = File(filePath)
        val checkInterval = 1000L
        var waited = 0L
        var lastModified: Long? = null
        var stableTime = 0L

        while (waited < timeoutMs) {
            logger.debug("Waited: $waited")
            logger.debug("TimeOut: $timeoutMs")
            if (file.exists()) {
                val currentModified = file.lastModified()

                if (lastModified == null || currentModified != lastModified) {
                    lastModified = currentModified
                    stableTime = 0L
                } else {
                    stableTime += checkInterval
                    logger.debug("StableTime: $stableTime")
                    if (stableTime >= stableDurationMs) {
                        return true
                    }
                }
            } else {
                return false
            }

            Thread.sleep(checkInterval)
            waited += checkInterval
        }

        return false
    }


}
