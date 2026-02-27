package dev.loconox.phpstorm.ecs

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import java.io.File

object EcsConfigValidator {

    private val LOG = Logger.getInstance(EcsConfigValidator::class.java)
    private var lastNotificationTimeMs = 0L
    private const val NOTIFICATION_COOLDOWN_MS = 60_000L

    /**
     * Resolves the ECS binary path from configuration and checks that it exists and is executable.
     * Shows a balloon notification in the IDE when something is wrong (rate-limited to once per minute).
     *
     * @return `true` if the binary is usable, `false` otherwise.
     */
    fun validate(project: Project): Boolean {
        val basePath = project.basePath
        val config = EcsConfigurationManager.getInstance(project).localSettings
        val toolPath = config.toolPath

        // Resolve the expected binary path (mirrors the resolution logic in ConfigurationManager/ReformatService)
        val toolFile = when {
            !toolPath.isNullOrBlank() && File(toolPath).isAbsolute -> File(toolPath)
            !toolPath.isNullOrBlank() && basePath != null -> File(basePath, toolPath)
            basePath != null -> File(basePath, EcsConfiguration.DEFAULT_TOOL_PATH)
            else -> null
        }

        val toolError = when {
            toolFile == null ->
                "ECS binary path could not be resolved (no project base path)."
            !toolFile.exists() ->
                "ECS binary not found: ${toolFile.absolutePath}"
            !toolFile.canExecute() ->
                "ECS binary is not executable: ${toolFile.absolutePath}"
            else -> null
        }

        if (toolError != null) {
            LOG.warn("ECS validation failed: $toolError")
            notifyUser(project, toolError)
            return false
        }

        // Resolve the ECS config file path
        val ecsConfigPath = config.getEcsConfigPath()
        val configFile = when {
            ecsConfigPath.isNotBlank() && File(ecsConfigPath).isAbsolute -> File(ecsConfigPath)
            ecsConfigPath.isNotBlank() && basePath != null -> File(basePath, ecsConfigPath)
            basePath != null -> File(basePath, EcsConfiguration.DEFAULT_CONFIG_PATH)
            else -> null
        }

        if (configFile != null && !configFile.exists()) {
            val configError = "ECS config file not found: ${configFile.absolutePath}"
            LOG.warn("ECS validation failed: $configError")
            notifyUser(project, configError)
            return false
        }

        return true
    }

    private const val CONFIGURABLE_ID = "settings.php.quality.tools.ecs"

    private fun notifyUser(project: Project, message: String) {
        val now = System.currentTimeMillis()
        if (now - lastNotificationTimeMs < NOTIFICATION_COOLDOWN_MS) return
        lastNotificationTimeMs = now

        NotificationGroupManager.getInstance()
            .getNotificationGroup("ECS")
            .createNotification(
                "ECS Configuration Problem",
                message,
                NotificationType.WARNING
            )
            .addAction(NotificationAction.createSimple("Configure\u2026") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, CONFIGURABLE_ID)
            })
            .notify(project)
    }
}