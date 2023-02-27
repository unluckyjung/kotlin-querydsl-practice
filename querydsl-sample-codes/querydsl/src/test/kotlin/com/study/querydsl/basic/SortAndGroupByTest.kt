package com.study.querydsl.basic

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember.*
import com.study.querydsl.member.domain.QTeam.team
import com.study.querydsl.member.domain.Team
import com.study.querydsl.member.domain.TeamRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class SortAndGroupByTest(
    private val entityManager: EntityManager,
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)


    @DisplayName("null 인것들을 마지막으로 배치해서 정렬할 수 있다.")
    @Test
    fun sortTest() {
        memberRepository.save(Member(name = "cc", age = 20))
        memberRepository.save(Member(name = "aa", age = 10))
        memberRepository.save(Member(name = null, age = 15))    // bb 랑 동률이지만, 순서가 밀린다.
        memberRepository.save(Member(name = "bb", age = 15))

        val result = queryFactory.selectFrom(member)
            .orderBy(
                member.age.desc(),
                member.name.desc().nullsLast()
            ).fetch()

        result[0].name shouldBe "cc"
        result[1].name shouldBe "bb"
        result[2].name shouldBe null
        result[3].name shouldBe "aa"
    }

    @DisplayName("집계 연산이고, 데이터 타입이 여러개인경우 Tuple 로 반환된다. ")
    @Test
    fun aggregateOperationTest() {
        memberRepository.save(Member(name = "a", age = 10))
        memberRepository.save(Member(name = "b", age = 20))
        memberRepository.save(Member(name = "c", age = 30))

        // queryDSL Tuple 로 반환된다.
        val result = queryFactory.select(
            member.count(),
            member.age.sum(),
            member.age.avg(),
            member.age.max(),
            member.age.min(),
        ).from(member).fetchOne()

        result?.let {
            it[member.count()] shouldBe 3
            it[member.age.sum()] shouldBe 60
            it[member.age.avg()] shouldBe 60 / 3
            it[member.age.max()] shouldBe 30
            it[member.age.min()] shouldBe 10
        }
    }

    @Test
    fun groupByTest() {
        val team2 = teamRepository.save(Team(name = "BTeam")).also { team ->
            memberRepository.save(Member(name = "c", age = 30)).run {
                this.changeTeam(team)
            }
        }

        val team1 = teamRepository.save(Team(name = "ATeam")).also { team ->
            memberRepository.save(Member(name = "a", age = 10)).run {
                this.changeTeam(team)
            }
            memberRepository.save(Member(name = "b", age = 10)).run {
                this.changeTeam(team)
            }
        }

        val teams = queryFactory.select(team.name, member.age.avg())
            .from(member)
            .join(member.team, team)
            .groupBy(team.name)
            .fetch()

        teams[0]?.let {
            it[team.name] shouldBe "ATeam"  // 정렬되어 먼저 집계
            it[member.age.avg()] shouldBe 10
        }

        teams[1]?.let {
            it[team.name] shouldBe "BTeam"
            it[member.age.avg()] shouldBe 30
        }
    }
}
