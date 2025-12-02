package com.example.uas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.uas.data.ProductRepository
import com.example.uas.model.Order
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class TrackOrderActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Important for osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_track_order)

        val orderId = intent.getStringExtra("ORDER_ID")
        val order = ProductRepository.getOrderById(orderId ?: "")

        if (order == null) {
            finish()
            return
        }

        val tvTrackingTitle = findViewById<TextView>(R.id.tvTrackingTitle)
        val tvTrackingStatus = findViewById<TextView>(R.id.tvTrackingStatus)
        tvTrackingTitle.text = "Track Order #${order.id}"
        tvTrackingStatus.text = "Status: ${order.status}"

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        val mapController = mapView.controller
        mapController.setZoom(15.0)
        
        // Use order location or default to Jakarta
        val startPoint = GeoPoint(order.currentLatitude, order.currentLongitude)
        mapController.setCenter(startPoint)

        val marker = Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Delivery Location"
        mapView.overlays.add(marker)
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
