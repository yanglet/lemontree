package com.lemontree.domain.wallet.entity

import com.lemontree.domain.common.entity.*
import com.lemontree.domain.member.entity.*
import jakarta.persistence.*

@Entity
@Table(name = "WALLET")
class Wallet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_no", nullable = false)
    var walletNo: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    var member: Member,

    @Column(name = "balance", nullable = false)
    var balance: Long,

    @Column(name = "maximum_balance", nullable = false)
    var maximumBalance: Long

) : BaseEntity() {
}