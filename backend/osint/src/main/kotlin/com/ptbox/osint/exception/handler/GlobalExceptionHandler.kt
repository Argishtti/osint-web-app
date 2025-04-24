package com.ptbox.osint.exception.handler

import com.ptbox.osint.dto.ErrorResponseDto
import com.ptbox.osint.exception.ScanProcessingException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
                timestamp = LocalDateTime.now(),
                message = ex.message ?: "Unexpected error occurred",
                details = request.getDescription(false)
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ScanProcessingException::class)
    fun handleResourceNotFound(ex: ScanProcessingException, request: WebRequest): ResponseEntity<ErrorResponseDto> {
        val errorResponse = ErrorResponseDto(
                timestamp = LocalDateTime.now(),
                message = ex.message ?: "Error occurred during scanning",
                details = request.getDescription(false)
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}