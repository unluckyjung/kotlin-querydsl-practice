package com.study.querydsl.basic

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class CaseAndConstConcatTest(
    private val entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @Test
    fun caseBasic() {
        val oldMember = memberRepository.save(
            Member(age = 30),
        )

        val result = queryFactory.select(
            QMember.member.age
                .`when`(8).then("8살")
                .`when`(20).then("20살")
                .`when`(30).then("30살")
                .otherwise("그외")
        ).from(QMember.member).fetch()

        result.first() shouldBe "30살"
    }

    @Test
    fun caseBuilder() {
        val oldMember = memberRepository.save(
            Member(age = 30),
        )
        val babyMember = memberRepository.save(
            Member(age = 3),
        )
        val youngMember = memberRepository.save(
            Member(age = 15),
        )

        val result = queryFactory.select(
            CaseBuilder()
                .`when`(QMember.member.age.between(0, 7)).then("미취학")
                .`when`(QMember.member.age.between(8, 13)).then("초등학생")
                .`when`(QMember.member.age.between(14, 16)).then("중학생")
                .`when`(QMember.member.age.between(17, 19)).then("고등학생")
                .otherwise("성인")
        ).from(QMember.member).fetch()

        result[0] shouldBe "성인"
        result[1] shouldBe "미취학"
        result[2] shouldBe "중학생"
    }

    @DisplayName("Expressions.constant 통해 상수를 결과에 포함하면, jpql 에 포함되진 않고, 결과에만 포함된다.")
    @Test
    fun constPrefix() {
        val jys = memberRepository.save(
            Member(name = "goodall", age = 30),
        )

        val result = queryFactory
            .select(
                QMemberInfo(
                    Expressions.constant("DUNAMU"),
                    QMember.member.name,
                    QMember.member.age
                )
            )
            .from(QMember.member)
            .fetchOne()!!

        result.company shouldBe "DUNAMU"
        result.name shouldBe jys.name
        result.age shouldBe jys.age
    }

    @DisplayName("문자가 아닌 타입(Long, Enum 등등) 들은 stringValue 를 통해서 문자열 형태의 값을 얻을 수 있다.")
    @Test
    fun concatTest() {
        val jys = memberRepository.save(
            Member(name = "goodall", age = 30),
        )

        val result = queryFactory.select(
            QMember.member.name.concat("_")
                .concat(QMember.member.age.stringValue())
        ).from(QMember.member).fetchOne()!!

        result shouldBe "goodall_30"
    }
}
