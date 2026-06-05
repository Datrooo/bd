package ru.autoenterprise.web

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalViewModelAttributes(
    private val appStageProperties: AppStageProperties,
) {

    @ModelAttribute
    fun contribute(model: Model) {
        model.addAttribute("appStageCurrent", appStageProperties.current)
        model.addAttribute("appStageDescription", appStageProperties.description)
        model.addAttribute("appStageHeading", "${appStageProperties.current}: ${appStageProperties.description}")
    }
}
