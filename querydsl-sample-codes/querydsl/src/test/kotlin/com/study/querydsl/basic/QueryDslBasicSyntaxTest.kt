package com.study.querydsl.basic

import com.querydsl.core.NonUniqueResultException
import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember
import com.study.querydsl.member.domain.QMember.member
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager

@IntegrationTest
class QueryDslBasicSyntaxTest(
    private val entityManager: EntityManager,
    private val memberRepository: MemberRepository,
) {
    lateinit var firstMember: Member
    private val baseName = "jys"
    private val queryFactory = JPAQueryFactory(entityManager)

    @BeforeEach
    internal fun setUp() {
        firstMember = memberRepository.save(Member(name = baseName))
    }

    @Test
    fun QTypeTest1() {

        val result = queryFactory.select(member)
            .from(member)
            .where(member.name.eq(baseName))
            .fetchOne()

        result!!.name shouldBe baseName
    }

    @DisplayName("같은 테이블을 Join 해야하는 경우, jpql 에서 사용되는 테이블 이름을 지정해줄 수 있다.")
    @Test
    fun QTypeTest2() {
        val m1 = QMember("m1")

        val queryFactory = JPAQueryFactory(entityManager)

        val result = queryFactory.select(m1)
            .from(m1)
            .where(m1.name.eq(baseName))
            .fetchOne()

        result!!.name shouldBe baseName
    }

    @Test
    fun conditionTest0() {
        val member1 = memberRepository.save(Member(name = "goodall", age = 31))

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.contains("ood") // like %ood%
            )
            .fetchOne()!!.name shouldBe member1.name

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.startsWith("good")  // like good%
            )
            .fetchOne()!!.name shouldBe member1.name
    }

    @DisplayName("여러 조건 검색시, and 혹은 , 로 구분할 수 있다.")
    @Test
    fun conditionTest1() {
        val goodallName = "goodall"
        val member1 = memberRepository.save(Member(name = goodallName, age = 31))

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.eq(goodallName),
                member.age.eq(31)
            )
            .fetchOne()!!.name shouldBe member1.name

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.eq(goodallName).and(member.age.eq(31))
            )
            .fetchOne()!!.name shouldBe member1.name
    }

    @DisplayName("조건에 null 을 넣으면 해당 조건은 무시된다..")
    @Test
    fun conditionTest2() {
        val member1 = memberRepository.save(Member(name = "goodall", age = 31))
        val goodallName = "goodall"

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.eq(goodallName),
                member.age.eq(31),
                null,
            )
            .fetchOne()!!.name shouldBe member1.name

        queryFactory.select(member)
            .from(member)
            .where(
                member.name.eq(goodallName).and(member.age.eq(31))
            )
            .fetchOne()!!.name shouldBe member1.name
    }


    @Nested
    inner class FetchTest {
        @Test
        fun fetchBasicTest() {
            memberRepository.save(Member(name = "goodall", age = 31))
            memberRepository.save(Member(name = "goodall", age = 31))

            queryFactory.selectFrom(member).fetch().size shouldBe 1 + 2
            queryFactory.selectFrom(member).where(
                member.name.eq("none")
            ).fetch().size shouldBe 0
        }

        @DisplayName("fetchOne 의 경우, 조회했을때 없으면 null 을 반환하고, 2개 이상인 경우 예외가 발생한다.")
        @Test
        fun fetchOneTest() {
            val goodallName = "goodall"
            memberRepository.save(Member(name = goodallName, age = 31))

            queryFactory.selectFrom(member).where(
                member.name.eq(goodallName)
            ).fetchOne()!!.name shouldBe goodallName

            queryFactory.selectFrom(member).where(
                member.name.eq("none")
            ).fetchOne() shouldBe null


            // name = goodall 인 경우가 2개
            memberRepository.save(Member(name = goodallName, age = 31))

            shouldThrowExactly<NonUniqueResultException> {
                queryFactory.selectFrom(member).where(
                    member.name.eq(goodallName)
                ).fetchOne()
            }
        }

        @Test
        fun fetchFirstAndCountTest() {
            memberRepository.save(Member(name = "goodall", age = 31))
            memberRepository.save(Member(name = "unluckyjung", age = 31))

            // limit(1) + fetchOne
            queryFactory.selectFrom(member).fetchFirst().name shouldBe baseName

            queryFactory.selectFrom(member).fetchCount() shouldBe 1 + 2
        }

        @DisplayName("fetchResults 를 사용하면, 페이징 정보도 같이 가져오나, 같이 동작하는 countQuery 이슈 때문에 (ex group by 등등..) 5.0 에서 deprecated 되었다.")
        @Test
        fun fetchResultsTest() {
            val baseSize = 2L
            memberRepository.save(Member(name = "goodall", age = 31))
            memberRepository.save(Member(name = "unluckyjung", age = 31))


            val result = queryFactory.selectFrom(member)
                .limit(baseSize)
                .orderBy(member.id.desc())
                .fetchResults()

            result.total shouldBe 1 + 2
            result.limit shouldBe baseSize
            result.offset shouldBe 0
            result.results.size shouldBe 2

            memberRepository.save(Member(name = "unluckyjung", age = 31))
            memberRepository.save(Member(name = "unluckyjung", age = 31))
            memberRepository.save(Member(name = "unluckyjung", age = 31))

            val afterResult = queryFactory.selectFrom(member)
                .offset(baseSize + result.offset)
                .limit(baseSize)
                .orderBy(member.id.desc())
                .fetchResults()

            afterResult.total shouldBe 1 + 2 + 3
            afterResult.limit shouldBe baseSize
            afterResult.offset shouldBe 2
            afterResult.results.size shouldBe 2
        }
    }
}
