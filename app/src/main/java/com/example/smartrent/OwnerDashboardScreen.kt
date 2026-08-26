package com.example.smartrent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(viewModel: RentViewModel) {
    val properties by viewModel.properties.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val tenants by viewModel.tenants.collectAsState()
    val bills by viewModel.bills.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var selectedPropertyId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(properties) {
        if (selectedPropertyId == null && properties.isNotEmpty()) {
            selectedPropertyId = properties.first().id
        }
    }

    val currentProperty = properties.find { it.id == selectedPropertyId }
    val currentRooms = rooms.filter { it.propertyId == selectedPropertyId }
    val currentTenants = tenants.filter { it.propertyId == selectedPropertyId }
    val currentBills = bills.filter { it.propertyId == selectedPropertyId }

    val totalUnits = currentRooms.size
    val vacantUnits = currentRooms.count { it.isVacant }
    val occupiedUnits = totalUnits - vacantUnits
    val pendingCollection = currentBills.filter { !it.isPaid }.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentProperty?.name ?: "Owner Portal",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${currentProperty?.area ?: ""}, ${currentProperty?.city ?: ""}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddPropertyDialog = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Add Property", tint = BrandPrimary)
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
            if (properties.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(properties) { prop ->
                        FilterChip(
                            selected = prop.id == selectedPropertyId,
                            onClick = { selectedPropertyId = prop.id },
                            label = { Text(prop.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Units", fontSize = 11.sp, color = Color.Gray)
                        Text("$occupiedUnits/$totalUnits Occ.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDarkNavy)
                        Text("$vacantUnits Vacant 🟢", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1.3f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pending Dues", fontSize = 11.sp, color = Color.Gray)
                        Text(formatCurrency(pendingCollection), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WarningRed)
                        Text("${currentBills.count { !it.isPaid }} unpaid bills", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BrandSurface,
                contentColor = BrandPrimary,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Rooms (${currentRooms.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Billing & Ledger") }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentRooms) { room ->
                                val tenant = currentTenants.find { it.roomId == room.id }
                                RoomItemCard(
                                    room = room,
                                    tenant = tenant,
                                    onToggleVacancy = { viewModel.toggleRoomVacancy(room.id) }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { showAddRoomDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add New Room / Unit")
                                }
                            }
                        }
                    }
                                        1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentBills) { bill ->
                                val room = currentRooms.find { it.id == bill.roomId }
                                val tenant = currentTenants.find { it.id == bill.tenantId }

                                BillItemCard(
                                    bill = bill,
                                    room = room,
                                    tenant = tenant,
                                    property = currentProperty,
                                    viewModel = viewModel
                                )
                            }

                            item {
                                Button(
                                    onClick = { showAddBillDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Calculate & Generate Month Bill")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { showAddPropertyDialog = false },
            onAdd = { n, addr, c, a, on, op ->
                viewModel.addProperty(n, addr, c, a, on, op)
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog && currentProperty != null) {
        AddRoomDialog(
            propertyName = currentProperty.name,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { no, type, rent, rate ->
                viewModel.addRoom(currentProperty.id, no, type, rent, rate)
                showAddRoomDialog = false
            }
        )
    }

    if (showAddBillDialog && currentTenants.isNotEmpty() && currentProperty != null) {
        AddBillDialog(
            tenants = currentTenants,
            rooms = currentRooms,
            onDismiss = { showAddBillDialog = false },
            onGenerate = { rId, tId, month, base, prev, cur, rate ->
                viewModel.generateBill(currentProperty.id, rId, tId, month, base, prev, cur, rate, 200.0)
                showAddBillDialog = false
            }
        )
    }
}
