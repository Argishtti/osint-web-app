package com.ptbox.osint.repository

import com.ptbox.osint.model.Scan
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ScanRepository : JpaRepository<Scan, Long> {
    fun findByDomainContainingIgnoreCase(domain: String) : Optional<Scan>
}
