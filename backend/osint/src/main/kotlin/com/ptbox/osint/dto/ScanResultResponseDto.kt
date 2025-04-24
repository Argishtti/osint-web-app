package com.ptbox.osint.dto

import java.time.LocalDateTime
import java.util.UUID

data class ScanResultResponseDto(
        val id: UUID,
        val value: String,
        val startedAt: LocalDateTime,
        var finishedAt: LocalDateTime
)
