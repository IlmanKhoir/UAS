package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uas.data.ProductRepository


import androidx.lifecycle.lifecycleScope
import com.example.uas.data.local.AppDatabase
import com.example.uas.data.local.WishlistEntity
import com.example.uas.util.SessionManager
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val product = ProductRepository.getProductById(productId)

        if (product == null) {
            finish()
            return
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvDetailDescription = findViewById<TextView>(R.id.tvDetailDescription)
        val btnAddToCart = findViewById<Button>(R.id.btnAddToCart)
        val ivDetailFavorite = findViewById<ImageView>(R.id.ivDetailFavorite)

        tvDetailName.text = product.name
        tvDetailPrice.text = "$${product.price}"
        tvDetailDescription.text = product.description

        Glide.with(this)
            .load(product.imageUrl)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(ivDetailImage)

        btnAddToCart.setOnClickListener {
            ProductRepository.addToCart(product)
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
        }

        // Wishlist Logic
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        val db = AppDatabase.getDatabase(applicationContext)

        if (userId != -1) {
            lifecycleScope.launch {
                val existingItem = db.wishlistDao().getWishlistItem(userId, product.id)
                updateFavoriteIcon(ivDetailFavorite, existingItem != null)
            }
        }

        ivDetailFavorite.setOnClickListener {
            if (userId == -1) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    try {
                        val existingItem = db.wishlistDao().getWishlistItem(userId, product.id)
                        if (existingItem != null) {
                            db.wishlistDao().deleteWishlist(existingItem)
                            updateFavoriteIcon(ivDetailFavorite, false)
                            Toast.makeText(this@DetailActivity, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                        } else {
                            val newItem = WishlistEntity(user_id = userId, product_id = product.id)
                            db.wishlistDao().insertWishlist(newItem)
                            updateFavoriteIcon(ivDetailFavorite, true)
                            Toast.makeText(this@DetailActivity, "Added to Wishlist", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DetailActivity, "Error updating wishlist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFavoriteIcon(imageView: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            imageView.setColorFilter(android.graphics.Color.parseColor("#F44336")) // Red
        } else {
            imageView.setColorFilter(android.graphics.Color.parseColor("#BDBDBD")) // Grey
        }
    }
}
