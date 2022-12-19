package com.study.querydsl.querydsl

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.QMember
import com.study.querydsl.member.domain.Team
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class QueryDSLBasicTest(
    private val em: EntityManager,
) {
    private val teamAName = "teamA"
    private val member1Name = "member1"

    // 스프링에서 동시성 처리에 대한 설계가 다 되어있다.
    // 주입받은 EntityManager 가 트랜잭션에 따라서 바인딩 처리가 다 따로된다.
    // 즉 한개의 팩토리를 공유해서 사용하는 형태여도 상관없다.
    private val queryFactory = JPAQueryFactory(em)

    @BeforeEach
    internal fun setUp() {
        val teamA = Team(teamAName).apply {
            em.persist(this)
        }

        val member1 = Member(member1Name, 10, teamA).apply {
            em.persist(this)
        }
    }

    @Test
    fun queryDSLFindTest() {
        val m = QMember(member1Name)

        val findMember = queryFactory
            .select(m)
            .from(m)
            .where(m.name.eq(member1Name))
            .fetchOne() ?: throw IllegalArgumentException("")

        findMember.name shouldBe member1Name
    }
}
