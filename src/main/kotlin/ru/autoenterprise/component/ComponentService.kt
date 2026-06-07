package ru.autoenterprise.component

import jakarta.persistence.EntityNotFoundException
import java.math.BigDecimal
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.ComponentStatus
import ru.autoenterprise.domain.VehicleComponentActionType
import ru.autoenterprise.repair.RepairEntity
import ru.autoenterprise.repair.RepairRepository
import ru.autoenterprise.vehicle.VehicleEntity
import ru.autoenterprise.vehicle.VehicleRepository

@Service
@Transactional(readOnly = true)
class ComponentService(
    private val componentRepository: ComponentRepository,
) {

    fun listComponents(page: Int): Page<ComponentRow> =
        componentRepository.findAllBy(pageable(page, "serialNumber")).map { component ->
            ComponentRow(
                id = component.id!!,
                componentType = component.componentType,
                serialNumber = component.serialNumber,
                model = component.model,
                manufacturer = component.manufacturer,
                status = component.status.name,
            )
        }

    fun componentOptions(): List<ComponentOption> =
        componentRepository.findAllByOrderBySerialNumberAsc().map { component ->
            ComponentOption(component.id!!, "${component.serialNumber} / ${component.componentType} / ${component.status.name}")
        }

    fun componentTypeOptions(): List<String> =
        componentRepository.findDistinctComponentTypes()

    fun getComponentForm(id: Long): ComponentForm {
        val component = componentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Агрегат не найден.") }

        return ComponentForm(
            id = component.id,
            componentType = component.componentType,
            serialNumber = component.serialNumber,
            model = component.model,
            manufacturer = component.manufacturer,
            productionDate = component.productionDate,
            purchaseDate = component.purchaseDate,
            status = component.status,
            notes = component.notes,
        )
    }

    @Transactional
    fun createComponent(form: ComponentForm) {
        val component = ComponentEntity(status = ComponentStatus.IN_STOCK)
        applyComponent(component, form)
        componentRepository.save(component)
    }

    @Transactional
    fun updateComponent(id: Long, form: ComponentForm) {
        val component = componentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Агрегат не найден.") }

        applyComponent(component, form)
    }

    @Transactional
    fun deleteComponent(id: Long) {
        componentRepository.deleteById(id)
    }

    private fun applyComponent(component: ComponentEntity, form: ComponentForm) {
        component.componentType = form.componentType.trim()
        component.serialNumber = form.serialNumber.trim()
        component.model = form.model?.trim().takeUnless { it.isNullOrBlank() }
        component.manufacturer = form.manufacturer?.trim().takeUnless { it.isNullOrBlank() }
        component.productionDate = form.productionDate
        component.purchaseDate = form.purchaseDate
        component.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }
}

@Service
@Transactional(readOnly = true)
class VehicleComponentHistoryService(
    private val vehicleComponentHistoryRepository: VehicleComponentHistoryRepository,
    private val vehicleRepository: VehicleRepository,
    private val componentRepository: ComponentRepository,
    private val repairRepository: RepairRepository,
) {

    fun listHistory(page: Int): Page<VehicleComponentHistoryRow> =
        vehicleComponentHistoryRepository.findAllBy(pageable(page, "actionDate").withSort(Sort.by("actionDate").descending())).map { history ->
            VehicleComponentHistoryRow(
                id = history.id!!,
                vehicleLabel = vehicleLabel(history.vehicle),
                componentLabel = "${history.component.serialNumber} / ${history.component.componentType}",
                repairLabel = history.repair?.let { "#${it.id}" },
                actionType = history.actionType.displayName,
                actionDate = history.actionDate,
                cost = history.cost,
            )
        }

    fun getHistoryForm(id: Long): VehicleComponentHistoryForm {
        val history = vehicleComponentHistoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("История агрегата не найдена.") }

        return VehicleComponentHistoryForm(
            id = history.id,
            vehicleId = history.vehicle.id,
            componentId = history.component.id,
            repairId = history.repair?.id,
            actionType = history.actionType,
            actionDate = history.actionDate,
            cost = history.cost,
            notes = history.notes,
        )
    }

    @Transactional
    fun createHistory(form: VehicleComponentHistoryForm) {
        val history = VehicleComponentHistoryEntity()
        applyHistory(history, form)
        vehicleComponentHistoryRepository.save(history)
    }

    @Transactional
    fun updateHistory(id: Long, form: VehicleComponentHistoryForm) {
        val history = vehicleComponentHistoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("История агрегата не найдена.") }

        applyHistory(history, form)
    }

    @Transactional
    fun deleteHistory(id: Long) {
        vehicleComponentHistoryRepository.deleteById(id)
    }

    companion object {
        val actionTypes: List<VehicleComponentActionType> = VehicleComponentActionType.entries
    }

    private fun applyHistory(history: VehicleComponentHistoryEntity, form: VehicleComponentHistoryForm) {
        history.vehicle = findVehicle(form.vehicleId)
        history.component = findComponent(form.componentId)
        history.repair = form.repairId?.let(::findRepair)
        history.actionType = form.actionType
        history.actionDate = form.actionDate ?: LocalDate.now()
        history.cost = form.cost ?: BigDecimal.ZERO
        history.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun findComponent(id: Long?): ComponentEntity {
        val componentId = id ?: throw EntityNotFoundException("Агрегат не найден.")
        return componentRepository.findById(componentId)
            .orElseThrow { EntityNotFoundException("Агрегат не найден.") }
    }

    private fun findRepair(id: Long): RepairEntity =
        repairRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Ремонт не найден.") }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())
