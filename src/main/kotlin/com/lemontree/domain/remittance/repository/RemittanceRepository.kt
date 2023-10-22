package com.lemontree.domain.remittance.repository

import com.lemontree.domain.remittance.entity.*
import org.springframework.data.jpa.repository.JpaRepository

interface RemittanceRepository : JpaRepository<Remittance, Long> {
    fun findByFrom(from: Long): List<Remittance>
}