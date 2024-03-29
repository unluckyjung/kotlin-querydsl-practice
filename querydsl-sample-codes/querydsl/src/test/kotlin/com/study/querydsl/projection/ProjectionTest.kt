package com.study.querydsl.projection

import com.querydsl.core.types.ExpressionException
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.*
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class ProjectionTest(
    entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    @DisplayName("[비추천] Projections.bean 을 이용하면, setter 를 이용해서 값을 채운다.")
    @Test
    fun bySetter() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        val result = queryFactory.select(
            Projections.bean(
                MemberDtoWithNoArgusAndSetter::class.java,
                QMember.member.name,
                QMember.member.age
            )
        ).from(QMember.member).fetchOne()!!

        result.name shouldBe "yoonsung"
        result.age shouldBe 31
    }

    @DisplayName("[비추천] Projections.fields 을 이용하면, 필드를 이용해서 값을 채운다.")
    @Test
    fun byFieldsReflection() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        val result = queryFactory.select(
            Projections.fields(
                MemberDtoWithNoArgus::class.java,
                QMember.member.name,
                QMember.member.age
            )
        ).from(QMember.member).fetchOne()!!

        result.name shouldBe "yoonsung"
        result.age shouldBe 31
    }

    @DisplayName("[비추천] Projections.fields 을 사용시, 필드 이름이 다르면 as 로 이름을 정해주어야한다.")
    @Test
    fun byFieldsReflection2() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        val result = queryFactory.select(
            Projections.fields(
                MemberDtoOtherFieldName::class.java,
                QMember.member.name,
                QMember.member.age
            )
        ).from(QMember.member).fetchOne()!!

        result.userName shouldBe ""   // 리플랙션 단계에서 userName 을 못찾아서 "" 로 채워버림.

        val result2 = queryFactory.select(
            Projections.fields(
                MemberDtoOtherFieldName::class.java,
                QMember.member.name.`as`("userName"),
                QMember.member.age
            )
        ).from(QMember.member).fetchOne()!!

        result2.userName shouldBe "yoonsung"   // 리플랙션 단계에서 userName 을 못찾아서 "" 로 채워버림.
    }

    @DisplayName("[보통] Projections.constructor 을 이용하면, 필드를 이용해서 값을 채운다. 다만 필드 위치가 정확히 맞아야한다.")
    @Test
    fun byConstructor() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        val result = queryFactory.select(
            Projections.constructor(
                MemberDtoByConstructor::class.java,
                QMember.member.name,
                QMember.member.age
            )
        ).from(QMember.member).fetchOne()!!

        result.name shouldBe "yoonsung"
        result.age shouldBe 31

        shouldThrowExactly<ExpressionException> {
            queryFactory.select(
                Projections.constructor(
                    MemberDtoByConstructor::class.java,
                    QMember.member.age, // 잘못된 위치
                    QMember.member.name,
                )
            ).from(QMember.member).fetchOne()!!
        }

        // fields 는 예외가 발생하지 않음.
        shouldNotThrowAny {
            val result = queryFactory.select(
                Projections.fields(
                    MemberDtoWithNoArgus::class.java,
                    QMember.member.age,  // 잘못된 위치
                    QMember.member.name,
                )
            ).from(QMember.member).fetchOne()!!

            result.name shouldBe "yoonsung"
            result.age shouldBe 31
        }

        // setter 는 예외가 발생하지 않음.
        shouldNotThrowAny {
            val result = queryFactory.select(
                Projections.bean(
                    MemberDtoWithNoArgusAndSetter::class.java,
                    QMember.member.age, // 잘못된 위치
                    QMember.member.name,
                )
            ).from(QMember.member).fetchOne()!!

            result.name shouldBe "yoonsung"
            result.age shouldBe 31
        }
    }

    @DisplayName("[추천] QueryPojection 어노테이션을 이용하면, 잘못된 위치에 다른 타입의 값을 넣는경우 컴파일 타임에 예외가 잡힌다.")
    @Test
    fun byAnnotation() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        val result = queryFactory.select(
            QMemberDtoByAnnotation(
                QMember.member.name,
                QMember.member.age,
            )
        ).from(QMember.member).fetchOne()!!

        result.name shouldBe "yoonsung"
        result.age shouldBe 31
    }

    @DisplayName("group by 결과를 count 집계연산으로 처리하는경우, 프로젝션 필드 타입은 Long 으로 해주어야한다.")
    @Test
    fun projectionCountQueryTest() {
        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 31,
            )
        )

        memberRepository.save(
            Member(
                name = "yoonsung",
                age = 30,
            )
        )

        memberRepository.save(
            Member(
                name = "unluckyjung",
                age = 30,
            )
        )

        val result = queryFactory.select(
            QNameCountDto(
                QMember.member.name,
                QMember.member.count(),
            )
        ).from(QMember.member).groupBy(
            QMember.member.name
        ).fetch()

        result.size shouldBe 2

        result[0].name shouldBe "unluckyjung"
        result[0].count shouldBe 1

        result[1].name shouldBe "yoonsung"
        result[1].count shouldBe 2
    }
}
