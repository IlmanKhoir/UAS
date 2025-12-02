package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.adapter.Category
import com.example.uas.adapter.CategoryAdapter
import com.example.uas.adapter.ProductAdapter
import com.example.uas.data.ProductRepository
import com.example.uas.data.local.AppDatabase
import com.example.uas.data.local.WishlistEntity
import com.example.uas.util.SessionManager
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Categories
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        val categories = listOf(
            Category("All", R.drawable.tool),
            Category("Electronics", R.drawable.icons8electronics100),
            Category("Fashion", R.drawable.iconfashion),
            // Category("Home", R.drawable.icon_home), // Removed as requested
            Category("Beauty", R.drawable.iconslipstick),
            Category("Sports", R.drawable.iconsports)
        )
        rvCategories.adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(requireContext(), CategoryActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category.name)
            startActivity(intent)
        }

        // Setup User Name
        val sessionManager = SessionManager(requireContext())
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val userName = sessionManager.getUserName()
        if (!userName.isNullOrEmpty()) {
            tvGreeting.text = userName
        }

        // Setup Products
        val rvProducts = view.findViewById<RecyclerView>(R.id.rvProducts)
        rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProducts.isNestedScrollingEnabled = false // Important for NestedScrollView
        
        val products = ProductRepository.getProducts()
        val db = AppDatabase.getDatabase(requireContext())
        val userId = sessionManager.getUserId()

        val adapter = ProductAdapter(
            products, 
            emptySet(),
            onProductClick = { product ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onFavoriteClick = { product ->
                if (userId == -1) {
                    Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
                } else {
                    lifecycleScope.launch {
                        try {
                            val existingItem = db.wishlistDao().getWishlistItem(userId, product.id)
                            if (existingItem != null) {
                                db.wishlistDao().deleteWishlist(existingItem)
                                Toast.makeText(requireContext(), "Removed from Wishlist", Toast.LENGTH_SHORT).show()
                            } else {
                                val newItem = WishlistEntity(user_id = userId, product_id = product.id)
                                db.wishlistDao().insertWishlist(newItem)
                                Toast.makeText(requireContext(), "Added to Wishlist", Toast.LENGTH_SHORT).show()
                            }
                            // Refresh wishlist
                            val updatedWishlist = db.wishlistDao().getWishlistByUserId(userId)
                            val updatedIds = updatedWishlist.map { it.product_id }.toSet()
                            (rvProducts.adapter as? ProductAdapter)?.updateWishlist(updatedIds)
                        } catch (_: android.database.sqlite.SQLiteConstraintException) {
                            Toast.makeText(requireContext(), "Error: User not found. Please login again.", Toast.LENGTH_LONG).show()
                            sessionManager.logout()
                            // Optional: Redirect to login
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error updating wishlist: ${e.message}", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        )
        rvProducts.adapter = adapter

        // Setup Search
        val etSearch = view.findViewById<android.widget.EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                val filteredProducts = products.filter {
                    it.name.lowercase().contains(query)
                }
                adapter.updateProducts(filteredProducts)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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
