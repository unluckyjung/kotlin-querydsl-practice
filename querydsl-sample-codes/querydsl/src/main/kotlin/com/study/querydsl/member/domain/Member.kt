package com.study.querydsl.member.domain

import com.querydsl.core.annotations.QueryProjection
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLDeleteAll
import org.hibernate.annotations.Where
import java.time.ZonedDateTime
import javax.persistence.*

@SQLDelete(sql = "UPDATE member SET deleted_at = NOW() WHERE member_id = ?")
@Where(clause = "deleted_at is null")
@Table(name = "member")
@Entity
class Member(
    @Column(name = "member_name", nullable = true)
    val name: String? = null,

    @Column(name = "member_age", nullable = false)
    val age: Int = 0,

    team: Team? = null,

    deletedAt: ZonedDateTime? = null,

    @Column(name = "member_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    var team = team
        protected set

    fun changeTeam(team: Team) {
        this.team = team
        team.members.add(this)
    }

    @Column(name = "deleted_at")
    var deletedAt: ZonedDateTime? = deletedAt
        protected set

    fun delete(deletedAt: ZonedDateTime = ZonedDateTime.now()) {
        this.deletedAt = deletedAt
    }

    override fun toString(): String {
        return "Member(name=$name, age=$age, id=$id)"
    }
}


data class MemberInfo @QueryProjection constructor(
    val company: String,
    val name: String,
    val age: Int,
)


// noArgusConstructor 를 위해서 기본값
// setter 를 위해서 var
data class MemberDtoWithNoArgusAndSetter(
    var name: String = "",
    var age: Int = 30,
)

// noArgusConstructor 를 위해서 기본값
data class MemberDtoWithNoArgus(
    val name: String = "",
    val age: Int = 30,
)

data class MemberDtoOtherFieldName(
    val userName: String = "",
    val age: Int = 30,
)


data class MemberDtoByConstructor(
    val name: String,
    val age: Int,
)

// QueryDSL 에 의존적인 Dto가 된다.
// 생성자 위에 달아주어야 하기 때문에, 코틀린의 경우 constructor 를 명시해주어야한다.
data class MemberDtoByAnnotation @QueryProjection constructor(
    val name: String,
    val age: Int,
)

data class NameCountDto @QueryProjection constructor(
    val name: String,
    val count: Long,
)


