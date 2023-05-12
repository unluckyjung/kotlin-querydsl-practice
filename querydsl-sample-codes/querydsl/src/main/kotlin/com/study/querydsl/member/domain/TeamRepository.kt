package com.study.querydsl.member.domain

import com.study.querydsl.member.domain.QMember.member
import com.study.querydsl.member.domain.QTeam.team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface TeamRepository : JpaRepository<Team, Long>

@Repository
class TeamDao : QuerydslRepositorySupport(Team::class.java) {

    @Transactional(readOnly = true)
    fun findTeamById(teamId: Long): Team? {
        return from(team)
            .where(team.id.eq(teamId))
            .select(team)
            .fetchOne()
    }

    @Transactional(readOnly = true)
    fun findTeamAndMember(teamId: Long): List<MemberTeamNames> {
        return from(team)
            .join(member).on(member.team.eq(team))
            .where(team.id.eq(teamId))
            .select(
                QMemberTeamNames(
                    team.name,
                    member.name,
                )
            )
            .fetch()
    }
}
