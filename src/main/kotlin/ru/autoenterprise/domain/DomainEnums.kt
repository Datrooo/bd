package ru.autoenterprise.domain

enum class VehicleStatus {
    ACTIVE,
    IN_REPAIR,
    WRITTEN_OFF,
    SOLD,
    RESERVE,
}

enum class EmployeeStatus {
    ACTIVE,
    VACATION,
    SICK_LEAVE,
    DISMISSED,
}

enum class VehicleAcquisitionType {
    PURCHASE,
    TRANSFER,
    LEASE,
}

enum class VehicleDisposalType {
    WRITE_OFF,
    SALE,
    TRANSFER,
}

enum class GarageObjectType {
    GARAGE,
    BOX,
    REPAIR_BUILDING,
    PARKING,
    WAREHOUSE,
}

enum class RouteType {
    BUS,
    MINIBUS,
}

enum class TransportationRecordType {
    PASSENGER,
    CARGO,
    SERVICE,
}

enum class VehicleDriverAssignmentType {
    PRIMARY,
    RESERVE,
    SHIFT,
}

enum class RepairStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
}

enum class ComponentStatus {
    IN_STOCK,
    INSTALLED,
    UNDER_REPAIR,
    WRITTEN_OFF,
}

enum class VehicleComponentActionType(val displayName: String) {
    INSTALLED("Установка"),
    REMOVED("Снятие"),
    REPLACED("Замена"),
    REPAIRED("Ремонт"),
}
