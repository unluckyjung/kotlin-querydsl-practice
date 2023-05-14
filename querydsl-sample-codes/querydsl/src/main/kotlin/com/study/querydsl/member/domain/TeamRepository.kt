package com.study.querydsl.member.domain

import com.querydsl.core.group.GroupBy.groupBy
import com.querydsl.core.group.GroupBy.list
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

    @Transactional(readOnly = true)
    fun findMembersByTeam(teamId: Long): List<Member> {
        return from(team)
            .join(member).on(member.team.eq(team))
            .where(team.id.eq(teamId))
            .select(member)
            .fetch()
    }

    @Transactional(readOnly = true)
    fun findMembersByTeam2(teamId: Long): List<Member> {
        return from(team)
            .join(member).on(member.team.eq(team), member.deletedAt.isNull)
            .where(team.id.eq(teamId))
            .select(member)
            .fetch()
    }

    @Transactional(readOnly = true)
    fun findMembersWithTeam(teamId: Long): Map<Team, List<Member>>? {
        return from(team)
            .join(member).on(member.team.eq(team))
            .where(team.id.eq(teamId))
            .transform(groupBy(team).`as`(list(member)))
    }
}
