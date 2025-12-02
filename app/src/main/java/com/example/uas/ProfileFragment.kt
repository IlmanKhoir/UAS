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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.uas.util.SessionManager
import androidx.core.net.toUri

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var layoutLoggedOut: LinearLayout
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivProfileImage: android.widget.ImageView

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
        
        val cardView = view.findViewById<androidx.cardview.widget.CardView>(R.id.cvProfileImage)
        ivProfileImage = cardView.getChildAt(0) as android.widget.ImageView

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

        view.findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
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
            
            // Load fresh data from DB including image
            lifecycleScope.launch {
                val db = com.example.uas.data.local.AppDatabase.getDatabase(requireContext())
                val user = db.userDao().getUserById(sessionManager.getUserId())
                if (user != null) {
                    tvUserName.text = user.name
                    tvUserEmail.text = user.email
                    
                    if (user.profile_picture_uri != null) {
                        try {
                            ivProfileImage.setImageURI(user.profile_picture_uri.toUri())
                        } catch (_: Exception) {
                            // Ignore
                        }
                    }
                }
            }

        } else {
            layoutLoggedIn.visibility = View.GONE
            layoutLoggedOut.visibility = View.VISIBLE
        }
    }
}
