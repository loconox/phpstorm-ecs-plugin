package com.yousign.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolConfigurationProvider
import org.jdom.Element

object EcsConfigurationProvider : QualityToolConfigurationProvider<EcsConfiguration>() {

    override fun canLoad(tagName: String): Boolean = tagName == "ecs_settings"

    override fun load(element: Element): EcsConfiguration? = null

    override fun createConfigurationForm(
        project: Project,
        configuration: EcsConfiguration
    ): QualityToolConfigurableForm<EcsConfiguration> {
        return EcsConfigurationForm(project, configuration)
    }

    override fun createNewInstance(
        project: Project?,
        existingConfigurations: MutableList<EcsConfiguration>
    ): EcsConfiguration {
        return EcsConfiguration()
    }

    override fun createConfigurationByInterpreter(interpreter: PhpInterpreter): EcsConfiguration {
        return EcsConfiguration()
    }
}
