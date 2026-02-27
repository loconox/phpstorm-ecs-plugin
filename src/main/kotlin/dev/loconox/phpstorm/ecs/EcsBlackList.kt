package dev.loconox.phpstorm.ecs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.jetbrains.php.tools.quality.QualityToolBlackList

@Service(Service.Level.PROJECT)
@State(name = "EcsBlackList", storages = [Storage("ecs_blacklist.xml")])
class EcsBlackList : QualityToolBlackList() {

    companion object {
        fun getInstance(project: Project): EcsBlackList {
            return project.getService(EcsBlackList::class.java)
        }
    }
}
