package ru.autoenterprise

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AutoEnterpriseApplication

fun main(args: Array<String>) {
    runApplication<AutoEnterpriseApplication>(*args)
}
