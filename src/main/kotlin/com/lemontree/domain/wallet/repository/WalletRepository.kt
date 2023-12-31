package com.lemontree.domain.wallet.repository

import com.lemontree.domain.member.entity.*
import com.lemontree.domain.wallet.entity.*
import org.springframework.data.jpa.repository.JpaRepository

interface WalletRepository : JpaRepository<Wallet, Long> {
    fun findByMember(member: Member): Wallet?
}