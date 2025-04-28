package com.ptbox.osint.service.impl

import com.ptbox.osint.dto.ScanResponseDto
import com.ptbox.osint.dto.ScanStatusResponseDto

interface ScanService {
    fun startScan(sessionId: String, domain: String) : ScanStatusResponseDto
    fun getScannedDomains(): List<ScanResponseDto>
}