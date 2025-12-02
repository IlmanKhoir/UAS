package com.example.uas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uas.R
import com.example.uas.model.CartItem

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCartProductImage: ImageView = itemView.findViewById(R.id.ivCartProductImage)
        val tvCartProductName: TextView = itemView.findViewById(R.id.tvCartProductName)
        val tvCartProductPrice: TextView = itemView.findViewById(R.id.tvCartProductPrice)
        val tvCartQuantity: TextView = itemView.findViewById(R.id.tvCartQuantity)
        val btnRemoveFromCart: ImageButton = itemView.findViewById(R.id.btnRemoveFromCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val product = cartItem.product

        holder.tvCartProductName.text = product.name
        holder.tvCartProductPrice.text = "$${product.price}"
        holder.tvCartQuantity.text = "Qty: ${cartItem.quantity}"

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivCartProductImage)

        holder.btnRemoveFromCart.setOnClickListener {
            onRemoveClick(cartItem)
        }
    }

    override fun getItemCount(): Int = cartItems.size
}
