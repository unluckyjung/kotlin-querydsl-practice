package com.study.querydsl.trade.domain

import com.querydsl.core.annotations.QueryProjection
import javax.persistence.*

@Table(name = "trades")
@Entity
class Trade(
    @Column(name = "buyer_id", nullable = false)
    val buyerId: Long,

    @Column(name = "seller_id", nullable = false)
    val sellerId: Long,

    @Column(name = "trade_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)

data class TraderNames @QueryProjection constructor(
    val buyerName: String,
    val sellerName: String,
)
