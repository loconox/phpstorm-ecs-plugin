package dev.loconox.phpstorm.ecs

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.codeInspection.InspectionProfile
import com.jetbrains.php.tools.quality.QualityToolAnnotator
import com.jetbrains.php.tools.quality.QualityToolAnnotatorInfo
import com.jetbrains.php.tools.quality.QualityToolMessageProcessor
import com.jetbrains.php.tools.quality.QualityToolType

class EcsAnnotator : QualityToolAnnotator<EcsValidationInspection>() {

    override fun getQualityToolType(): QualityToolType<*> = EcsQualityToolType()

    override fun getOptions(
        filePath: String?,
        inspection: EcsValidationInspection,
        profile: InspectionProfile?,
        project: Project
    ): MutableList<String> {
        if (!EcsConfigValidator.validate(project)) {
            LOG.warn("ECS annotator: skipping check for file $filePath — configuration is invalid")
            return mutableListOf()
        }

        val config = EcsConfigurationManager.getInstance(project).localSettings
        val configPath = getEcsConfigPath(project)

        val options = mutableListOf("check", "--output-format=json", "--no-progress-bar", "-n")
        if (configPath.isNotEmpty()) {
            options.add("--config=$configPath")
        }
        if (!filePath.isNullOrBlank()) {
            options.add(filePath)
        }

        LOG.info("ECS annotator: executing check for file: $filePath")
        LOG.info("ECS annotator: tool path: ${config.toolPath}, timeout: ${config.timeout}ms, options: $options")

        return options
    }

    private fun getEcsConfigPath(project: Project): String {
        val config = EcsConfigurationManager.getInstance(project).localSettings
        val configPath = config.getEcsConfigPath()
        if (configPath.isNotEmpty()) {
            return configPath
        }
        val basePath = project.basePath ?: return ""
        val defaultConfig = java.io.File(basePath, EcsConfiguration.DEFAULT_CONFIG_PATH)
        return if (defaultConfig.exists()) defaultConfig.absolutePath else ""
    }

    override fun createMessageProcessor(
        info: QualityToolAnnotatorInfo<EcsValidationInspection>
    ): QualityToolMessageProcessor {
        return EcsMessageProcessor(info)
    }

    companion object {
        private val LOG = Logger.getInstance(EcsAnnotator::class.java)
        val INSTANCE = EcsAnnotator()
    }
}
