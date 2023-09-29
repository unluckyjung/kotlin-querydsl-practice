package com.study.querydsl.student.domain

enum class StudentSortType(desc: String) {
    AGE_ASC(desc = "나이 오름차순"),
    AGE_DESC(desc = "나이 내림차순"),
}
