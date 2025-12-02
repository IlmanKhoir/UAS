package com.example.uas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.uas.data.ProductRepository
import com.example.uas.data.local.AppDatabase
import com.example.uas.model.Order
import com.example.uas.model.OrderStatus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class TrackOrderActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // Dummy Store Location (e.g., Central Jakarta)
    private val storeLocation = GeoPoint(-6.1751, 106.8650) 

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Important for osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_track_order)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val orderId = intent.getStringExtra("ORDER_ID")
        
        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        
        lifecycleScope.launch {
            var order = ProductRepository.getOrderById(orderId ?: "")
            
            if (order == null) {
                // Try to fetch from DB
                val db = AppDatabase.getDatabase(applicationContext)
                val idInt = orderId?.toIntOrNull()
                if (idInt != null) {
                    val entity = db.orderDao().getOrderById(idInt)
                    if (entity != null) {
                        order = Order(
                            id = entity.id.toString(),
                            date = java.util.Date(entity.order_date),
                            totalAmount = entity.total_amount.toDouble(),
                            status = try { OrderStatus.valueOf(entity.status.uppercase()) } catch (_: Exception) { OrderStatus.PLACED },
                            items = emptyList() // Simplified
                        )
                    }
                }
            }

            if (order == null) {
                finish()
                return@launch
            }

            val tvTrackingTitle = findViewById<TextView>(R.id.tvTrackingTitle)
            val tvTrackingStatus = findViewById<TextView>(R.id.tvTrackingStatus)
            tvTrackingTitle.text = getString(R.string.track_order_header, order.id)
            tvTrackingStatus.text = getString(R.string.status_header, order.status)

            // Update Timeline
            val ivStep1 = findViewById<android.widget.ImageView>(R.id.ivStep1)
            val viewLine1 = findViewById<android.view.View>(R.id.viewLine1)
            val ivStep2 = findViewById<android.widget.ImageView>(R.id.ivStep2)
            val viewLine2 = findViewById<android.view.View>(R.id.viewLine2)
            val ivStep3 = findViewById<android.widget.ImageView>(R.id.ivStep3)
            
            val purple = ContextCompat.getColor(this@TrackOrderActivity, R.color.purple_500)
            val gray = ContextCompat.getColor(this@TrackOrderActivity, R.color.gray_500)
            val white = ContextCompat.getColor(this@TrackOrderActivity, R.color.white)

            // Reset all
            ivStep1.background.setTint(gray)
            ivStep1.setColorFilter(white)
            viewLine1.setBackgroundColor(gray)
            ivStep2.background.setTint(gray)
            ivStep2.setColorFilter(white)
            viewLine2.setBackgroundColor(gray)
            ivStep3.background.setTint(gray)
            ivStep3.setColorFilter(white)

            when (order.status) {
                OrderStatus.PLACED -> {
                    ivStep1.background.setTint(purple)
                    tvTrackingStatus.text = getString(R.string.status_placed)
                }
                OrderStatus.PACKED -> {
                    ivStep1.background.setTint(purple)
                    viewLine1.setBackgroundColor(purple)
                    ivStep2.background.setTint(purple)
                    tvTrackingStatus.text = getString(R.string.status_packed)
                }
                OrderStatus.SHIPPED -> {
                    ivStep1.background.setTint(purple)
                    viewLine1.setBackgroundColor(purple)
                    ivStep2.background.setTint(purple)
                    viewLine2.setBackgroundColor(purple)
                    ivStep3.background.setTint(purple)
                    tvTrackingStatus.text = getString(R.string.status_shipped)
                }
                OrderStatus.DELIVERED -> {
                    ivStep1.background.setTint(purple)
                    viewLine1.setBackgroundColor(purple)
                    ivStep2.background.setTint(purple)
                    viewLine2.setBackgroundColor(purple)
                    ivStep3.background.setTint(purple)
                    tvTrackingStatus.text = getString(R.string.status_delivered)
                }
                OrderStatus.CANCELLED -> {
                    tvTrackingStatus.text = getString(R.string.status_cancelled)
                    tvTrackingStatus.background.setTint(ContextCompat.getColor(this@TrackOrderActivity, R.color.red_error))
                }
            }

            checkLocationPermissionAndMap()
        }
    }

    private fun checkLocationPermissionAndMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            setupMapWithLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMapWithLocation()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMapWithLocation() {
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
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLocation = GeoPoint(location.latitude, location.longitude)
                
                val mapController = mapView.controller
                mapController.setZoom(13.0)
                mapController.setCenter(userLocation)

                // Marker for Store
                val storeMarker = Marker(mapView)
                storeMarker.position = storeLocation
                storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                storeMarker.title = getString(R.string.store_location_title)
                storeMarker.icon = ContextCompat.getDrawable(this, R.drawable.ic_store_location)
                mapView.overlays.add(storeMarker)

                // Marker for User
                val userMarker = Marker(mapView)
                userMarker.position = userLocation
                userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                userMarker.title = getString(R.string.user_location_title)
                // userMarker.icon = ... // Use a different icon if available
                mapView.overlays.add(userMarker)

                // Draw Line
                val line = Polyline()
                line.addPoint(storeLocation)
                line.addPoint(userLocation)
                line.outlinePaint.color = ContextCompat.getColor(this, R.color.purple_500)
                line.outlinePaint.strokeWidth = 5.0f
                mapView.overlays.add(line)
                
                mapView.invalidate()
            } else {
                Toast.makeText(this, getString(R.string.unable_to_get_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
