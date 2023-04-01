package com.study.querydsl.member.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.querydsl.QSort
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

    @Test
    fun getMemberWithPagingTest() {
        memberRepository.save(Member(name = "goodall", age = 30))
        memberRepository.save(Member(name = "jys", age = 30))
        memberRepository.save(Member(name = "yooonsung", age = 30))
        memberRepository.save(Member(name = "unluckyjung", age = 30))
        memberRepository.save(Member(name = "fortune", age = 30))

        val result1 = memberDao.getMemberWithPaging(age = 30, PageRequest.of(0, 3))
        result1.size shouldBe 3

        val result2 = memberDao.getMemberWithPaging(age = 30, PageRequest.of(1, 3))
        result2.size shouldBe 2
    }

    @DisplayName("querydsl 에서 paging 에 sort 를 사용하는경우, QeuryDsl에서 지원하는 QSort 를 사용해주어야한다.")
    @Test
    fun getMemberWithPagingTest2() {
        memberRepository.save(Member(name = "unluckyjung", age = 40))
        memberRepository.save(Member(name = "goodall", age = 10))
        memberRepository.save(Member(name = "yooonsung", age = 30))
        memberRepository.save(Member(name = "jys", age = 20))
        memberRepository.save(Member(name = "fortune", age = 50))

        // Spring Data JPA Sort 는 정상 작동하지 않음.
//        val result1 = memberDao.getMemberWithPagingAndSort(PageRequest.of(0, 3, Sort.by("age").ascending()))
//        result1.size shouldBe 3

        val queryDslSort = QSort(QMember.member.age.asc())
        val result2 = memberDao.getMemberWithPagingAndSort(PageRequest.of(0, 3, queryDslSort))
        result2.size shouldBe 3

        result2[0].age shouldBe 10
        result2[1].age shouldBe 20
        result2[2].age shouldBe 30
    }
}
