package com.lemontree.domain.remittance.service.dto

data class RemittanceSaveRequest(
    val from: Long,
    val to: Long,
    val amount: Long
)