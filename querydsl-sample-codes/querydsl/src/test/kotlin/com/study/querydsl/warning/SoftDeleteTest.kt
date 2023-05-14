package com.study.querydsl.warning

import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

@IntegrationTest
class SoftDeleteTest(
    private val teamRepository: TeamRepository,
    private val memberRepository: MemberRepository,
    private val memberDao: MemberDao,
    private val teamDao: TeamDao,
) {
    @DisplayName("softDelete 된 Team 이 from 으로 시작되면 조회되지 않는다.")
    @Test
    fun softDeleteTest1() {
        val team = teamRepository.save(
            Team(name = "team1", deletedAt = ZonedDateTime.now()),
        )

        teamDao.findTeamById(team.id) shouldBe null
    }

    @DisplayName("softDelete 된 member 가 from 으로 시작하면 조회되지 않는다.")
    @Test
    fun softDeleteTest2() {
        val member = memberRepository.save(
            Member(name = "member1", deletedAt = ZonedDateTime.now()),
        )

        memberDao.findMemberById(member.id) shouldBe null
    }

    @DisplayName("softDelete 된 member 가 join 조건절에 있으면 where 쿼리가 나가지 않아 무시하고 조회되어버린다.")
    @Test
    fun softDeleteTest3() {
        val team = teamRepository.save(
            Team(name = "team1"),
        )
        val softDeletedMember = memberRepository.save(
            Member(name = "member1", team = team, deletedAt = ZonedDateTime.now()),
        )

        val result = teamDao.findMembersByTeam(teamId = team.id)

        result.size shouldBe 1
        result[0] shouldBe softDeletedMember
        result[0].team shouldBe team
    }

    @DisplayName("softDelete 된 팀이 from 조건에 있으면, 드라이빙 테이블이 존재하지않아 member 도 조회되지 않는다.")
    @Test
    fun softDeleteTest4() {
        val softDeletedTeam = teamRepository.save(
            Team(name = "team1", deletedAt = ZonedDateTime.now()),
        )
        val member = memberRepository.save(
            Member(name = "member1", team = softDeletedTeam),
        )

        val result = teamDao.findMembersByTeam(teamId = softDeletedTeam.id)
        result.size shouldBe 0
    }

    @DisplayName("join 되는 member 에 isNull 조건을 붙여주면 조회되지 않는다.")
    @Test
    fun softDeleteTest5() {
        val softDeletedTeam = teamRepository.save(
            Team(name = "team1", deletedAt = ZonedDateTime.now()),
        )
        val member = memberRepository.save(
            Member(name = "member1", team = softDeletedTeam),
        )

        val result = teamDao.findMembersByTeam2(teamId = softDeletedTeam.id)
        result.size shouldBe 0
    }
}
