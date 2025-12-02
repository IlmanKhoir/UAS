package com.example.uas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WishlistDao {
    @Insert
    suspend fun insertWishlist(wishlist: WishlistEntity): Long

    @Delete
    suspend fun deleteWishlist(wishlist: WishlistEntity)

    @Query("SELECT * FROM wishlist WHERE user_id = :userId")
    suspend fun getWishlistByUserId(userId: Int): List<WishlistEntity>

    @Query("SELECT * FROM wishlist WHERE user_id = :userId AND product_id = :productId")
    suspend fun getWishlistItem(userId: Int, productId: Int): WishlistEntity?
}
