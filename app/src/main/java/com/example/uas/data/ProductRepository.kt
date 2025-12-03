package com.example.uas.data

import com.example.uas.model.CartItem
import com.example.uas.model.Order
import com.example.uas.model.Product

object ProductRepository {
    private val products = listOf(
        Product(
            1,
            "Wireless Headphones",
            "Premium noise-cancelling wireless headphones with 30-hour battery life.",
            199.99,
            "android.resource://com.example.uas/drawable/wirelessheadphone",
            "Electronics"
        ),
        Product(
            2,
            "Smart Watch",
            "Fitness tracker with heart rate monitor and GPS.",
            149.50,
            "android.resource://com.example.uas/drawable/smartwatch",
            "Electronics"
        ),
        Product(
            3,
            "Running Shoes",
            "Lightweight running shoes for daily training.",
            89.99,
            "android.resource://com.example.uas/drawable/sepatulari",
            "Sports"
        ),
        Product(
            4,
            "Backpack",
            "Durable backpack with laptop compartment.",
            49.99,
            "android.resource://com.example.uas/drawable/tas",
            "Fashion"
        ),
        Product(
            5,
            "Sunglasses",
            "Classic aviator sunglasses with UV protection.",
            120.00,
            "android.resource://com.example.uas/drawable/kacamata",
            "Fashion"
        ),
        Product(
            6,
            "Gaming Mouse",
            "High-precision gaming mouse with RGB lighting.",
            59.99,
            "android.resource://com.example.uas/drawable/mouse",
            "Electronics"
        ),
        Product(
            7,
            "Remote AC",
            "Universal Remote AC for all brands.",
            15.00,
            "android.resource://com.example.uas/drawable/remotac",
            "Electronics"
        ),
        Product(
            8,
            "Smart TV",
            "55-inch 4K Ultra HD Smart TV with HDR support.",
            499.99,
            "android.resource://com.example.uas/drawable/tv_samsung_32_kelasfhd",
            "Electronics"),
        Product(
            9,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),
        Product(
            10,
            "HP Realme Type C",
            "HP OP",
            499.99,
            "android.resource://com.example.uas/drawable/hprealme",
            "Electronics"
        ),
        Product(
            11,
            "Ultrawear",
            "nyaman sepanjang hari.",
            499.99,
            "android.resource://com.example.uas/drawable/ultrawear",
            "Beauty"
        ),
        Product(
            12,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),
        Product(
            13,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),Product(
            14,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),
        Product(
            15,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),
        Product(
            16,
            "Gosokan Listrik",
            "Gosokan dengan garansi 5 tahun.",
            499.99,
            "android.resource://com.example.uas/drawable/gosokan",
            "Electronics"
        ),


    )

    private val cartItems = mutableListOf<CartItem>()
    private val orders = mutableListOf<Order>()

    fun getProducts(): List<Product> = products

    fun getProductById(id: Int): Product? = products.find { it.id == id }

    fun getCartItems(): List<CartItem> = cartItems

    fun addToCart(product: Product) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(product, 1))
        }
    }

    fun removeFromCart(product: Product) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity--
            } else {
                cartItems.remove(existingItem)
            }
        }
    }

    fun getCartTotal(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }

    fun placeOrder() {
        val order = Order(
            id = "ORD-${System.currentTimeMillis()}",
            date = java.util.Date(),
            totalAmount = getCartTotal(),
            items = ArrayList(cartItems)
        )
        orders.add(order)
        cartItems.clear()
    }

    fun getOrders(): List<Order> = orders

    fun getOrderById(id: String): Order? = orders.find { it.id == id }
}
