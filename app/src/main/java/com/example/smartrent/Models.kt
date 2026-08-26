package com.example.smartrent

import java.util.UUID

// Core Property Definition
data class Property(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val city: String,
    val area: String,
    val ownerName: String,
    val ownerPhone: String
)

// Unit / Room Definition
data class RoomUnit(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomNumber: String,
    val roomType: String, // "Single Room", "1BHK", "2BHK", "PG"
    val baseRent: Double,
    val electricityRate: Double = 10.0,
    val isVacant: Boolean = true,
    val description: String = "",
    val amenities: List<String> = listOf("24/7 Water", "Wi-Fi", "Separate Meter"),
    val rating: Double = 4.8,
    val reviewCount: Int = 10
)

// Active Tenant Profile
data class Tenant(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomId: String,
    val name: String,
    val phone: String,
    val moveInDate: String,
    val securityDeposit: Double = 0.0,
    val initialMeterReading: Double = 0.0
)

// Monthly Ledger & Bill Statement
data class BillRecord(
    val id: String = UUID.randomUUID().toString(),
    val propertyId: String,
    val roomId: String,
    val tenantId: String,
    val monthYear: String, // e.g. "August 2026"
    val baseRent: Double,
    val prevMeterReading: Double,
    val currentMeterReading: Double,
    val electricityRate: Double,
    val maintenanceCharge: Double = 0.0,
    val isPaid: Boolean = false
) {
    val electricityUnitsUsed: Double
        get() = (currentMeterReading - prevMeterReading).coerceAtLeast(0.0)

    val electricityBill: Double
        get() = electricityUnitsUsed * electricityRate

    val totalAmount: Double
        get() = baseRent + electricityBill + maintenanceCharge
}

