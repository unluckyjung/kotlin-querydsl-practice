package com.study.querydsl.student.domain

import com.querydsl.core.types.Predicate
import com.study.querydsl.student.domain.QStudent.student
import org.springframework.data.querydsl.QSort

enum class StudentSortType(val qSort: QSort) {
    AGE_ASC(QSort(student.age.asc(), student.id.asc())) {
        override fun getPredicate(cursorStudent: Student): Predicate {
            return student.age.gt(cursorStudent.age).or(
                (student.age.eq(cursorStudent.age).and(student.id.gt(cursorStudent.id)))
            )
        }
    },
    AGE_DESC(QSort(student.age.desc(), student.id.asc())) {
        override fun getPredicate(cursorStudent: Student): Predicate {
            return student.age.lt(cursorStudent.age).or(
                (student.age.eq(cursorStudent.age).and(student.id.gt(cursorStudent.id)))
            )
        }
    };

    abstract fun getPredicate(cursorStudent: Student): Predicate
}
