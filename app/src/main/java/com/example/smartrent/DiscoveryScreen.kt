package com.example.smartrent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VacancyListingCard(
    room: RoomUnit,
    property: Property,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = property.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrandDarkNavy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = BrandSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${property.area}, ${property.city}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🟢 VACANT",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = Color(0xFFEEEEEE)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Unit ${room.roomNumber} • ${room.roomType}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "⭐ ${room.rating} (${room.reviewCount} reviews)",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(room.baseRent),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = BrandPrimary
                    )
                    Text("/ month", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(room.amenities) { tag ->
                    Surface(
                        color = BrandBackground,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val clean = property.ownerPhone.replace("+", "").replace(" ", "")
                        val text = "Hello ${property.ownerName}, I saw your listing for Unit ${room.roomNumber} (${room.roomType}) at ${property.name} on SmartRent and I'm interested in renting it."
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(text)}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.ownerPhone}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Owner", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RoomDetailDialog(
    room: RoomUnit,
    property: Property,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(property.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("Unit ${room.roomNumber} • ${room.roomType}", fontSize = 13.sp, color = BrandPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = room.description.ifBlank { "Clean, well-ventilated unit available for immediate move-in." },
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                Text("📍 Location: ${property.address}, ${property.area}, ${property.city}", fontSize = 12.sp)
                Text("⚡ Meter: ₹${room.electricityRate}/unit", fontSize = 12.sp)
                Text("👤 Owner: ${property.ownerName}", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Amenities:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(room.amenities.joinToString(" • "), fontSize = 12.sp, color = Color.DarkGray)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.ownerPhone}"))
                    context.startActivity(intent)
                }
            ) {
                Text("Call Owner")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
package com.example.smartrent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(viewModel: RentViewModel) {
    val properties by viewModel.properties.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedRoomForDetail by remember { mutableStateOf<Pair<RoomUnit, Property>?>(null) }

    val vacantListings = rooms.filter { it.isVacant }.mapNotNull { room ->
        val property = properties.find { it.id == room.propertyId }
        if (property != null) Pair(room, property) else null
    }.filter { (room, prop) ->
        val matchesSearch = prop.name.contains(searchQuery, ignoreCase = true) ||
                prop.area.contains(searchQuery, ignoreCase = true) ||
                prop.city.contains(searchQuery, ignoreCase = true)
        val matchesType = if (selectedFilter == "All") true else room.roomType.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesType
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Find Vacant Rooms", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            "${vacantListings.size} verified units available near you",
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandBackground)
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Area, Locality, or Building...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Single Room", "1BHK", "2BHK")
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            if (vacantListings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No vacant rooms match your filter.", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Try selecting 'All' or checking another locality.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(vacantListings) { (room, property) ->
                        VacancyListingCard(
                            room = room,
                            property = property,
                            onClick = { selectedRoomForDetail = Pair(room, property) }
                        )
                    }
                }
            }
        }
    }

    selectedRoomForDetail?.let { (room, property) ->
        RoomDetailDialog(
            room = room,
            property = property,
            onDismiss = { selectedRoomForDetail = null }
        )
    }
}

