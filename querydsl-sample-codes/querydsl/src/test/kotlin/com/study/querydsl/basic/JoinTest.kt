package com.study.querydsl.basic

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.member.domain.Member
import com.study.querydsl.member.domain.MemberRepository
import com.study.querydsl.member.domain.QMember.member
import com.study.querydsl.member.domain.QTeam.team
import com.study.querydsl.member.domain.Team
import com.study.querydsl.member.domain.TeamRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory

@IntegrationTest
class JoinTest(
    private val entityManager: EntityManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository,
) {
    private val queryFactory = JPAQueryFactory(entityManager)

    lateinit var teamA: Team
    lateinit var teamB: Team

    @BeforeEach
    internal fun setUp() {
        teamA = teamRepository.save(Team(name = "ATeam")).also { team ->
            memberRepository.save(Member(name = "a", age = 10)).run {
                this.changeTeam(team)
            }
            memberRepository.save(Member(name = "b", age = 10)).run {
                this.changeTeam(team)
            }
        }

        teamB = teamRepository.save(Team(name = "BTeam")).also { team ->
            memberRepository.save(Member(name = "c", age = 30)).run {
                this.changeTeam(team)
            }
        }
    }

    @Test
    fun joinTest1() {
        val members = queryFactory.selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq(teamA.name))
            .fetch()

        members.size shouldBe 2
    }

    @Test
    fun joinTest2() {
        queryFactory.select(member, team)
            .from(member)
            .leftJoin(member.team, team).on(team.name.eq(teamA.name))
            .fetch().size shouldBe 3

        queryFactory.select(member, team)
            .from(member)
            .join(member.team, team)
//            .on(team.name.eq(teamA.name))
            .where(team.name.eq(teamA.name))    // inner join 은 where 로 처리하는것이 가독성이 좋다.
            .fetch().size shouldBe 2
    }

    @Test
    fun crossJoinTest() {
        memberRepository.save(Member(name = "x", age = 30))
        memberRepository.save(Member(name = "y", age = 30))
        memberRepository.save(Member(name = "z", age = 30))

        val result = queryFactory.select(member, team)
            .from(member)
            // leftJoin(member.team, team) 처럼 연관관계를 안잡아 주어도 cross join 해서 다 가져온다.
            .leftJoin(team).on(team.name.eq(teamA.name))
            .fetch()

        result.size shouldBe 2 + 1 + 3
    }


    @Test
    fun fetchJoinTest() {
        entityManager.flush()
        entityManager.clear()

        val noFetchJoinMember = queryFactory.selectFrom(member)
            .fetchFirst()!!

        // 실제로 로드됐는지를 확인하는 fun (lazy loading 이라서 로드 되지 않음 = false)
        entityManagerFactory.persistenceUnitUtil.isLoaded(noFetchJoinMember.team) shouldBe false

        entityManager.flush()
        entityManager.clear()

        val joinMember = queryFactory.selectFrom(member)
            .join(member.team, team)
            .fetchFirst()!!
        entityManagerFactory.persistenceUnitUtil.isLoaded(joinMember.team) shouldBe false

        entityManager.flush()
        entityManager.clear()


        val fetchJoinMember = queryFactory.selectFrom(member)
            .join(member.team, team).fetchJoin()
            .fetchFirst()!!

        entityManagerFactory.persistenceUnitUtil.isLoaded(fetchJoinMember.team) shouldBe true
    }
}
