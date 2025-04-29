package com.ptbox.osint.contoller

import com.ptbox.osint.dto.ScanResponseDto
import com.ptbox.osint.dto.ScanStatusResponseDto
import com.ptbox.osint.service.impl.ScanService
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ScanController(private val scanService: ScanService) {

    @PostMapping("/scan")
    fun startScan(
            @RequestBody request: ScanRequest,
            @CookieValue(name = "JSESSIONID", required = false) sessionId: String?
    ): ResponseEntity<ScanStatusResponseDto> {
        if (sessionId == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build()
        }

        val response = scanService.startScan(sessionId, request.domain)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/scans")
    fun getScans(session: HttpSession): ResponseEntity<List<ScanResponseDto>> {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(scanService.getScannedDomains())
    }
}