package com.lemontree.domain.wallet.service

import com.lemontree.domain.common.service.*
import com.lemontree.domain.member.entity.MemberStatus.ACTIVE
import com.lemontree.domain.member.exception.*
import com.lemontree.domain.member.repository.*
import com.lemontree.domain.wallet.exception.*
import com.lemontree.domain.wallet.repository.*
import com.lemontree.domain.wallet.service.dto.*
import org.springframework.stereotype.*

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val memberRepository: MemberRepository,

    private val distributedLockService: DistributedLockService
) {
    fun deposit(memberNo: Long, request: WalletDepositRequest) {
        distributedLockService.doDistributedLock("deposit::$memberNo") {
            val member = memberRepository.findByMemberNoAndStatus(memberNo, ACTIVE) ?: throw MemberNotFoundException("찾을 수 없는 회원입니다.")
            val wallet = walletRepository.findByMember(member) ?: throw WalletNotFoundException("찾을 수 없는 지갑입니다.")
            wallet.deposit(request.amount)
        }
    }
}