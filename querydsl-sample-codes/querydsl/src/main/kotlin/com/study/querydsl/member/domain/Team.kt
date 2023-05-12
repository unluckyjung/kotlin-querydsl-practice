package com.study.querydsl.member.domain

import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import java.time.ZonedDateTime
import javax.persistence.*


@SQLDelete(sql = "UPDATE team SET deleted_at = NOW() WHERE team_id = ?")
@Where(clause = "deleted_at is null")
@Table(name = "team")
@Entity
class Team(

    @Column(name = "team_name", nullable = false)
    val name: String,

    @OneToMany(mappedBy = "team", orphanRemoval = true, cascade = [CascadeType.ALL])
    val members: MutableList<Member> = mutableListOf(),

    deletedAt: ZonedDateTime? = null,

    @Column(name = "team_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    override fun toString(): String {
        return "Team(name='$name', id=$id)"
    }

    @Column(name = "deleted_at")
    var deletedAt: ZonedDateTime? = deletedAt
        protected set

    fun delete() {
        val now = ZonedDateTime.now()
        deletedAt = now

        members.forEach {
            it.delete()
        }
    }
}
