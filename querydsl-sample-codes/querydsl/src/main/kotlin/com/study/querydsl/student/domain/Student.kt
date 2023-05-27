package com.study.querydsl.student.domain

import javax.persistence.*

@Table(name = "students")
@Entity
class Student(
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "grade", nullable = false)
    val grade: Int,

    @Column(name = "age", nullable = false)
    val age: Int,

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
