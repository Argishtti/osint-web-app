package com.ptbox.osint.dto

import java.util.UUID

data class ScanResponseDto(
        val id: UUID,
        val domain: String,
        val result: List<ScanResultResponseDto>
)
