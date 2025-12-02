package com.example.uas.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist",
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
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val product_id: Int,
    val created_at: Long = System.currentTimeMillis()
)
