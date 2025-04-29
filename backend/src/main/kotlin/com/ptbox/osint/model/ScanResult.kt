package com.ptbox.osint.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class ScanResult(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID? = null,

        @Column(columnDefinition = "TEXT")
        val value: String,

        @Column
        val startedAt: LocalDateTime = LocalDateTime.now(),

        @Column
        var finishedAt: LocalDateTime? = null,

        @ManyToOne
        @JoinColumn(name = "scan_id")
        var scan: Scan? = null
)
