package com.study.querydsl.basic

import com.querydsl.core.types.dsl.Expressions
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
class SqlFunctionTest(
    entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @DisplayName("Expressions 를 통해서, 사용중인 DB 의 fun 을 사용할 수 있다. 다만 {DB이름Dialect} 에 등록된 것만 기본적으로 사용가능하다.")
    @Test
    fun replaceTest() {
        repeat(3) {
            memberRepository.save(
                Member(name = "yoonsung $it", age = 30)
            )
        }

        // replace 함수를 이용해서, yoonsung 을 goodall 로 전부 변경
        val functionTemplate = "function('replace', {0}, {1}, {2})"

        val result = queryFactory.select(
            Expressions.stringTemplate(
                functionTemplate,
                QMember.member.name, "yoonsung", "goodall"
            )
        ).from(QMember.member).fetch()!!

        result.forEachIndexed { index, name ->
            name shouldBe "goodall $index"
        }
    }

    @DisplayName("Expressions 대신 queryDSL 에서 제공하는 함수로, sql function 을 사용할 수 있다.")
    @Test
    fun replaceTest2() {
        memberRepository.save(
            Member(name = "yoonsung", age = 30)
        )

        memberRepository.save(
            Member(name = "YoonSung", age = 30)
        )

        val result = queryFactory.select(QMember.member.name)
            .from(QMember.member)
            .where(QMember.member.name.lower().eq("yoonsung"))   // lower 함수 사용
            .fetch()!!

        result.size shouldBe 2
    }
}
