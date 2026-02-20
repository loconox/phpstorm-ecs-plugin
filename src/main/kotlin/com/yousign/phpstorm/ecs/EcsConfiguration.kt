package com.yousign.phpstorm.ecs

import com.jetbrains.php.tools.quality.QualityToolConfiguration

class EcsConfiguration : QualityToolConfiguration {

    private var toolPath: String = ""
    private var timeout: Int = 30000
    private var interpreterId: String = ""
    private var ecsConfigPath: String = ""

    override fun getId(): String = "ecs"

    override fun getInterpreterId(): String = interpreterId

    override fun getTimeout(): Int = timeout

    override fun setTimeout(value: Int) {
        timeout = value
    }

    override fun getToolPath(): String = toolPath

    override fun setToolPath(path: String?) {
        toolPath = path ?: ""
    }

    override fun getMaxMessagesPerFile(): Int = 100

    override fun clone(): EcsConfiguration {
        val copy = EcsConfiguration()
        copy.toolPath = this.toolPath
        copy.timeout = this.timeout
        copy.interpreterId = this.interpreterId
        copy.ecsConfigPath = this.ecsConfigPath
        return copy
    }

    override fun compareTo(other: QualityToolConfiguration): Int {
        return toolPath.compareTo(other.toolPath ?: "")
    }

    fun getEcsConfigPath(): String = ecsConfigPath

    fun setEcsConfigPath(path: String) {
        ecsConfigPath = path
    }
}
