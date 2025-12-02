package com.example.uas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["user_id"])]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val total_amount: Int,
    val status: String,
    val order_date: Long = System.currentTimeMillis(),
    val shipping_address: String?,
    val payment_method: String?,
    val tracking_number: String?,
    val items_json: String // JSON string for items
)
