package com.ptbox.osint.dto

import java.time.LocalDateTime

data class ErrorResponseDto(
        val timestamp: LocalDateTime,
        val message: String,
        val details: String
)
