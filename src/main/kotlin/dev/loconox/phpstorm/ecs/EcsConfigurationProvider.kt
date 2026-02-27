package dev.loconox.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import com.jetbrains.php.config.interpreters.PhpInterpreter
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolConfigurationProvider
import org.jdom.Element

object EcsConfigurationProvider : QualityToolConfigurationProvider<EcsConfiguration>() {

    override fun canLoad(tagName: String): Boolean = tagName == "ecs_settings"

    override fun load(element: Element): EcsConfiguration? {
        return XmlSerializer.deserialize(element, EcsConfiguration::class.java)
    }

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
        val config = EcsConfiguration()
        if (project != null) {
            resolveDefaultToolPath(project, config)
        }
        return config
    }

    override fun createConfigurationByInterpreter(interpreter: PhpInterpreter): EcsConfiguration {
        return EcsConfiguration()
    }

    private fun resolveDefaultToolPath(project: Project, config: EcsConfiguration) {
        val basePath = project.basePath ?: return
        val defaultBinary = java.io.File(basePath, EcsConfiguration.DEFAULT_TOOL_PATH)
        if (defaultBinary.exists()) {
            config.setToolPath(defaultBinary.absolutePath)
        }
        val defaultConfigFile = java.io.File(basePath, EcsConfiguration.DEFAULT_CONFIG_PATH)
        if (defaultConfigFile.exists()) {
            config.setEcsConfigPath(defaultConfigFile.absolutePath)
        }
    }
}
