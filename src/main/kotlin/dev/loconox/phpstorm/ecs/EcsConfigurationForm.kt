package dev.loconox.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.jetbrains.php.tools.quality.QualityToolConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolConfiguration
import com.jetbrains.php.tools.quality.QualityToolType
import org.jetbrains.annotations.NonNls

class EcsConfigurationForm(
    project: Project,
    configuration: EcsConfiguration
) : QualityToolConfigurableForm<EcsConfiguration>(project, configuration, "Easy Coding Standard", "ecs") {

    @Suppress("UNCHECKED_CAST")
    override fun getQualityToolType(): QualityToolType<QualityToolConfiguration> {
        return EcsQualityToolType() as QualityToolType<QualityToolConfiguration>
    }

    override fun validateMessage(message: String): Pair<Boolean, String> {
        return Pair.create(true, "")
    }

    override fun getHelpTopic(): @NonNls String {
        return "reference.settings.php.ecs"
    }
}
