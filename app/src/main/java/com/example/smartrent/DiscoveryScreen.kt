package com.example.smartrent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    var isMapView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedRoomForDetail by remember { mutableStateOf<Pair<RoomUnit, Property>?>(null) }
    var selectedPropertyForModal by remember { mutableStateOf<Property?>(null) }

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
                            "${vacantListings.size} verified units available",
                            fontSize = 12.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isMapView = !isMapView }) {
                        Icon(
                            imageVector = if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                            contentDescription = "Toggle View",
                            tint = BrandPrimary
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
            // Map Legend Status Bar
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Pins: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("🟢 Available Room", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🔴 All Occupied", fontSize = 11.sp, color = WarningRed, fontWeight = FontWeight.Bold)
                    }
                    Text(if (isMapView) "Map Mode" else "List Mode", fontSize = 11.sp, color = BrandPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            if (isMapView) {
                Box(modifier = Modifier.fillMaxSize()) {
                    SmartRentMapView(
                        properties = properties,
                        rooms = rooms,
                        onPropertySelected = { selectedPropertyForModal = it }
                    )
                }
            } else {
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
                            Text("Try switching to Map View or checking another area.", fontSize = 12.sp, color = Color.Gray)
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
    }

    // Modal when user taps a house pin on the map
    selectedPropertyForModal?.let { prop ->
        val propRooms = rooms.filter { it.propertyId == prop.id }
        val vacantRooms = propRooms.filter { it.isVacant }

        AlertDialog(
            onDismissRequest = { selectedPropertyForModal = null },
            title = {
                Column {
                    Text(prop.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("${prop.area}, ${prop.city}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (vacantRooms.isEmpty()) {
                        Text("🔴 All rooms currently occupied in this property.", color = WarningRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    } else {
                        Text("🟢 ${vacantRooms.size} unit(s) available:", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        vacantRooms.forEach { room ->
                            Text("• Unit ${room.roomNumber} (${room.roomType}) - ${formatCurrency(room.baseRent)}/mo", fontSize = 12.sp)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Owner: ${prop.ownerName} (${prop.ownerPhone})", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { selectedPropertyForModal = null }) {
                    Text("Close")
                }
            }
        )
    }

    selectedRoomForDetail?.let { (room, property) ->
        RoomDetailDialog(
            room = room,
            property = property,
            onDismiss = { selectedRoomForDetail = null }
        )
    }
}
