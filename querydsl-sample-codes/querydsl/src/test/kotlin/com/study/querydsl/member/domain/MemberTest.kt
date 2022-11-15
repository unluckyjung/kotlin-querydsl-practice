package com.study.querydsl.member.domain

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import javax.persistence.EntityManager
import javax.transaction.Transactional

// @Commit // 롤백 비활성화
@Transactional
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MemberTest(
    private val em: EntityManager,
) {

    @Test
    fun memberBasicTest() {
        val teamA = Team("teamA").apply {
            em.persist(this)
        }
        val teamB = Team("teamB").apply {
            em.persist(this)
        }

        val member1 = Member("member1", 10, teamA).apply {
            em.persist(this)
        }
        val member2 = Member("member2", 20, teamA).apply {
            em.persist(this)
        }
        val member3 = Member("member3", 30, teamB).apply {
            em.persist(this)
        }
        val member4 = Member("member4", 40, teamB).apply {
            em.persist(this)
        }

        em.flush()
        em.clear()

        val members = em.createQuery("select m from Member m", Member::class.java).resultList
        members.forEach {
            println("member= $it")
            println("-> member.team ${it.team}")
        }
    }
}
