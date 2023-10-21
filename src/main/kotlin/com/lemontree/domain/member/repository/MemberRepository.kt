package com.lemontree.domain.member.repository

import com.lemontree.domain.member.entity.*
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByMemberNoAndStatus(memberNo: Long, status: MemberStatus): Member?
}