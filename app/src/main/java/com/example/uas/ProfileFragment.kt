package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uas.util.SessionManager

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var layoutLoggedOut: LinearLayout
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn)
        layoutLoggedOut = view.findViewById(R.id.layoutLoggedOut)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)

        // Logged Out Buttons
        view.findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        // Logged In Buttons
        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            sessionManager.logout()
            updateUI()
        }

        view.findViewById<View>(R.id.btnOrderHistory).setOnClickListener {
            // Switch to Orders tab
            activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.nav_view)?.selectedItemId = R.id.navigation_orders
        }
        
        view.findViewById<View>(R.id.btnWishlist).setOnClickListener {
             startActivity(Intent(requireContext(), WishlistActivity::class.java))
        }

        view.findViewById<View>(R.id.btnHelp).setOnClickListener {
            Toast.makeText(requireContext(), "Contact support at support@example.com", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (sessionManager.isLoggedIn()) {
            layoutLoggedIn.visibility = View.VISIBLE
            layoutLoggedOut.visibility = View.GONE
            tvUserName.text = sessionManager.getUserName()
            tvUserEmail.text = sessionManager.getUserEmail()
        } else {
            layoutLoggedIn.visibility = View.GONE
            layoutLoggedOut.visibility = View.VISIBLE
        }
    }
}
