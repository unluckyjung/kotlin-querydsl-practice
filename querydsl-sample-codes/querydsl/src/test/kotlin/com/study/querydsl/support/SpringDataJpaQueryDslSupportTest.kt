package com.study.querydsl.support

import com.querydsl.core.BooleanBuilder
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberPredicateRepository
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@IntegrationTest
class SpringDataJpaQueryDslSupportTest(
    private val memberRepository: MemberRepository,
    private val memberPredicateRepository: MemberPredicateRepository,
) {

    @DisplayName("QuerydslPredicateExecutor 를 레포지토리에 넣어주면, predicate 를 파라메터에 넣어서 findAll 이 가능하다. 다만, 쿼리성 로직이 서비스 로직으로 빠져나오는 구조가되어 좋지않다.")
    @Test
    fun predicateExecutorTest() {

        memberPredicateRepository.save(Member(name = "jys", age = 29))
        memberPredicateRepository.save(Member(name = "yoonsung", age = 30))
        memberPredicateRepository.save(Member(name = "goodall", age = 31))

        val result1 = memberPredicateRepository.findAll(
            QMember.member.age.goe(30)
        ).toList()

        result1.size shouldBe 2


        val predicate = BooleanBuilder().apply {
            this.and(QMember.member.age.goe(30)).and(QMember.member.name.eq("goodall"))
        }
        val result2 = memberPredicateRepository.findAll(predicate).toList()
        result2.size shouldBe 1
    }
}
