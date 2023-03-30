package com.study.querydsl.member.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor

interface MemberRepository : JpaRepository<Member, Long>

interface MemberPredicateRepository : JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member>
