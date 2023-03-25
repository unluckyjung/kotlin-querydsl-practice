package com.study.querydsl.basic

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager


@IntegrationTest
class BulkTest(
    private val entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @DisplayName("bulk 연산은 영속성 컨텍스트를 무시하고 데이터를 저장하기 때문에, DB 와 영속성 컨텍스트의 정합성이 깨진다.")
    @Test
    fun batchUpdate() {
        val jysName = "jys"
        val goodallName = "goodall"

        memberRepository.save(Member(name = jysName, age = 10))
        memberRepository.save(Member(name = jysName, age = 20))
        memberRepository.save(Member(name = jysName, age = 30))
        memberRepository.save(Member(name = jysName, age = 40))

        // 영향받은 개수
        val affectedCount = queryFactory
            .update(QMember.member)
            .set(QMember.member.name, goodallName)
            .where(QMember.member.age.lt(25))
            .execute()

        affectedCount shouldBe 2

        // 영속성 컨텍스트에 있는 값을 가져와서 전부 "jys"로 처리됌
        val result = queryFactory.selectFrom(QMember.member).fetch()
        result.map {
            it.name
        } shouldNotContain goodallName

        // 영속성 컨텍스트의 값을 비움
        entityManager.flush()
        entityManager.clear()

        val result2 = queryFactory.selectFrom(QMember.member).fetch()
        result2.map {
            it.name
        } shouldContainExactly (listOf(goodallName, goodallName, jysName, jysName))
    }

    @DisplayName("where 조건이 있으면 새롭게 쿼리가 발생하여, 영속성컨텍스트의 결과를 보지 않고 DB의 값을 본다.")
    @Test
    fun batchUpdate2() {
        val jysName = "jys"
        val goodallName = "goodall"

        memberRepository.save(Member(name = jysName, age = 10))
        memberRepository.save(Member(name = jysName, age = 20))
        memberRepository.save(Member(name = jysName, age = 30))
        memberRepository.save(Member(name = jysName, age = 40))

        val goodallCount1 = findByNameCount(goodallName)
        val jysCount1 = findByNameCount(jysName)

        goodallCount1 shouldBe 0
        jysCount1 shouldBe 4

        queryFactory
            .update(QMember.member)
            .set(QMember.member.name, goodallName)
            .where(QMember.member.age.lt(25))
            .execute()

        // 영속성 컨텍스트를 비우지 않고 where 조건넣어 조회
        val goodallCount2 = findByNameCount(goodallName)
        val jysCount2 = findByNameCount(jysName)

        goodallCount2 shouldBe 2
        jysCount2 shouldBe 2
    }

    @DisplayName("bulkDelete 를 진행하면 @SqlDelete 를 설정해주어도, hardDelete 가 진행된다.")
    @Test
    fun batchDelete() {
        val jysName = "jys"

        memberRepository.save(Member(name = jysName, age = 10))
        memberRepository.save(Member(name = jysName, age = 20))
        memberRepository.save(Member(name = jysName, age = 30))
        memberRepository.save(Member(name = jysName, age = 40))

        // hardDelete 쿼리발생
        queryFactory.delete(QMember.member)
            .where(QMember.member.age.lt(25))
            .execute()

        val softDeletedCount1 = queryFactory.selectFrom(QMember.member)
            .where(QMember.member.deletedAt.isNotNull)
            .fetch().count()

        // 2개가 추가되지 않음. 쿼리를 확인해보아도, 쌩 삭제쿼리 발생
        softDeletedCount1 shouldBe 0

        entityManager.flush()
        entityManager.clear()

        // softDelete 진행
        memberRepository.deleteAll()

        val softDeletedCount2 = queryFactory.selectFrom(QMember.member)
            .where(QMember.member.deletedAt.isNotNull)
            .fetch().count()

        softDeletedCount2 shouldBe 2
    }

    private fun findByNameCount(name: String): Int {
        return queryFactory.selectFrom(QMember.member)
            .where(QMember.member.name.eq(name))
            .fetch()
            .count()
    }
}
