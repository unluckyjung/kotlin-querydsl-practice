package com.study.querydsl.member.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class MemberDaoTest(
    entityManager: EntityManager,
    private val memberRepository: MemberRepository,
    private val memberDao: MemberDao,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @DisplayName("Dao 를 통해 name 에 해당되는 모든 멤버를 찾는다.")
    @Test
    fun findByNameTest() {
        val jysName = "jys"
        val goodallName = "goodall"

        memberRepository.save(Member(name = jysName, age = 10))
        memberRepository.save(Member(name = jysName, age = 20))
        memberRepository.save(Member(name = jysName, age = 30))
        memberRepository.save(Member(name = jysName, age = 40))

        memberDao.findMembersByName(jysName).size shouldBe 4
    }

    @DisplayName("bulk Update 작업 이후, 조회시 반영된 결과 값이 정상적으로 나온다.")
    @Test
    fun bulkUpdateTest() {
        val jysName = "jys"
        val goodallName = "goodall"

        memberRepository.save(Member(name = jysName, age = 10))
        memberRepository.save(Member(name = jysName, age = 20))
        memberRepository.save(Member(name = jysName, age = 30))
        memberRepository.save(Member(name = jysName, age = 40))

        val changedCount = memberDao.changeNameLtAge(goodallName, 25)

        changedCount shouldBe 2

        val result = queryFactory.selectFrom(QMember.member).fetch()
        result.map {
            it.name
        } shouldContainExactly (listOf(goodallName, goodallName, jysName, jysName))
    }
}
