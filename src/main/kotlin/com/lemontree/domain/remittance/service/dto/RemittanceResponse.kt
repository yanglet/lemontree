package com.lemontree.domain.remittance.service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.*

data class RemittanceReadResponse(
    val remittanceNo: Long,
    val to: Long,
    val from: Long,
    val toBalance: Long,
    val fromBalance: Long,
    val amount: Long,
    val status: String,
    val reason: String?,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val insertDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val updateDate : LocalDateTime
)