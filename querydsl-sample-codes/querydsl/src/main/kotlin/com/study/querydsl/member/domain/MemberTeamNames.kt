package com.study.querydsl.member.domain

import com.querydsl.core.annotations.QueryProjection

data class MemberTeamNames @QueryProjection constructor(
    val teamName: String,
    val memberName: String,
)
