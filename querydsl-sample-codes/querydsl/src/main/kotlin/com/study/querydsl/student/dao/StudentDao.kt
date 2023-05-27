package com.study.querydsl.student.dao

import com.querydsl.core.BooleanBuilder
import com.study.querydsl.student.domain.QStudent.student
import com.study.querydsl.student.domain.Student
import com.study.querydsl.student.domain.StudentSortType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

interface StudentRepository : JpaRepository<Student, Long>

@Repository
class StudentDao(
    private val studentRepository: StudentRepository,
) : QuerydslRepositorySupport(Student::class.java) {
    fun getStudents(sortType: StudentSortType, size: Long, cursor: Long? = null): List<Student> {
        val predicate = BooleanBuilder()

        cursor?.let { it ->
            val cursorStudent =
                studentRepository.findByIdOrNull(it) ?: throw IllegalArgumentException("잘못된 커서가 들어왔습니다.")
            predicate.and(
                sortType.getPredicate(cursorStudent = cursorStudent)
            )
        }
        
        val query = from(student)
            .where(predicate)
            .limit(size)

        return querydsl!!.applySorting(sortType.qSort, query).fetch()
    }
}

