package com.yousign.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolConfiguration
import com.jetbrains.php.tools.quality.QualityToolType

class EcsConfigurationForm(
    project: Project,
    configuration: EcsConfiguration
) : QualityToolConfigurableForm<EcsConfiguration>(project, configuration, "ecs", "ecs") {

    @Suppress("UNCHECKED_CAST")
    override fun getQualityToolType(): QualityToolType<QualityToolConfiguration> {
        return EcsQualityToolType() as QualityToolType<QualityToolConfiguration>
    }

    override fun validateMessage(message: String): Pair<Boolean, String> {
        return Pair.create(true, "")
    }
}
