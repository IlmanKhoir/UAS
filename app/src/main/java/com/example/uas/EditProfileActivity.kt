package com.example.uas

import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.uas.data.local.AppDatabase
import com.example.uas.data.local.User
import com.example.uas.util.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var ivProfileImage: ImageView
    private lateinit var sessionManager: SessionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivProfileImage.setImageURI(uri)
            
            val flag = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, flag)
            } catch (_: SecurityException) {
                // Handle exception if permission cannot be taken
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = SessionManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnSelectImage = findViewById<android.view.View>(R.id.btnSelectImage)
        val btnGetLocation = findViewById<Button>(R.id.btnGetLocation)

        btnSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnGetLocation.setOnClickListener {
            getLocation()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        loadUserData()
    }

    private fun loadUserData() {
        val email = sessionManager.getEmail() ?: return
        
        ApiClient.instance.getUser(email).enqueue(object : retrofit2.Callback<com.example.uas.data.remote.UserResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.uas.data.remote.UserResponse>,
                response: retrofit2.Response<com.example.uas.data.remote.UserResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()!!
                    etName.setText(user.name)
                    etEmail.setText(user.email)
                    etPhone.setText(user.phone)
                    etAddress.setText(user.address)
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.uas.data.remote.UserResponse>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfile() {
        val name = etName.text.toString()
        val phone = etPhone.text.toString()
        val address = etAddress.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.name_cannot_be_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val email = sessionManager.getEmail() ?: return

        // Menggunakan API Remote (Railway)
        ApiClient.instance.updateUser(email, name, phone, address).enqueue(object : retrofit2.Callback<com.example.uas.data.remote.BaseResponse> {
            override fun onResponse(
                call: retrofit2.Call<com.example.uas.data.remote.BaseResponse>,
                response: retrofit2.Response<com.example.uas.data.remote.BaseResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update session
                    sessionManager.createLoginSession(sessionManager.getUserId(), name, email)
                    Toast.makeText(this@EditProfileActivity, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Update Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.uas.data.remote.BaseResponse>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                if (addresses.isNotEmpty()) {
                                    val address = addresses[0].getAddressLine(0)
                                    runOnUiThread {
                                        etAddress.setText(address)
                                    }
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val address = addresses[0].getAddressLine(0)
                                etAddress.setText(address)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, getString(R.string.error_getting_address, e.message), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show()
                }
            }
    }
}
