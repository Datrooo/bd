package ru.autoenterprise.garage

import jakarta.persistence.EntityNotFoundException
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.autoenterprise.domain.GarageObjectType
import ru.autoenterprise.organization.SectionRepository
import ru.autoenterprise.organization.WorkshopRepository
import ru.autoenterprise.vehicle.VehicleEntity
import ru.autoenterprise.vehicle.VehicleRepository

@Service
@Transactional(readOnly = true)
class GarageObjectService(
    private val garageObjectRepository: GarageObjectRepository,
    private val vehicleLocationHistoryRepository: VehicleLocationHistoryRepository,
    private val workshopRepository: WorkshopRepository,
    private val sectionRepository: SectionRepository,
) {

    fun listGarageObjects(page: Int): Page<GarageObjectRow> =
        garageObjectRepository.findAllBy(pageable(page, "name")).map { garageObject ->
            GarageObjectRow(
                id = garageObject.id!!,
                name = garageObject.name,
                objectType = garageObject.objectType.name,
                parentName = garageObject.parentObject?.name,
                workshopName = garageObject.workshop?.name,
                sectionName = garageObject.section?.name,
                capacity = garageObject.capacity,
            )
        }

    fun garageObjectOptions(excludeId: Long? = null): List<GarageObjectOption> =
        garageObjectRepository.findAllByOrderByNameAsc()
            .asSequence()
            .filter { excludeId == null || it.id != excludeId }
            .map { GarageObjectOption(it.id!!, it.name) }
            .toList()

    fun getGarageObjectForm(id: Long): GarageObjectForm {
        val garageObject = garageObjectRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Объект гаражного хозяйства не найден.") }

        return GarageObjectForm(
            id = garageObject.id,
            name = garageObject.name,
            objectType = garageObject.objectType,
            parentObjectId = garageObject.parentObject?.id,
            workshopId = garageObject.workshop?.id,
            sectionId = garageObject.section?.id,
            address = garageObject.address,
            capacity = garageObject.capacity,
            description = garageObject.description,
        )
    }

    @Transactional
    fun createGarageObject(form: GarageObjectForm) {
        val garageObject = GarageObjectEntity()
        applyGarageObject(garageObject, form)
        garageObjectRepository.save(garageObject)
    }

    @Transactional
    fun updateGarageObject(id: Long, form: GarageObjectForm) {
        val garageObject = garageObjectRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Объект гаражного хозяйства не найден.") }

        applyGarageObject(garageObject, form.copy(id = id))
    }

    @Transactional
    fun deleteGarageObject(id: Long) {
        if (garageObjectRepository.existsByParentObjectId(id)) {
            throw IllegalStateException("Нельзя удалить объект, пока на него ссылаются дочерние объекты гаражного хозяйства.")
        }
        if (vehicleLocationHistoryRepository.existsByGarageObjectId(id)) {
            throw IllegalStateException("Нельзя удалить объект, пока по нему есть история размещения транспорта.")
        }
        garageObjectRepository.deleteById(id)
    }

    companion object {
        val objectTypes: List<GarageObjectType> = GarageObjectType.entries
    }

    private fun applyGarageObject(garageObject: GarageObjectEntity, form: GarageObjectForm) {
        garageObject.name = form.name.trim()
        garageObject.objectType = form.objectType
        val parentObject = form.parentObjectId?.let { parentId ->
            garageObjectRepository.findById(parentId)
                .orElseThrow { EntityNotFoundException("Родительский объект не найден.") }
        }
        if (form.id != null && parentObject != null) {
            ensureParentDoesNotCreateCycle(form.id, parentObject)
        }
        garageObject.parentObject = parentObject
        garageObject.workshop = form.workshopId?.let { workshopId ->
            workshopRepository.findById(workshopId)
                .orElseThrow { EntityNotFoundException("Цех не найден.") }
        }
        garageObject.section = form.sectionId?.let { sectionId ->
            sectionRepository.findById(sectionId)
                .orElseThrow { EntityNotFoundException("Участок не найден.") }
        }
        garageObject.address = form.address?.trim().takeUnless { it.isNullOrBlank() }
        garageObject.capacity = form.capacity
        garageObject.description = form.description?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun ensureParentDoesNotCreateCycle(objectId: Long, parentObject: GarageObjectEntity) {
        var currentId = parentObject.id ?: return
        val visitedIds = mutableSetOf<Long>()

        while (true) {
            if (currentId == objectId) {
                throw IllegalStateException("Объект не может быть родителем самого себя или своего предка.")
            }
            if (!visitedIds.add(currentId)) {
                throw IllegalStateException("В иерархии объектов гаражного хозяйства обнаружен цикл.")
            }

            val current = garageObjectRepository.findById(currentId)
                .orElseThrow { EntityNotFoundException("Родительский объект не найден.") }
            currentId = current.parentObject?.id ?: return
        }
    }
}

@Service
@Transactional(readOnly = true)
class VehicleLocationHistoryService(
    private val vehicleLocationHistoryRepository: VehicleLocationHistoryRepository,
    private val vehicleRepository: VehicleRepository,
    private val garageObjectRepository: GarageObjectRepository,
) {

    fun listLocations(page: Int): Page<VehicleLocationHistoryRow> =
        vehicleLocationHistoryRepository.findAllBy(pageable(page, "startDate").withSort(Sort.by("startDate").descending())).map { location ->
            VehicleLocationHistoryRow(
                id = location.id!!,
                vehicleLabel = vehicleLabel(location.vehicle),
                garageObjectName = location.garageObject.name,
                startDate = location.startDate,
                endDate = location.endDate,
            )
        }

    fun getLocationForm(id: Long): VehicleLocationHistoryForm {
        val location = vehicleLocationHistoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("История размещения транспорта не найдена.") }

        return VehicleLocationHistoryForm(
            id = location.id,
            vehicleId = location.vehicle.id,
            garageObjectId = location.garageObject.id,
            startDate = location.startDate,
            endDate = location.endDate,
            notes = location.notes,
        )
    }

    @Transactional
    fun createLocation(form: VehicleLocationHistoryForm) {
        val location = VehicleLocationHistoryEntity()
        applyLocation(location, form)
        vehicleLocationHistoryRepository.save(location)
    }

    @Transactional
    fun updateLocation(id: Long, form: VehicleLocationHistoryForm) {
        val location = vehicleLocationHistoryRepository.findById(id)
            .orElseThrow { EntityNotFoundException("История размещения транспорта не найдена.") }

        applyLocation(location, form)
    }

    @Transactional
    fun deleteLocation(id: Long) {
        vehicleLocationHistoryRepository.deleteById(id)
    }

    private fun applyLocation(location: VehicleLocationHistoryEntity, form: VehicleLocationHistoryForm) {
        location.vehicle = findVehicle(form.vehicleId)
        location.garageObject = findGarageObject(form.garageObjectId)
        location.startDate = form.startDate ?: LocalDate.now()
        location.endDate = form.endDate
        location.notes = form.notes?.trim().takeUnless { it.isNullOrBlank() }
    }

    private fun findVehicle(id: Long?): VehicleEntity {
        val vehicleId = id ?: throw EntityNotFoundException("Транспорт не найден.")
        return vehicleRepository.findById(vehicleId)
            .orElseThrow { EntityNotFoundException("Транспорт не найден.") }
    }

    private fun findGarageObject(id: Long?): GarageObjectEntity {
        val garageObjectId = id ?: throw EntityNotFoundException("Объект гаражного хозяйства не найден.")
        return garageObjectRepository.findById(garageObjectId)
            .orElseThrow { EntityNotFoundException("Объект гаражного хозяйства не найден.") }
    }

    private fun vehicleLabel(vehicle: VehicleEntity): String =
        "${vehicle.inventoryNumber} / ${vehicle.registrationNumber}"
}

private fun pageable(page: Int, sortProperty: String): PageRequest =
    PageRequest.of(page.coerceAtLeast(0), 10, Sort.by(sortProperty).ascending())
