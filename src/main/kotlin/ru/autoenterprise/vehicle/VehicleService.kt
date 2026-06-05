package ru.autoenterprise.vehicle

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.VehicleStatus

@Service
class VehicleCategoryService(
    private val categoryRepository: VehicleCategoryRepository,
) {

    fun listCategories(): List<VehicleCategoryRow> =
        categoryRepository.findAllByOrderByNameAsc().map { category ->
            VehicleCategoryRow(
                id = category.id!!,
                name = category.name,
                description = category.description,
            )
        }

    fun getCategoryForm(id: Long): VehicleCategoryForm {
        val category = categoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Категория транспорта не найдена.") }

        return VehicleCategoryForm(
            id = category.id,
            name = category.name,
            description = category.description,
        )
    }

    @Transactional
    fun createCategory(form: VehicleCategoryForm) {
        val category = VehicleCategoryEntity()
        applyCategory(category, form)
        categoryRepository.save(category)
    }

    @Transactional
    fun updateCategory(id: Long, form: VehicleCategoryForm) {
        val category = categoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Категория транспорта не найдена.") }

        applyCategory(category, form)
    }

    @Transactional
    fun deleteCategory(id: Long) {
        categoryRepository.deleteById(id)
    }

    fun categoryOptions(): List<VehicleCategoryOption> =
        categoryRepository.findAllByOrderByNameAsc().map { VehicleCategoryOption(it.id!!, it.name) }

    private fun applyCategory(category: VehicleCategoryEntity, form: VehicleCategoryForm) {
        category.name = form.name.trim()
        category.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }
}

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val categoryRepository: VehicleCategoryRepository,
) {

    fun listVehicles(page: Int): Page<VehicleRow> =
        vehicleRepository.findAllBy(pageable(page)).map { vehicle ->
            VehicleRow(
                id = vehicle.id!!,
                inventoryNumber = vehicle.inventoryNumber,
                registrationNumber = vehicle.registrationNumber,
                categoryName = vehicle.category.name,
                brandName = vehicle.brandName,
                modelName = vehicle.modelName,
                status = vehicle.status.name,
                currentMileage = vehicle.currentMileage,
            )
        }

    fun getVehicleForm(id: Long): VehicleForm {
        val vehicle = vehicleRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }

        return VehicleForm(
            id = vehicle.id,
            inventoryNumber = vehicle.inventoryNumber,
            registrationNumber = vehicle.registrationNumber,
            vin = vehicle.vin,
            categoryId = vehicle.category.id,
            brandName = vehicle.brandName,
            modelName = vehicle.modelName,
            manufactureYear = vehicle.manufactureYear,
            purchaseDate = vehicle.purchaseDate,
            commissioningDate = vehicle.commissioningDate,
            currentMileage = vehicle.currentMileage,
            status = vehicle.status,
            passengerCapacity = vehicle.passengerCapacity,
            loadCapacityKg = vehicle.loadCapacityKg,
            cargoVolumeM3 = vehicle.cargoVolumeM3,
            bodyType = vehicle.bodyType,
            taxiLicenseNumber = vehicle.taxiLicenseNumber,
            servicePurpose = vehicle.servicePurpose,
            color = vehicle.color,
            engineNumber = vehicle.engineNumber,
            chassisNumber = vehicle.chassisNumber,
            notes = vehicle.notes,
        )
    }

    fun vehicleOptions(): List<VehicleReferenceOption> =
        vehicleRepository.findAll(Sort.by("inventoryNumber").ascending()).map { vehicle ->
            VehicleReferenceOption(
                id = vehicle.id!!,
                label = "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}",
            )
        }

    fun brandOptions(): List<String> =
        vehicleRepository.findDistinctBrandNames()

    @Transactional
    fun createVehicle(form: VehicleForm) {
        val vehicle = VehicleEntity()
        applyVehicle(vehicle, form)
        vehicleRepository.save(vehicle)
    }

    @Transactional
    fun updateVehicle(id: Long, form: VehicleForm) {
        val vehicle = vehicleRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }

        applyVehicle(vehicle, form)
    }

    @Transactional
    fun deleteVehicle(id: Long) {
        vehicleRepository.deleteById(id)
    }

    companion object {
        val vehicleStatuses: List<VehicleStatus> = VehicleStatus.entries

        private const val pageSize = 10

        private fun pageable(page: Int): PageRequest =
            PageRequest.of(page.coerceAtLeast(0), pageSize, Sort.by("inventoryNumber").ascending())
    }

    private fun applyVehicle(vehicle: VehicleEntity, form: VehicleForm) {
        val category = categoryRepository.findById(form.categoryId!!)
            .orElseThrow { EntityNotFoundException("Категория транспорта не найдена.") }

        vehicle.inventoryNumber = form.inventoryNumber.trim()
        vehicle.registrationNumber = form.registrationNumber.trim()
        vehicle.vin = form.vin?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.category = category
        vehicle.brandName = form.brandName.trim()
        vehicle.modelName = form.modelName.trim()
        vehicle.manufactureYear = form.manufactureYear
        vehicle.purchaseDate = form.purchaseDate
        vehicle.commissioningDate = form.commissioningDate
        vehicle.currentMileage = form.currentMileage!!
        vehicle.status = form.status
        vehicle.passengerCapacity = form.passengerCapacity
        vehicle.loadCapacityKg = form.loadCapacityKg
        vehicle.cargoVolumeM3 = form.cargoVolumeM3
        vehicle.bodyType = form.bodyType?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.taxiLicenseNumber = form.taxiLicenseNumber?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.servicePurpose = form.servicePurpose?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.color = form.color?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.engineNumber = form.engineNumber?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.chassisNumber = form.chassisNumber?.trim().takeUnless { it.isNullOrBlank() }
        vehicle.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }
}
