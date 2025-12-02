package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.adapter.CartAdapter
import com.example.uas.data.ProductRepository

class CartFragment : Fragment() {

    private lateinit var adapter: CartAdapter
    private lateinit var tvCartTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCartItems = view.findViewById<RecyclerView>(R.id.rvCartItems)
        tvCartTotal = view.findViewById<TextView>(R.id.tvCartTotal)
        val btnCheckout = view.findViewById<Button>(R.id.btnCheckout)

        rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        
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
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireContext(), PaymentActivity::class.java)
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
