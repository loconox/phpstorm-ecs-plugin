package dev.loconox.phpstorm.ecs

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.jetbrains.php.tools.quality.QualityToolProjectConfiguration
import com.jetbrains.php.tools.quality.QualityToolType

@Service(Service.Level.PROJECT)
@State(name = "EcsProjectConfiguration", storages = [Storage("ecs_project.xml")])
class EcsProjectConfiguration :
    QualityToolProjectConfiguration<EcsConfiguration>(),
    PersistentStateComponent<EcsProjectConfiguration> {

    override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()

    override fun getState(): EcsProjectConfiguration = this

    override fun loadState(state: EcsProjectConfiguration) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): EcsProjectConfiguration {
            return project.getService(EcsProjectConfiguration::class.java)
        }
    }
}
