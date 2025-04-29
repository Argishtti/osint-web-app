package com.ptbox.osint.dto

import org.springframework.http.HttpStatus

enum class ScanStatus(val statusCode: HttpStatus) {
    ACCEPTED(HttpStatus.ACCEPTED),
    IN_PROGRESS(HttpStatus.OK);
}