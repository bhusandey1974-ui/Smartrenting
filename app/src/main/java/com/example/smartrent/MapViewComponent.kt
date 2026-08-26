package com.example.smartrent

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

fun createHouseMarker(context: Context, hasVacantRooms: Boolean, label: String): Drawable {
    val size = 110
    val bitmap = Bitmap.createBitmap(size, size + 20, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (hasVacantRooms) android.graphics.Color.parseColor("#16A34A") else android.graphics.Color.parseColor("#DC2626")
        style = Paint.Style.FILL
        setShadowLayer(6f, 0f, 4f, android.graphics.Color.parseColor("#55000000"))
    }

    // Pin Bubble Circle
    canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), 42f, paint)

    // Pin Pointer Triangle
    val path = Path().apply {
        moveTo((size / 2 - 14).toFloat(), (size / 2 + 30).toFloat())
        lineTo((size / 2 + 14).toFloat(), (size / 2 + 30).toFloat())
        lineTo((size / 2).toFloat(), (size + 14).toFloat())
        close()
    }
    canvas.drawPath(path, paint)

    // White House Icon Silhouette
    val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    val housePath = Path().apply {
        // Roof
        moveTo((size / 2).toFloat(), (size / 2 - 20).toFloat())
        lineTo((size / 2 - 18).toFloat(), (size / 2 - 3).toFloat())
        lineTo((size / 2 - 13).toFloat(), (size / 2 - 3).toFloat())
        // Walls & Base
        lineTo((size / 2 - 13).toFloat(), (size / 2 + 16).toFloat())
        lineTo((size / 2 + 13).toFloat(), (size / 2 + 16).toFloat())
        lineTo((size / 2 + 13).toFloat(), (size / 2 - 3).toFloat())
        lineTo((size / 2 + 18).toFloat(), (size / 2 - 3).toFloat())
        close()
    }
    canvas.drawPath(housePath, iconPaint)

    // Door cutout
    val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (hasVacantRooms) android.graphics.Color.parseColor("#16A34A") else android.graphics.Color.parseColor("#DC2626")
        style = Paint.Style.FILL
    }
    canvas.drawRect(
        (size / 2 - 4).toFloat(),
        (size / 2 + 5).toFloat(),
        (size / 2 + 4).toFloat(),
        (size / 2 + 16).toFloat(),
        doorPaint
    )

    return BitmapDrawable(context.resources, bitmap)
}

@Composable
fun SmartRentMapView(
    properties: List<Property>,
    rooms: List<RoomUnit>,
    onPropertySelected: (Property) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK) // Clean 2D street map only
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                val defaultCenter = if (properties.isNotEmpty()) {
                    GeoPoint(properties.first().latitude, properties.first().longitude)
                } else {
                    GeoPoint(23.8315, 91.2868)
                }
                controller.setCenter(defaultCenter)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            properties.forEach { property ->
                val propRooms = rooms.filter { it.propertyId == property.id }
                val hasVacant = propRooms.any { it.isVacant }

                val marker = Marker(mapView).apply {
                    position = GeoPoint(property.latitude, property.longitude)
                    title = property.name
                    snippet = if (hasVacant) "🟢 Rooms Available!" else "🔴 All Occupied"
                    icon = createHouseMarker(context, hasVacant, property.name)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onPropertySelected(property)
                        showInfoWindow()
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

