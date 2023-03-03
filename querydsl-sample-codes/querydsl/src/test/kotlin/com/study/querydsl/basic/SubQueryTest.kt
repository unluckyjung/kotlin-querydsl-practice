package com.study.querydsl.basic

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.*
import com.study.querydsl.member.domain.QMember.member
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


@IntegrationTest
class SubQueryTest(
    private val entityManager: EntityManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    lateinit var teamA: Team
    lateinit var teamB: Team

    @BeforeEach
    internal fun setUp() {
        teamA = teamRepository.save(Team(name = "ATeam")).also { team ->
            memberRepository.save(Member(name = "a", age = 10)).run {
                this.changeTeam(team)
            }
            memberRepository.save(Member(name = "b", age = 20)).run {
                this.changeTeam(team)
            }
        }

        teamB = teamRepository.save(Team(name = "BTeam")).also { team ->
            memberRepository.save(Member(name = "c", age = 30)).run {
                this.changeTeam(team)
            }
        }
    }

    @DisplayName("서브쿼리 사용시에는 JPAExpressions(static import 가능) 를 이용하여 서브쿼리를 만들어줘야한다.")
    @Test
    fun subQueryTest1() {
        // sub query 에 사용되어야 해서 이름이 겹치면 안됌
        val subMember = QMember("subMember")

        val resultMember = queryFactory
            .selectFrom(member)
            .where(
                member.age.eq(
                    // subQuery 사용시 JPAExpressions 이용
                    JPAExpressions
                        .select(subMember.age.max())
                        .from(subMember)
                )
            ).fetchOne()!!

        resultMember.age shouldBe 30

        val member2 = queryFactory
            .selectFrom(member)
            .where(
                member.age.`in`(
                    JPAExpressions
                        .select(subMember.age)
                        .from(subMember)
                        .where(subMember.age.gt(10))
                )
            ).fetch()

        member2.size shouldBe 2
    }

    @DisplayName("select 에도 서브쿼리를 쓸수 있다. 하지만 From 절에는 사용할 수 없다.")
    @Test
    fun subQueryTest2() {
        val subMember = QMember("subMember")

        val maxAge = "maxAge"

        // QMember.member.age 같은 것들도 전부 stringPath 이다.
        val maxAgeStringPath = Expressions.stringPath("maxAge")

        val result = queryFactory
            .select(
                member.name,
                // Expressions.as 를 통해 alias 를 정해줄 수 있다.
                Expressions.`as`(
                    JPAExpressions
                        .select(subMember.age.max())
                        .from(subMember),
                    maxAge
                )
            ).from(member)
            .fetch()

        result.forEach {
            // stringPath 를 이용한 값 검증
            it[maxAgeStringPath] shouldBe 30

            // 두번째 인덱스에 maxAge 가 있는것을 아는 상태로 값 검증
            it.get(1, Int::class.java) shouldBe 30
        }
    }
}
