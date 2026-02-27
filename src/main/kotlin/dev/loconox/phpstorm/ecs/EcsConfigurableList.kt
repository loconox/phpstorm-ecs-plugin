package dev.loconox.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.QualityToolConfigurableList
import com.jetbrains.php.tools.quality.QualityToolType

class EcsConfigurableList(project: Project) :
    QualityToolConfigurableList<EcsConfiguration>(project, EcsQualityToolType(), "Easy Coding Standard") {

    override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()
}
