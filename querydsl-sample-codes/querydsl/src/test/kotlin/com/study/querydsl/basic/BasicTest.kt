package com.study.querydsl.basic

import com.querydsl.jpa.impl.JPAQueryFactory
import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.dummy.DummyEntity
import com.study.querydsl.dummy.QDummyEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.persistence.EntityManager


@IntegrationTest
class BasicTest(
    private val entityManager: EntityManager
) {

    @Test
    fun queryDslSettingTest() {
        val dummy = DummyEntity("goodall")
        entityManager.persist(dummy)

        // TODO: JPAQueryFactory 역할 파악 08.06
        val query = JPAQueryFactory(entityManager)

        val result = query.selectFrom(QDummyEntity.dummyEntity)
            .fetchOne()

        result!!.name shouldBe "goodall"
    }
}
