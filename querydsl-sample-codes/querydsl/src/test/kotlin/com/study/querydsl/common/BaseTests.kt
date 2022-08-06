package com.study.querydsl.common

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
annotation class TestEnvironment

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Transactional
@SpringBootTest
@TestEnvironment
annotation class IntegrationTest