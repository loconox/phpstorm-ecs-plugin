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
        return mutableListOf("check", "--output-format=json")
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
