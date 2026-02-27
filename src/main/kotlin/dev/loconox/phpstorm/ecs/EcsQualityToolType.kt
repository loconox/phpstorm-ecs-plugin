package dev.loconox.phpstorm.ecs

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.*

class EcsQualityToolType : QualityToolType<EcsConfiguration>() {

    override fun getDisplayName(): String = "Easy Coding Standard"

    override fun getQualityToolBlackList(project: Project): QualityToolBlackList {
        return EcsBlackList.getInstance(project)
    }

    override fun getInspection(): QualityToolValidationInspection<*> {
        return EcsValidationInspection()
    }

    override fun createConfigurableForm(
        project: Project,
        configuration: EcsConfiguration
    ): QualityToolConfigurableForm<EcsConfiguration> {
        return EcsConfigurationForm(project, configuration)
    }

    override fun getToolConfigurable(project: Project): Configurable {
        return EcsConfigurableList(project)
    }

    override fun getProjectConfiguration(project: Project): QualityToolProjectConfiguration<EcsConfiguration> {
        return EcsProjectConfiguration.getInstance(project)
    }

    override fun createConfiguration(): EcsConfiguration {
        return EcsConfiguration()
    }

    override fun getConfigurationManager(project: Project): QualityToolConfigurationManager<EcsConfiguration> {
        return EcsConfigurationManager.getInstance(project)
    }

    override fun getConfigurationProvider(): QualityToolConfigurationProvider<EcsConfiguration> {
        return EcsConfigurationProvider
    }
}
