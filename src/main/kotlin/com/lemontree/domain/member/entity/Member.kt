package com.lemontree.domain.member.entity

import com.lemontree.domain.common.entity.*
import com.lemontree.domain.wallet.entity.*
import jakarta.persistence.*

@Entity
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no", nullable = false)
    var memberNo: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MemberStatus = MemberStatus.ACTIVE

) : BaseEntity() {
}