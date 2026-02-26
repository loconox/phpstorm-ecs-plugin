package com.yousign.phpstorm.ecs

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
        val options = mutableListOf("check", "--output-format=json", "--no-progress-bar")

        val configPath = getEcsConfigPath(project)
        if (configPath.isNotEmpty()) {
            options.add("--config=$configPath")
        }

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
        val INSTANCE = EcsAnnotator()
    }
}
