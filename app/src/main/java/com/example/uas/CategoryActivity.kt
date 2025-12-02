package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.adapter.ProductAdapter
import com.example.uas.data.ProductRepository
import com.example.uas.data.local.AppDatabase
import com.example.uas.data.local.WishlistEntity
import com.example.uas.util.SessionManager
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "All"

        sessionManager = SessionManager(this)
        rvProducts = findViewById(R.id.rvProducts)
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle)
        
        tvCategoryTitle.text = categoryName

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvProducts.layoutManager = GridLayoutManager(this, 2)

        loadProducts(categoryName)
    }

    private fun loadProducts(category: String) {
        val allProducts = ProductRepository.getProducts()
        val filteredProducts = if (category == "All") {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }

        val userId = sessionManager.getUserId()
        val db = AppDatabase.getDatabase(applicationContext)

        adapter = ProductAdapter(
            filteredProducts,
            emptySet(),
            onProductClick = { product ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onFavoriteClick = { product ->
                if (userId == -1) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        try {
                            val existingItem = db.wishlistDao().getWishlistItem(userId, product.id)
                            if (existingItem != null) {
                                db.wishlistDao().deleteWishlist(existingItem)
                                Toast.makeText(this@CategoryActivity, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                            } else {
                                val newItem = WishlistEntity(user_id = userId, product_id = product.id)
                                db.wishlistDao().insertWishlist(newItem)
                                Toast.makeText(this@CategoryActivity, "Added to Wishlist", Toast.LENGTH_SHORT).show()
                            }
                            // Refresh wishlist state in adapter
                            val updatedWishlist = db.wishlistDao().getWishlistByUserId(userId)
                            val updatedIds = updatedWishlist.map { it.product_id }.toSet()
                            adapter.updateWishlist(updatedIds)
                        } catch (_: Exception) {
                            Toast.makeText(this@CategoryActivity, "Error updating wishlist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
        rvProducts.adapter = adapter

        // Load initial wishlist state
        if (userId != -1) {
            lifecycleScope.launch {
                val wishlist = db.wishlistDao().getWishlistByUserId(userId)
                val ids = wishlist.map { it.product_id }.toSet()
                adapter.updateWishlist(ids)
            }
        }
    }
}
