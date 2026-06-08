package ru.autoenterprise.report

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class ReportCatalog(
    private val resourceLoader: ResourceLoader,
) {

    private val definitions: List<ReportDefinition> = listOf(
        definition("r01", "Данные об автопарке предприятия", "Полный перечень транспорта с категориями и эксплуатационными атрибутами.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r02", "Перечень водителей по предприятию", "Список сотрудников с должностью DRIVER.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r03", "Общее число водителей", "Сводный количественный показатель по водителям.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r04", "Водители по указанной автомашине", "История закрепления водителей за выбранной машиной.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.vehicle())),
        definition("r05", "Число водителей по указанной автомашине", "Количество назначений водителей для выбранной машины.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.vehicle())),
        definition("r06", "Распределение водителей по автомобилям", "Срез закрепления водителей по всему автопарку.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r07", "Распределение пассажирского транспорта по маршрутам", "Назначения транспорта на маршруты с деталями смен.", AUTOPARK_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r08", "Пробег транспорта определенной категории за период", "Суммарный пробег транспорта выбранной категории за период.", EXPLOITATION_GROUP, listOf(ReportParameterDefinition.category(), ReportParameterDefinition.dateRange())),
        definition("r09", "Пробег конкретной автомашины за период", "Суммарный пробег выбранной машины за период.", EXPLOITATION_GROUP, listOf(ReportParameterDefinition.vehicle(), ReportParameterDefinition.dateRange())),
        definition("r10", "Число ремонтов и их стоимость для категории транспорта за период", "Количество ремонтов и общая стоимость по категории транспорта.", REPAIRS_GROUP, listOf(ReportParameterDefinition.category(), ReportParameterDefinition.dateRange())),
        definition("r11", "Число ремонтов и их стоимость для марки транспорта за период", "Количество ремонтов и общая стоимость по марке транспорта.", REPAIRS_GROUP, listOf(ReportParameterDefinition.brand(), ReportParameterDefinition.dateRange())),
        definition("r12", "Число ремонтов и их стоимость для конкретной автомашины за период", "Количество ремонтов и общая стоимость по выбранной машине.", REPAIRS_GROUP, listOf(ReportParameterDefinition.vehicle(), ReportParameterDefinition.dateRange())),
        definition("r13", "Подчиненность персонала", "Текущий состав бригад с непосредственным руководителем и дальнейшей цепочкой подчиненности.", PERSONNEL_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r14", "Наличие гаражного хозяйства в целом", "Список объектов гаражного хозяйства предприятия.", GARAGE_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r15", "Наличие гаражного хозяйства по каждой категории транспорта", "Текущее размещение транспорта по объектам гаражного хозяйства и категориям.", GARAGE_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r16", "Распределение автотранспорта на предприятии", "Текущее размещение транспорта по объектам предприятия.", GARAGE_GROUP, listOf(ReportParameterDefinition.none())),
        definition("r17", "Грузоперевозки, выполненные указанной автомашиной за период", "Детализация грузовых перевозок по выбранной машине.", EXPLOITATION_GROUP, listOf(ReportParameterDefinition.vehicle(), ReportParameterDefinition.dateRange())),
        definition("r18", "Число использованных для ремонта агрегатов для категории транспорта за период", "Установленные при ремонте агрегаты выбранного типа по категории транспорта.", REPAIRS_GROUP, listOf(ReportParameterDefinition.category(), ReportParameterDefinition.componentType(), ReportParameterDefinition.dateRange())),
        definition("r19", "Число использованных для ремонта агрегатов для марки транспорта за период", "Установленные при ремонте агрегаты выбранного типа по марке транспорта.", REPAIRS_GROUP, listOf(ReportParameterDefinition.brand(), ReportParameterDefinition.componentType(), ReportParameterDefinition.dateRange())),
        definition("r20", "Число использованных для ремонта агрегатов для конкретной машины за период", "Установленные при ремонте агрегаты выбранного типа по выбранной машине.", REPAIRS_GROUP, listOf(ReportParameterDefinition.vehicle(), ReportParameterDefinition.componentType(), ReportParameterDefinition.dateRange())),
        definition("r21", "Полученная техника за период", "Поступление техники за выбранный период.", ACQUISITION_GROUP, listOf(ReportParameterDefinition.dateRange())),
        definition("r22", "Списанная техника за период", "Выбытие техники за выбранный период.", ACQUISITION_GROUP, listOf(ReportParameterDefinition.dateRange())),
        definition("r23", "Состав подчиненных указанного бригадира", "Сотрудники, закрепленные за бригадой выбранного бригадира.", PERSONNEL_GROUP, listOf(ReportParameterDefinition.employee())),
        definition("r24", "Состав подчиненных указанного мастера", "Сотрудники, закрепленные за участками выбранного мастера.", PERSONNEL_GROUP, listOf(ReportParameterDefinition.employee())),
        definition("r25", "Состав подчиненных указанного начальника цеха", "Сотрудники в цехах выбранного начальника.", PERSONNEL_GROUP, listOf(ReportParameterDefinition.employee())),
        definition("r26", "Работы, выполненные указанным специалистом за период в целом", "Ремонтные работы выбранного специалиста за период.", REPAIRS_GROUP, listOf(ReportParameterDefinition.employee(), ReportParameterDefinition.dateRange())),
        definition("r27", "Работы, выполненные указанным специалистом за период по конкретной машине", "Ремонтные работы выбранного специалиста по конкретной машине.", REPAIRS_GROUP, listOf(ReportParameterDefinition.employee(), ReportParameterDefinition.vehicle(), ReportParameterDefinition.dateRange())),
        definition("r28", "Суммарная работа бригад по ремонту", "Сводка по ремонтным бригадам и стоимости работ.", REPAIRS_GROUP, listOf(ReportParameterDefinition.none())),
    )

    private val groupOrder = listOf(
        AUTOPARK_GROUP,
        EXPLOITATION_GROUP,
        REPAIRS_GROUP,
        PERSONNEL_GROUP,
        GARAGE_GROUP,
        ACQUISITION_GROUP,
    )

    init {
        validateDefinitions()
    }

    fun all(): List<ReportDefinition> = definitions

    fun find(code: String): ReportDefinition? =
        definitions.firstOrNull { definition -> definition.code == code.lowercase() }

    fun groups(): List<ReportCatalogGroup> {
        val grouped = definitions.groupBy { definition -> definition.groupTitle }
        return groupOrder.mapNotNull { title ->
            grouped[title]?.let { reports -> ReportCatalogGroup(title, reports) }
        }
    }

    private fun definition(
        code: String,
        title: String,
        description: String,
        groupTitle: String,
        parameters: List<ReportParameterDefinition>,
    ): ReportDefinition =
        ReportDefinition(
            code = code,
            title = title,
            description = description,
            groupTitle = groupTitle,
            sqlResource = "sql/reports/$code.sql",
            parameters = parameters,
        )

    private fun validateDefinitions() {
        require(definitions.size == 28) { "Ожидалось 28 отчетов, найдено ${definitions.size}." }

        val expectedCodes = (1..28).map { index -> "r%02d".format(index) }
        require(definitions.map { definition -> definition.code } == expectedCodes) {
            "Коды отчетов должны быть последовательностью r01..r28."
        }

        require(definitions.map { definition -> definition.code }.toSet().size == definitions.size) {
            "Коды отчетов должны быть уникальными."
        }

        definitions.forEach { definition ->
            require(resourceLoader.getResource("classpath:${definition.sqlResource}").exists()) {
                "Не найден SQL-ресурс для отчета ${definition.code}: ${definition.sqlResource}"
            }
        }
    }

    companion object {
        private const val AUTOPARK_GROUP = "Автопарк и водители"
        private const val EXPLOITATION_GROUP = "Эксплуатация и пробег"
        private const val REPAIRS_GROUP = "Ремонты и агрегаты"
        private const val PERSONNEL_GROUP = "Персонал и подчиненность"
        private const val GARAGE_GROUP = "Гаражное хозяйство"
        private const val ACQUISITION_GROUP = "Поступление и выбытие техники"
    }
}
