package com.yousign.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.QualityToolProjectConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolType

class EcsConfigurable(project: Project) : QualityToolProjectConfigurableForm(project) {

    override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()
}
