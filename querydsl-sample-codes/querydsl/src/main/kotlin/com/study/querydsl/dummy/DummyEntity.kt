package com.study.querydsl.dummy

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class DummyEntity(
    val name: String,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
)