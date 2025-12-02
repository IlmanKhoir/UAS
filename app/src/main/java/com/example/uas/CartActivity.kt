package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.adapter.CartAdapter
import com.example.uas.data.ProductRepository

class CartActivity : AppCompatActivity() {
    private lateinit var adapter: CartAdapter
    private lateinit var tvCartTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val rvCartItems = findViewById<RecyclerView>(R.id.rvCartItems)
        tvCartTotal = findViewById<TextView>(R.id.tvCartTotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        rvCartItems.layoutManager = LinearLayoutManager(this)
        
        updateCart()

        adapter = CartAdapter(ProductRepository.getCartItems()) { cartItem ->
            ProductRepository.removeFromCart(cartItem.product)
            updateCart()
            @Suppress("NotifyDataSetChanged")
            adapter.notifyDataSetChanged()
        }
        rvCartItems.adapter = adapter

        btnCheckout.setOnClickListener {
            if (ProductRepository.getCartItems().isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("TOTAL_AMOUNT", ProductRepository.getCartTotal())
                startActivity(intent)
            }
        }
    }

    private fun updateCart() {
        val total = ProductRepository.getCartTotal()
        tvCartTotal.text = String.format(java.util.Locale.US, "$%.2f", total)
    }
    
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            @Suppress("NotifyDataSetChanged")
            adapter.notifyDataSetChanged()
            updateCart()
        }
    }
}
