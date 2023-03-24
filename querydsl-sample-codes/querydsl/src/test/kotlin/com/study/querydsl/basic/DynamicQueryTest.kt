package com.study.querydsl.basic

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class DynamicQueryTest(
    entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @DisplayName("BooleanBuilder 를 사용해 완성된 predicate 를 만든뒤, where 조건에 사용해서 동적쿼리를 생성할 수 있다.")
    @Test
    fun booleanBuilderTest() {
        memberRepository.save(
            Member(name = "yoonsung", age = 30)
        )

        memberRepository.save(
            Member(name = "goodall", age = 31)
        )

        memberRepository.save(
            Member(name = "plzMatchName", age = 31)
        )

        findMembers(name = "findAll").size shouldBe 3
        findMembers(name = "goodall").size shouldBe 1
    }

    @DisplayName("where 조건에 들어가는 predicate 가 null 이면 해당 조건은 무시된다.")
    @Test
    fun predicateTest() {
        val goodallCount = 3
        val jysCount = 2

        repeat(goodallCount) {
            memberRepository.save(
                Member(name = "goodall", age = 31)
            )
        }

        repeat(jysCount) {
            memberRepository.save(
                Member(name = "jys", age = 31)
            )
        }

        val result = queryFactory.selectFrom(QMember.member)
            .where(
                nameCheck("default")
            ).fetch()!!

        result.size shouldBe goodallCount + jysCount

        val result2 = queryFactory.selectFrom(QMember.member)
            .where(
                nameCheck("goodall")
            ).fetch()!!

        result2.size shouldBe goodallCount
    }

    @DisplayName("and 같은것을 이용해 predicate 끼리 조합하려면, BooleanExpression 을 사용해야한다.")
    @Test
    fun predicateCombineTest() {

        memberRepository.save(
            Member(name = "jys", age = 30)
        )

        memberRepository.save(
            Member(name = "jys", age = 30)
        )

        memberRepository.save(
            Member(name = "goodall", age = 31)
        )

        val result1 = queryFactory.selectFrom(QMember.member)
            .where(
                getPredicateByName("jys")?.and(getPredicateByAge(30))
            ).fetch()!!

        result1.size shouldBe 2

        val result2 = queryFactory.selectFrom(QMember.member)
            .where(
                getPredicateByName("findAll")?.and(getPredicateByAge(-1))
            ).fetch()!!

        result2.size shouldBe 3
    }

    private fun findMembers(name: String): MutableList<Member> {
        val booleanBuilder = BooleanBuilder().apply {
            if (name != "findAll") {
                this.and(QMember.member.name.eq(name))
            }
        }
        return queryFactory.selectFrom(QMember.member)
            .where(booleanBuilder).fetch()!!
    }

    private fun nameCheck(inputName: String): Predicate? {
        return if (inputName == "default") {
            null // 전부 조회해옴.
        } else {
            QMember.member.name.eq(inputName)
        }
    }

    private fun getPredicateByName(inputName: String): BooleanExpression? {
        return if (inputName != "findAll") {
            // 이처럼 식에 대한 Predicate 는 BooleanBuilder 구현체가 아닌 BooleanExpression 으로 나온다.
            QMember.member.name.eq(inputName)
        } else {
            null
        }
    }

    private fun getPredicateByAge(inputAge: Int): BooleanExpression? {
        return if (inputAge != -1) {
            QMember.member.age.eq(inputAge)
        } else {
            null
        }
    }
}
