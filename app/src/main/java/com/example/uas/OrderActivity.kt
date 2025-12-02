package com.example.uas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.uas.adapter.OrderAdapter
import com.example.uas.data.ProductRepository
import kotlinx.coroutines.launch

class OrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        val rvOrders = findViewById<RecyclerView>(R.id.rvOrders)
        rvOrders.layoutManager = LinearLayoutManager(this)

        val sessionManager = com.example.uas.util.SessionManager(this)
        
        if (sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                val db = com.example.uas.data.local.AppDatabase.getDatabase(applicationContext)
                val userId = sessionManager.getUserId()
                val orderEntities = db.orderDao().getOrdersByUserId(userId)
                
                val orders = orderEntities.map { entity ->
                    com.example.uas.model.Order(
                        id = entity.id.toString(),
                        date = java.util.Date(entity.order_date),
                        totalAmount = entity.total_amount.toDouble(),
                        status = try { com.example.uas.model.OrderStatus.valueOf(entity.status.uppercase()) } catch (e: Exception) { com.example.uas.model.OrderStatus.PLACED },
                        items = emptyList() // We simplified items for now, or parse JSON if needed
                    )
                }
                
                val adapter = OrderAdapter(orders) { order ->
                    val intent = Intent(this@OrderActivity, TrackOrderActivity::class.java)
                    intent.putExtra("ORDER_ID", order.id)
                    startActivity(intent)
                }
                rvOrders.adapter = adapter
            }
        } else {
            // Fallback to mock data or empty
            val adapter = OrderAdapter(ProductRepository.getOrders()) { order ->
                val intent = Intent(this, TrackOrderActivity::class.java)
                intent.putExtra("ORDER_ID", order.id)
                startActivity(intent)
            }
            rvOrders.adapter = adapter
        }
    }
}
