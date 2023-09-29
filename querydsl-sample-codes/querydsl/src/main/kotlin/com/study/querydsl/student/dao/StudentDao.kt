package com.study.querydsl.student.dao

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.study.querydsl.student.domain.QStudent.student
import com.study.querydsl.student.domain.Student
import com.study.querydsl.student.domain.StudentSortType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.querydsl.QSort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

interface StudentRepository : JpaRepository<Student, Long>

@Repository
class StudentDao(
    private val studentRepository: StudentRepository,
) : QuerydslRepositorySupport(Student::class.java) {
    fun getStudents(sortType: StudentSortType, size: Long, cursor: Long? = null): List<Student> {
        val cursorStudent = cursor?.let { it ->
            studentRepository.findByIdOrNull(it) ?: throw IllegalArgumentException("잘못된 커서가 들어왔습니다.")
        }

        val studentQuery = StudentQuery(sortType = sortType, cursorStudent = cursorStudent)

        val query = from(student)
            .where(studentQuery.predicate)
            .limit(size)

        return querydsl!!.applySorting(studentQuery.sort, query).fetch()
    }
}

internal class StudentQuery(
    sortType: StudentSortType,
    cursorStudent: Student? = null,
) {
    private val qSortType = StudentQSort.of(sortType = sortType)

    val sort = qSortType.sort
    val predicate = BooleanBuilder().apply {
        cursorStudent?.let { this.and(qSortType.getPredicate(it)) }
    }
}

internal enum class StudentQSort(val sort: QSort) {
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

    companion object {
        fun of(sortType: StudentSortType): StudentQSort {
            return when (sortType) {
                StudentSortType.AGE_DESC -> AGE_DESC
                StudentSortType.AGE_ASC -> AGE_ASC
            }
        }
    }
}
