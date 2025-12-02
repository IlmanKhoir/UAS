package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uas.data.ProductRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)

        val tvPaymentTotal = findViewById<TextView>(R.id.tvPaymentTotal)
        tvPaymentTotal.text = String.format("$%.2f", totalAmount)

        val rgPaymentMethod = findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val layoutCreditCard = findViewById<LinearLayout>(R.id.layoutCreditCard)
        val rbCreditCard = findViewById<RadioButton>(R.id.rbCreditCard)

        rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCreditCard) {
                layoutCreditCard.visibility = View.VISIBLE
            } else {
                layoutCreditCard.visibility = View.GONE
            }
        }

        val etCardNumber = findViewById<TextInputEditText>(R.id.etCardNumber)
        val etExpiry = findViewById<TextInputEditText>(R.id.etExpiry)
        val etCVV = findViewById<TextInputEditText>(R.id.etCVV)
        val btnPayNow = findViewById<Button>(R.id.btnPayNow)

        btnPayNow.setOnClickListener {
            if (rbCreditCard.isChecked) {
                if (validateCreditCard(etCardNumber, etExpiry, etCVV)) {
                    processPayment()
                }
            } else {
                processPayment()
            }
        }
    }

    private fun validateCreditCard(
        etCardNumber: TextInputEditText,
        etExpiry: TextInputEditText,
        etCVV: TextInputEditText
    ): Boolean {
        if (etCardNumber.text.isNullOrEmpty() || etCardNumber.text!!.length != 16) {
            etCardNumber.error = "Invalid Card Number"
            return false
        }
        if (etExpiry.text.isNullOrEmpty()) {
            etExpiry.error = "Invalid Expiry"
            return false
        }
        if (etCVV.text.isNullOrEmpty() || etCVV.text!!.length != 3) {
            etCVV.error = "Invalid CVV"
            return false
        }
        return true
    }

    private fun processPayment() {
        // Simulate payment processing
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show()
        
        // Save to DB if logged in
        val sessionManager = com.example.uas.util.SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserId()
            val cartItems = ProductRepository.getCartItems()
            val total = ProductRepository.getCartTotal()
            
            // Simple serialization
            val itemsString = cartItems.joinToString(";") { "${it.product.name} x${it.quantity}" }

            lifecycleScope.launch {
                val db = com.example.uas.data.local.AppDatabase.getDatabase(applicationContext)
                val order = com.example.uas.data.local.OrderEntity(
                    user_id = userId,
                    total_amount = total.toInt(),
                    status = "Paid",
                    shipping_address = "Default Address", // In real app, get from input
                    payment_method = if (findViewById<RadioButton>(R.id.rbCreditCard).isChecked) "Credit Card" else "Cash",
                    tracking_number = "TRK-${System.currentTimeMillis()}",
                    items_json = itemsString
                )
                db.orderDao().insertOrder(order)
            }
        }

        // Create Order (Legacy/In-Memory)
        ProductRepository.placeOrder()

        // Navigate to Order Tracking
        // Navigate to Order Tracking (via MainActivity)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("NAVIGATE_TO", "ORDERS")
        startActivity(intent)
        finish()
    }
}
