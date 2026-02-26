package com.yousign.phpstorm.ecs

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.tools.quality.QualityToolExternalFormatter
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

class EcsReformatService : AsyncDocumentFormattingService() {

    override fun getFeatures(): Set<FormattingService.Feature> = emptySet()

    override fun canFormat(file: PsiFile): Boolean {
        if (file.language != PhpLanguage.INSTANCE) return false
        val project = file.project

        // Don't interfere if the built-in External Formatter is active
        if (QualityToolExternalFormatter.isEnabled(project)) return false

        return resolveToolPath(project) != null
    }

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask {
        val project = request.context.containingFile.project
        val config = EcsConfigurationManager.getInstance(project).localSettings
        val basePath = project.basePath ?: "."

        val resolvedToolPath = resolveToolPath(project)!!

        val ecsConfigPath = config.getEcsConfigPath().ifEmpty {
            val defaultConfig = File(basePath, EcsConfiguration.DEFAULT_CONFIG_PATH)
            if (defaultConfig.exists()) defaultConfig.absolutePath else ""
        }

        return EcsFormattingTask(request, resolvedToolPath, ecsConfigPath, basePath, config.timeout)
    }

    override fun getNotificationGroupId(): String = "PHP External Quality Tools"

    override fun getName(): String = "Easy Coding Standard"

    private fun resolveToolPath(project: com.intellij.openapi.project.Project): String? {
        val config = EcsConfigurationManager.getInstance(project).localSettings
        val toolPath = config.toolPath
        val basePath = project.basePath

        val toolFile = when {
            !toolPath.isNullOrBlank() && File(toolPath).isAbsolute -> File(toolPath)
            !toolPath.isNullOrBlank() && basePath != null -> File(basePath, toolPath)
            basePath != null -> File(basePath, EcsConfiguration.DEFAULT_TOOL_PATH)
            else -> return null
        }

        return if (toolFile.exists()) toolFile.absolutePath else null
    }

    private class EcsFormattingTask(
        private val request: AsyncFormattingRequest,
        private val toolPath: String,
        private val configPath: String,
        private val workingDir: String,
        private val timeout: Int
    ) : FormattingTask {

        @Volatile
        private var process: Process? = null

        override fun run() {
            val ioFile = request.ioFile
            if (ioFile == null) {
                request.onError("ECS", "Cannot format: file is not saved to disk")
                return
            }

            val tempDir = createTempDirectory("ecs_format_").toFile()
            val tempFile = File(tempDir, ioFile.name)
            try {
                tempFile.writeText(request.documentText)

                val command = mutableListOf(toolPath, "check", "--fix", "--no-progress-bar")
                if (configPath.isNotEmpty()) {
                    command.add("--config=$configPath")
                }
                command.add(tempFile.absolutePath)

                val processBuilder = ProcessBuilder(command)
                    .directory(File(workingDir))
                    .redirectErrorStream(true)

                process = processBuilder.start()
                val finished = process!!.waitFor(timeout.toLong(), TimeUnit.MILLISECONDS)

                if (!finished) {
                    process?.destroyForcibly()
                    request.onError("ECS", "ECS format timed out after ${timeout}ms")
                    return
                }

                val formattedText = tempFile.readText()
                request.onTextReady(formattedText)
            } catch (e: Exception) {
                request.onError("ECS", e.message ?: "Unknown error during formatting")
            } finally {
                tempFile.delete()
                tempDir.delete()
            }
        }

        override fun cancel(): Boolean {
            process?.destroyForcibly()
            return true
        }
    }
}
