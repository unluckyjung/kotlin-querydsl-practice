package com.study.querydsl.Intermediate

import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Repository

@IntegrationTest
class GroupFunTest(
    private val teamRepository: TeamRepository,
    private val memberRepository: MemberRepository,
    private val teamDao: TeamDao,
) {
    @DisplayName("transform 을 사용하면, map 형태로 반환받을 수 있다.")
    @Test
    fun groupTest() {
        val team = teamRepository.save(
            Team(name = "team1"),
        )

        repeat(3) {
            memberRepository.save(
                Member(name = "member $it", team = team),
            )
        }

        val result = teamDao.findMembersWithTeam(team.id)
        result!!.size shouldBe 1
        result[team]!!.size shouldBe 3
    }
}
