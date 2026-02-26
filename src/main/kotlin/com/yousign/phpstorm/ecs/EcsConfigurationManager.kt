package com.yousign.phpstorm.ecs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import com.jetbrains.php.tools.quality.QualityToolConfigurationBaseManager
import com.jetbrains.php.tools.quality.QualityToolConfigurationManager
import com.jetbrains.php.tools.quality.QualityToolType
import org.jdom.Element

class EcsConfigurationManager(private val project: Project) :
    QualityToolConfigurationManager<EcsConfiguration>(project) {

    init {
        myApplicationManager = ApplicationManager.getApplication().getService(EcsAppConfigurationBaseManager::class.java)
        myProjectManager = project.getService(EcsProjectConfigurationBaseManager::class.java)
    }

    override fun getLocalSettings(): EcsConfiguration {
        val settings = super.getLocalSettings()

        // Resolve relative/empty tool path to absolute so the QualityToolAnnotator framework can find the binary
        if (settings.toolPath.isNullOrBlank() || !java.io.File(settings.toolPath).isAbsolute) {
            val basePath = project.basePath
            if (basePath != null) {
                val candidate = if (settings.toolPath.isNullOrBlank())
                    java.io.File(basePath, EcsConfiguration.DEFAULT_TOOL_PATH)
                else
                    java.io.File(basePath, settings.toolPath)

                if (candidate.exists()) {
                    settings.setToolPath(candidate.absolutePath)
                }
            }
        }

        return settings
    }

    @State(name = "EcsAppConfiguration", storages = [Storage("ecs.xml")])
    class EcsAppConfigurationBaseManager : QualityToolConfigurationBaseManager<EcsConfiguration>() {
        override fun getOldStyleToolPathName(): String = "ecs"
        override fun getConfigurationRootName(): String = "ecs_settings"
        override fun loadLocal(element: Element): EcsConfiguration {
            return XmlSerializer.deserialize(element, EcsConfiguration::class.java)
        }
        override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()
    }

    @State(name = "EcsProjectConfiguration2", storages = [Storage("ecs.xml")])
    class EcsProjectConfigurationBaseManager : QualityToolConfigurationBaseManager<EcsConfiguration>() {
        override fun getOldStyleToolPathName(): String = "ecs"
        override fun getConfigurationRootName(): String = "ecs_settings"
        override fun loadLocal(element: Element): EcsConfiguration {
            return XmlSerializer.deserialize(element, EcsConfiguration::class.java)
        }
        override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()
    }

    companion object {
        fun getInstance(project: Project): EcsConfigurationManager {
            return project.getService(EcsConfigurationManager::class.java)
        }
    }
}
