package com.example.smartrent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            onAdd = { n: String, addr: String, c: String, a: String, on: String, op: String ->
                viewModel.addProperty(n, addr, c, a, on, op)
                showAddPropertyDialog = false
            }
        )
    }

    if (showAddRoomDialog && currentProperty != null) {
        AddRoomDialog(
            propertyName = currentProperty.name,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { no: String, type: String, rent: Double, rate: Double ->
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
            onGenerate = { rId: String, tId: String, month: String, base: Double, prev: Double, cur: Double, rate: Double ->
                viewModel.generateBill(currentProperty.id, rId, tId, month, base, prev, cur, rate, 200.0)
                showAddBillDialog = false
            }
        )
    }
}
@Composable
fun RoomItemCard(
    room: RoomUnit,
    tenant: Tenant?,
    onToggleVacancy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (room.isVacant) SuccessGreen else WarningRed,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Room ${room.roomNumber} (${room.roomType})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BrandDarkNavy
                    )
                }

                Text(
                    text = "${formatCurrency(room.baseRent)}/mo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = BrandPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tenant != null) {
                Text(
                    text = "Tenant: ${tenant.name} (${tenant.phone})",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Moved In: ${tenant.moveInDate} • Deposit: ${formatCurrency(tenant.securityDeposit)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Status: Vacant & Listed on Discovery Map",
                    fontSize = 12.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onToggleVacancy,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (room.isVacant) "Mark as Occupied" else "Broadcast as Vacant",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BillItemCard(
    bill: BillRecord,
    room: RoomUnit?,
    tenant: Tenant?,
    property: Property?,
    viewModel: RentViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${tenant?.name ?: "Tenant"} (Room ${room?.roomNumber ?: ""})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(bill.monthYear, fontSize = 11.sp, color = Color.Gray)
                }

                Surface(
                    color = if (bill.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (bill.isPaid) "PAID ✅" else "PENDING ⏳",
                        color = if (bill.isPaid) SuccessGreen else WarningRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Base Rent: ${formatCurrency(bill.baseRent)}", fontSize = 12.sp)
                    Text("Electricity (${bill.electricityUnitsUsed.toInt()} units): ${formatCurrency(bill.electricityBill)}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Due", fontSize = 11.sp, color = Color.Gray)
                    Text(formatCurrency(bill.totalAmount), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = BrandPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (tenant != null && property != null && room != null) {
                            val msg = viewModel.getWhatsAppReceiptText(bill, tenant, property, room)
                            val cleanNum = tenant.phone.replace("+", "").replace(" ", "")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode(msg)}"))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send Bill", fontSize = 11.sp)
                }

                if (!bill.isPaid) {
                    OutlinedButton(
                        onClick = { viewModel.markBillPaid(bill.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Paid", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Property Name") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Street Address") }, singleLine = true)
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Locality") }, singleLine = true)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true)
                OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, singleLine = true)
                OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Owner Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && city.isNotBlank()) {
                        onAdd(name, address, city, area, ownerName, ownerPhone)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddRoomDialog(
    propertyName: String,
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, Double) -> Unit
) {
    var roomNo by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("1BHK") }
    var rent by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room to $propertyName", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = roomNo, onValueChange = { roomNo = it }, label = { Text("Room / Flat Number (e.g. 101)") }, singleLine = true)
                OutlinedTextField(value = roomType, onValueChange = { roomType = it }, label = { Text("Type (1BHK, 2BHK, Single)") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Monthly Base Rent (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Electricity Rate/Unit (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rentAmount = rent.toDoubleOrNull() ?: 0.0
                    val elecRate = rate.toDoubleOrNull() ?: 10.0
                    if (roomNo.isNotBlank() && rentAmount > 0) {
                        onAdd(roomNo, roomType, rentAmount, elecRate)
                    }
                }
            ) { Text("Add Room") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddBillDialog(
    tenants: List<Tenant>,
    rooms: List<RoomUnit>,
    onDismiss: () -> Unit,
    onGenerate: (String, String, String, Double, Double, Double, Double) -> Unit
) {
    var selectedTenant by remember { mutableStateOf(tenants.first()) }
    var prevUnits by remember { mutableStateOf(selectedTenant.initialMeterReading.toString()) }
    var curUnits by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("August 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Bill & Calculation", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tenant: ${selectedTenant.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Billing Month") }, singleLine = true)
                OutlinedTextField(value = prevUnits, onValueChange = { prevUnits = it }, label = { Text("Previous Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                OutlinedTextField(value = curUnits, onValueChange = { curUnits = it }, label = { Text("Current Meter Reading") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prev = prevUnits.toDoubleOrNull() ?: 0.0
                    val cur = curUnits.toDoubleOrNull() ?: prev
                    val room = rooms.find { it.id == selectedTenant.roomId }
                    if (room != null) {
                        onGenerate(room.id, selectedTenant.id, month, room.baseRent, prev, cur, room.electricityRate)
                    }
                }
            ) { Text("Generate") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

