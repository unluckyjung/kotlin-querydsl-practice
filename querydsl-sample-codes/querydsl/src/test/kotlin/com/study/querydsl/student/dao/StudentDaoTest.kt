package com.study.querydsl.student.dao

import com.study.querydsl.common.IntegrationTest
import com.study.querydsl.student.domain.Student
import com.study.querydsl.student.domain.StudentSortType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@IntegrationTest
class StudentDaoTest(
    private val studentRepository: StudentRepository,
    private val studentDao: StudentDao,
) {
    @DisplayName("정렬조건을 나이 오름차순으로 조회 한다면, 나이가 같은 케이스도 커서 처리가되어 조회된다.")
    @Test
    fun cursorPagingTest1() {
        val sortType = StudentSortType.AGE_ASC

        studentRepository.save(
            Student(name = "goodall", age = 10, grade = 1)
        )

        studentRepository.save(
            Student(name = "unluckyjung", age = 20, grade = 1)
        )

        studentRepository.save(
            Student(name = "yoonsung", age = 10, grade = 1)
        )

        val result = studentDao.getStudents(
            sortType = sortType,
            size = 1,
        )
        result.size shouldBe 1
        result[0].name shouldBe "goodall"

        val result2 = studentDao.getStudents(
            sortType = sortType,
            size = 3,
            cursor = result[0].id
        )
        result2.size shouldBe 2
        result2[0].name shouldBe "yoonsung"
        result2[1].name shouldBe "unluckyjung"
    }

    @DisplayName("정렬조건을 나이 내림차순으로 조회 한다면, 나이가 같은 케이스도 커서 처리가되어 조회된다.")
    @Test
    fun cursorPagingTest2() {
        val sortType = StudentSortType.AGE_DESC

        studentRepository.save(
            Student(name = "goodall", age = 10, grade = 1)
        )

        studentRepository.save(
            Student(name = "unluckyjung", age = 20, grade = 1)
        )

        studentRepository.save(
            Student(name = "yoonsung", age = 10, grade = 1)
        )

        val result = studentDao.getStudents(
            sortType = sortType,
            size = 1,
        )
        result.size shouldBe 1
        result[0].name shouldBe "unluckyjung"

        val result2 = studentDao.getStudents(
            sortType = sortType,
            size = 3,
            cursor = result[0].id
        )
        result2.size shouldBe 2
        result2[0].name shouldBe "goodall"
        result2[1].name shouldBe "yoonsung"
    }
}
