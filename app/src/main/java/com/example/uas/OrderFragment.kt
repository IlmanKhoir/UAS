package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas.adapter.OrderAdapter

import kotlinx.coroutines.launch

class OrderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvOrders = view.findViewById<RecyclerView>(R.id.rvOrders)
        rvOrders.layoutManager = LinearLayoutManager(requireContext())

        val sessionManager = com.example.uas.util.SessionManager(requireContext())
        
        if (sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                val db = com.example.uas.data.local.AppDatabase.getDatabase(requireContext().applicationContext)
                val userId = sessionManager.getUserId()
                val orderEntities = db.orderDao().getOrdersByUserId(userId)
                
                val orders = orderEntities.map { entity ->
                    com.example.uas.model.Order(
                        id = entity.id.toString(),
                        date = java.util.Date(entity.order_date),
                        totalAmount = entity.total_amount.toDouble(),
                        status = try { com.example.uas.model.OrderStatus.valueOf(entity.status.uppercase()) } catch (_: Exception) { com.example.uas.model.OrderStatus.PLACED },
                        items = emptyList() // We simplified items for now, or parse JSON if needed
                    )
                }
                
                val adapter = OrderAdapter(orders) { order ->
                    val intent = Intent(requireContext(), TrackOrderActivity::class.java)
                    intent.putExtra("ORDER_ID", order.id)
                    startActivity(intent)
                }
                rvOrders.adapter = adapter
            }
        } else {
            // Not logged in, show empty list
            val adapter = OrderAdapter(emptyList()) { _ -> }
            rvOrders.adapter = adapter
        }
    }
}
