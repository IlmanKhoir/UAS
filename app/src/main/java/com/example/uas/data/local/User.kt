package com.example.uas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String?,
    val phone: String?,
    val password_hash: String,
    val created_at: Long = System.currentTimeMillis(),
    val address: String?
)
