package com.lemontree.domain.remittance.controller

import com.lemontree.domain.remittance.service.*
import com.lemontree.domain.remittance.service.dto.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/remittances")
class RemittanceController(
    private val remittanceService: RemittanceService
) {
    @PostMapping("/save")
    fun remit(
        @RequestBody request: RemittanceSaveRequest
    ) = ResponseEntity.ok(
        remittanceService.remit(request)
    )
}