package com.example.uas.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uas.R
import com.example.uas.model.Product

class ProductAdapter(
    private val products: List<Product>,
    private var wishlistProductIds: Set<Int> = emptySet(),
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "$${product.price}"

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivProductImage)

        val isWishlisted = wishlistProductIds.contains(product.id)
        if (isWishlisted) {
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#F44336")) // Red
        } else {
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#BDBDBD")) // Grey
        }

        holder.ivFavorite.setOnClickListener {
            onFavoriteClick(product)
        }

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    fun updateWishlist(newWishlist: Set<Int>) {
        wishlistProductIds = newWishlist
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = products.size
}
