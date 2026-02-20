package com.yousign.phpstorm.ecs

import com.jetbrains.php.tools.quality.QualityToolAnnotator
import com.jetbrains.php.tools.quality.QualityToolValidationInspection

class EcsValidationInspection : QualityToolValidationInspection<EcsValidationInspection>() {

    override fun getAnnotator(): QualityToolAnnotator<EcsValidationInspection> = EcsAnnotator.INSTANCE

    override fun getToolName(): String = "ECS"

    override fun getShortName(): String = "EcsValidationInspection"
}
