package com.study.querydsl.member.domain

import com.querydsl.core.annotations.QueryProjection
import javax.persistence.*

@Table(name = "member")
@Entity
class Member(
    @Column(name = "member_name", nullable = true)
    val name: String? = null,

    @Column(name = "member_age", nullable = false)
    val age: Int = 0,

    team: Team? = null,

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

    override fun toString(): String {
        return "Member(name=$name, age=$age, id=$id)"
    }
}


data class MemberInfo @QueryProjection constructor(
    val company: String,
    val name: String,
    val age: Int,
)
