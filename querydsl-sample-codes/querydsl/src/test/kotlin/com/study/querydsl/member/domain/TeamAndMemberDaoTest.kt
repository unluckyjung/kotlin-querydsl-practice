package com.study.querydsl.member.domain

import com.study.querydsl.common.IntegrationTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

@IntegrationTest
class TeamAndMemberDaoTest(
    private val teamRepository: TeamRepository,
    private val memberRepository: MemberRepository,
    private val memberDao: MemberDao,
    private val teamDao: TeamDao,
) {
    @DisplayName("softDelete 된 Team 은 조회되지 않는다.")
    @Test
    fun softDeleteTest1() {
        val team = teamRepository.save(
            Team(name = "team1", deletedAt = ZonedDateTime.now()),
        )

        teamDao.findTeamById(team.id) shouldBe null
    }

    @DisplayName("softDelete 된 member 는 조회되지 않는다.")
    @Test
    fun softDeleteTest2() {
        val member = memberRepository.save(
            Member(name = "member1", deletedAt = ZonedDateTime.now()),
        )

        memberDao.findMemberById(member.id) shouldBe null
    }

    @DisplayName("softDelete 된 member 는 join 절에 있으면 조회되어버린다.")
    @Test
    fun softDeleteTest3() {
        val team = teamRepository.save(
            Team(name = "team1"),
        )
        val member = memberRepository.save(
            Member(name = "member1", team = team, deletedAt = ZonedDateTime.now()),
        )

        val result = teamDao.findTeamAndMember(teamId = team.id)
        result.size shouldBe 1
        result[0].teamName shouldBe team.name
        result[0].memberName shouldBe member.name
    }

    @DisplayName("softDelete 된 team 는 from 절로부터 시작되면 조회되지 않는다.")
    @Test
    fun softDeleteTest4() {
        val team = teamRepository.save(
            Team(name = "team1", deletedAt = ZonedDateTime.now()),
        )
        val member = memberRepository.save(
            Member(name = "member1", team = team),
        )

        val result = teamDao.findTeamAndMember(teamId = team.id)
        result.size shouldBe 0
    }
}

