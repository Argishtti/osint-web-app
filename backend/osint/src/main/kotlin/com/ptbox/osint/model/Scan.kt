package com.ptbox.osint.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.OneToMany
import jakarta.persistence.CascadeType
import jakarta.persistence.FetchType
import java.util.UUID

@Entity
data class Scan(
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        val id: UUID? = null,

        @Column
        val domain: String,

        @OneToMany(mappedBy = "scan", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val scanResult: MutableList<ScanResult> = mutableListOf()
)
