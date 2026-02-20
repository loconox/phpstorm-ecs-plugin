package com.yousign.phpstorm.ecs

import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager

class EcsConfigurationManager(project: Project) :
    QualityToolConfigurationManager<EcsConfiguration>(project) {

    companion object {
        fun getInstance(project: Project): EcsConfigurationManager {
            return project.getService(EcsConfigurationManager::class.java)
        }
    }
}
