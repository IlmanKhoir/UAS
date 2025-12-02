package com.example.uas.model

import java.util.Date

data class Order(
    val id: String,
    val date: Date,
    val totalAmount: Double,
    val items: List<CartItem>,
    var status: OrderStatus = OrderStatus.PLACED,
    var currentLatitude: Double = -6.2088, // Titik di jakarta
    var currentLongitude: Double = 106.8456
)

enum class OrderStatus {
    PLACED, PACKED, SHIPPED, DELIVERED, CANCELLED
}
