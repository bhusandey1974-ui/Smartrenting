package com.example.smartrent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RentViewModel : ViewModel() {

    private val _properties = MutableStateFlow<List<Property>>(emptyList())
    val properties: StateFlow<List<Property>> = _properties.asStateFlow()

    private val _rooms = MutableStateFlow<List<RoomUnit>>(emptyList())
    val rooms: StateFlow<List<RoomUnit>> = _rooms.asStateFlow()

    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants.asStateFlow()

    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    init {
        loadInitialDemoData()
    }

    private fun loadInitialDemoData() {
        val prop1 = Property(
            id = "PROP-1",
            name = "Green Villa Residency",
            address = "House #24, Near City Center",
            city = "Agartala",
            area = "Banamalipur",
            ownerName = "Rajesh Sharma",
            ownerPhone = "9876543210"
        )
        val prop2 = Property(
            id = "PROP-2",
            name = "Skyline Apartments",
            address = "Plot #8B, Road No 5",
            city = "Agartala",
            area = "Ramnagar",
            ownerName = "Amit Das",
            ownerPhone = "9812345678"
        )

        _properties.value = listOf(prop1, prop2)

        val room1 = RoomUnit(
            id = "ROOM-1",
            propertyId = prop1.id,
            roomNumber = "101",
            roomType = "1BHK",
            baseRent = 5500.0,
            electricityRate = 10.0,
            isVacant = false,
            description = "Spacious sunny room with attached balcony and modern tiled bathroom.",
            amenities = listOf("Wi-Fi", "RO Water", "24/7 Water", "Attached Balcony"),
            rating = 4.8,
            reviewCount = 12
        )
        val room2 = RoomUnit(
            id = "ROOM-2",
            propertyId = prop1.id,
            roomNumber = "102",
            roomType = "Single Room",
            baseRent = 3500.0,
            electricityRate = 10.0,
            isVacant = true,
            description = "Cozy single room ideal for students or working professionals.",
            amenities = listOf("Wi-Fi", "RO Water"),
            rating = 4.5,
            reviewCount = 5
        )
        val room3 = RoomUnit(
            id = "ROOM-3",
            propertyId = prop2.id,
            roomNumber = "201",
            roomType = "2BHK",
            baseRent = 8500.0,
            electricityRate = 12.0,
            isVacant = false,
            description = "Full 2BHK flat with modular kitchen and reserved parking.",
            amenities = listOf("AC", "Wi-Fi", "RO Water", "Bike Parking"),
            rating = 4.9,
            reviewCount = 18
        )

        _rooms.value = listOf(room1, room2, room3)

        val tenant1 = Tenant(
            id = "TENANT-1",
            propertyId = prop1.id,
            roomId = room1.id,
            name = "Rahul Sen",
            phone = "9436100001",
            moveInDate = "01 Jan 2026",
            securityDeposit = 10000.0,
            initialMeterReading = 420.0
        )
        val tenant2 = Tenant(
            id = "TENANT-2",
            propertyId = prop2.id,
            roomId = room3.id,
            name = "Priya Roy",
            phone = "9436100002",
            moveInDate = "15 Feb 2026",
            securityDeposit = 15000.0,
            initialMeterReading = 1050.0
        )

        _tenants.value = listOf(tenant1, tenant2)

        val bill1 = BillRecord(
            id = "BILL-1",
            propertyId = prop1.id,
            roomId = room1.id,
            tenantId = tenant1.id,
            monthYear = "August 2026",
            baseRent = 5500.0,
            prevMeterReading = 420.0,
            currentMeterReading = 495.0,
            electricityRate = 10.0,
            maintenanceCharge = 200.0,
            isPaid = false
        )

        _bills.value = listOf(bill1)
    }

    fun addProperty(name: String, address: String, city: String, area: String, ownerName: String, ownerPhone: String) {
        val newProp = Property(
            name = name,
            address = address,
            city = city,
            area = area,
            ownerName = ownerName,
            ownerPhone = ownerPhone
        )
        _properties.update { it + newProp }
    }

    fun addRoom(propertyId: String, roomNumber: String, roomType: String, baseRent: Double, rate: Double) {
        val newRoom = RoomUnit(
            propertyId = propertyId,
            roomNumber = roomNumber,
            roomType = roomType,
            baseRent = baseRent,
            electricityRate = rate,
            isVacant = true
        )
        _rooms.update { it + newRoom }
    }

    fun toggleRoomVacancy(roomId: String) {
        _rooms.update { list ->
            list.map { if (it.id == roomId) it.copy(isVacant = !it.isVacant) else it }
        }
    }

    fun assignTenant(propertyId: String, roomId: String, name: String, phone: String, deposit: Double, meterReading: Double) {
        val newTenant = Tenant(
            propertyId = propertyId,
            roomId = roomId,
            name = name,
            phone = phone,
            moveInDate = "Today",
            securityDeposit = deposit,
            initialMeterReading = meterReading
        )
        _tenants.update { it + newTenant }
        _rooms.update { list ->
            list.map { if (it.id == roomId) it.copy(isVacant = false) else it }
        }
    }

    fun generateBill(propertyId: String, roomId: String, tenantId: String, month: String, baseRent: Double, prevUnit: Double, curUnit: Double, rate: Double, maintenance: Double) {
        val newBill = BillRecord(
            propertyId = propertyId,
            roomId = roomId,
            tenantId = tenantId,
            monthYear = month,
            baseRent = baseRent,
            prevMeterReading = prevUnit,
            currentMeterReading = curUnit,
            electricityRate = rate,
            maintenanceCharge = maintenance,
            isPaid = false
        )
        _bills.update { it + newBill }
    }

    fun markBillPaid(billId: String) {
        _bills.update { list ->
            list.map { if (it.id == billId) it.copy(isPaid = true) else it }
        }
    }

    fun getWhatsAppReceiptText(bill: BillRecord, tenant: Tenant, property: Property, room: RoomUnit): String {
        return """
            🧾 *RENT INVOICE - SMARTRENT*
            --------------------------------
            🏠 *Property:* ${property.name}
            🚪 *Room:* ${room.roomNumber} (${room.roomType})
            👤 *Tenant:* ${tenant.name}
            📅 *Month:* ${bill.monthYear}
            --------------------------------
            💵 Base Rent: ₹${bill.baseRent}
            ⚡ Units Used: ${bill.electricityUnitsUsed} (${bill.prevMeterReading} -> ${bill.currentMeterReading})
            ⚡ Electricity Charges: ₹${bill.electricityBill} (@ ₹${bill.electricityRate}/unit)
            🛠️ Maintenance: ₹${bill.maintenanceCharge}
            --------------------------------
            💰 *TOTAL PAYABLE: ₹${bill.totalAmount}*
            Status: ${if (bill.isPaid) "PAID ✅" else "PENDING ⏳"}
            
            _Generated via SmartRent App_
        """.trimIndent()
    }
}

