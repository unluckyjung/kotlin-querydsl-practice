package com.study.querydsl.member.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Repository

interface MemberRepository : JpaRepository<Member, Long>

interface MemberPredicateRepository : JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member>

@Repository
class MemberDao : QuerydslRepositorySupport(Member::class.java) {
//     private val queryFactory = JPAQueryFactory(entityManager) // 해당 queryFactory 는 정상적으로 작동안함.

    fun findMembersByName(name: String): List<Member> {
        return from(QMember.member)
            .select(QMember.member)
            .where(QMember.member.name.eq(name))
            .fetch().toList()
    }

    fun changeNameLtAge(toChangeName: String, age: Int): Long {
        // 함수에서 만들어줘야함. 영속성 컨텍스트별로 엔티티 매니저가 관리되기 때문에 이슈가 되는것으로 보임.
        // 왜냐하면 윗단에서 만드는 엔티티 매니저는 다른 영속성 컨텍스트에서 호출할때는, 해당 영속성 컨텍스트 범위 밖에서 만들어진것이라 문제인것으로 추측
        // TODO: 리서칭 필요
        val queryFactory = JPAQueryFactory(entityManager)

        val resultCount = queryFactory
            .update(QMember.member)
            .set(QMember.member.name, toChangeName)
            .where(QMember.member.age.lt(age))
            .execute()

        // entityManager 를 바로 다룰 수 있음.
        entityManager!!.flush()
        entityManager!!.clear()

        return resultCount
    }
}
