package com.study.querydsl.member.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface MemberRepository : JpaRepository<Member, Long> {
    override fun findAll(pageable: Pageable): Page<Member>
}

interface MemberPredicateRepository : JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member>

@Repository
class MemberDao : QuerydslRepositorySupport(Member::class.java) {
//    private val queryFactory = JPAQueryFactory(entityManager) // 해당 queryFactory 는 정상적으로 작동안함.

    @Transactional(readOnly = true)
    fun findMembersByName(name: String): List<Member> {
        return from(QMember.member)
            .select(QMember.member)
            .where(QMember.member.name.eq(name))
            .fetch().toList()
    }

    @Transactional
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

    @Transactional(readOnly = true)
    fun getMemberWithPaging(age: Int, pageable: Pageable): List<Member> {
        val query = from(QMember.member)
            .where(
                QMember.member.age.eq(age),
            ).select(QMember.member)
        return querydsl!!.applyPagination(pageable, query).fetch()
    }

    @Transactional(readOnly = true)
    fun getMembersByPagingDataBasic(pageable: Pageable): Page<Member> {
        // pageable 에 있는 sort 를 사용하기가 매우 애매히진다.
        // https://uchupura.tistory.com/7 와 같은 방법을 통해서 직접 만들어줘야 함.

        return from(QMember.member)
            .select(QMember.member)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            // 여기서 쿼리가 한번 또 나간다. (total 을 얻기위해)
            /*
            select count(member0_.member_id) as col_0_0_
                    from
                    member member0_
             */
            .fetchResults().let {
                PageImpl(
                    it.results,
                    pageable,
                    it.total
                )
            }
    }

    @Transactional(readOnly = true)
    fun getMembersByPagingDataBasicWithOutCountQuery(pageable: Pageable): Page<Member> {
        val query = from(QMember.member)
            .select(QMember.member)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())

        val content = query.fetch()

        // PageableExecutionUtils 를 이용하면, 굳이 totalCount 를 조회하지 않아도 되는 경우에는 추가 쿼리가 나가지 않는다.
        // 데이터의 끝지점에 도달하면, count 쿼리를 보내지 않고 내부적인 로직으로 totalElement 를 보고 totalCount 를 리턴한다.
        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount)
    }

    @Transactional(readOnly = true)
    fun getMemberWithPagingAndSort(pageable: Pageable): List<Member> {
        val query = from(QMember.member).select(QMember.member)
            .orderBy(QMember.member.age.asc())

        return querydsl!!.applyPagination(pageable, query).fetch()
    }

    @Transactional(readOnly = true)
    fun getMemberWithPagingAndOrderByIdASC(pageable: Pageable): List<Member> {
        val query = from(QMember.member).select(QMember.member)
            .orderBy(QMember.member.age.asc())

        return querydsl!!.applyPagination(pageable, query).fetch()
    }
}
