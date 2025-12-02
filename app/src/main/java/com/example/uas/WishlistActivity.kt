package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.uas.util.SessionManager
import kotlinx.coroutines.launch

class WishlistActivity : AppCompatActivity() {

    private lateinit var rvWishlist: RecyclerView
    private lateinit var tvEmptyWishlist: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        sessionManager = SessionManager(this)
        rvWishlist = findViewById(R.id.rvWishlist)
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvWishlist.layoutManager = GridLayoutManager(this, 2)

        loadWishlist()
    }

    private fun loadWishlist() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "Please login to view wishlist", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val wishlistEntities = db.wishlistDao().getWishlistByUserId(userId)
            
            if (wishlistEntities.isEmpty()) {
                rvWishlist.visibility = View.GONE
                tvEmptyWishlist.visibility = View.VISIBLE
            } else {
                rvWishlist.visibility = View.VISIBLE
                tvEmptyWishlist.visibility = View.GONE

                val wishlistIds = wishlistEntities.map { it.product_id }.toSet()
                val allProducts = ProductRepository.getProducts()
                val wishlistProducts = allProducts.filter { wishlistIds.contains(it.id) }

                adapter = ProductAdapter(
                    wishlistProducts,
                    wishlistIds,
                    onProductClick = { product ->
                        val intent = Intent(this@WishlistActivity, DetailActivity::class.java)
                        intent.putExtra("PRODUCT_ID", product.id)
                        startActivity(intent)
                    },
                    onFavoriteClick = { product ->
                        lifecycleScope.launch {
                            val existingItem = db.wishlistDao().getWishlistItem(userId, product.id)
                            if (existingItem != null) {
                                db.wishlistDao().deleteWishlist(existingItem)
                                Toast.makeText(this@WishlistActivity, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                                loadWishlist() // Reload to remove item from list
                            }
                        }
                    }
                )
                rvWishlist.adapter = adapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }
}
