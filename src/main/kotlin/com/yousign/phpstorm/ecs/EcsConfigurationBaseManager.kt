package com.yousign.phpstorm.ecs

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.QualityToolConfigurationBaseManager
import com.jetbrains.php.tools.quality.QualityToolType
import org.jdom.Element

@State(name = "EcsConfiguration", storages = [Storage("ecs.xml")])
class EcsConfigurationBaseManager : QualityToolConfigurationBaseManager<EcsConfiguration>() {

    override fun getOldStyleToolPathName(): String = "ecs"

    override fun getConfigurationRootName(): String = "ecs_settings"

    override fun loadLocal(element: Element): EcsConfiguration? = null

    override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()

    companion object {
        fun getInstance(project: Project): EcsConfigurationBaseManager {
            return project.getService(EcsConfigurationBaseManager::class.java)
        }
    }
}
