package com.study.querydsl.member.domain

import javax.persistence.*


@Table(name = "team")
@Entity
class Team(

    @Column(name = "team_name", nullable = false)
    val name: String,

    @OneToMany(mappedBy = "team")
    val members: MutableList<Member> = mutableListOf(),

    @Column(name = "team_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

) {
    override fun toString(): String {
        return "Team(name='$name', id=$id)"
    }
}
