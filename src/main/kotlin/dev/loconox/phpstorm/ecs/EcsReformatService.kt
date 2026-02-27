package dev.loconox.phpstorm.ecs

import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.EnvironmentUtil
import com.jetbrains.php.config.PhpProjectConfigurationFacade
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.tools.quality.QualityToolExternalFormatter
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempDirectory

class EcsReformatService : AsyncDocumentFormattingService() {

    companion object {
        private val LOG = Logger.getInstance(EcsReformatService::class.java)
    }

    override fun getFeatures(): Set<FormattingService.Feature> = emptySet()

    override fun canFormat(file: PsiFile): Boolean {
        if (file.language != PhpLanguage.INSTANCE) return false
        val project = file.project

        // Don't interfere if the built-in External Formatter is active
        if (QualityToolExternalFormatter.isEnabled(project)) return false

        if (!EcsConfigValidator.validate(project)) return false

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

        val phpPath = resolvePhpPath(project)

        return EcsFormattingTask(request, resolvedToolPath, ecsConfigPath, basePath, config.timeout, phpPath)
    }

    override fun getNotificationGroupId(): String = "PHP External Quality Tools"

    override fun getName(): String = "Easy Coding Standard"

    private fun resolvePhpPath(project: Project): String? {
        return try {
            PhpProjectConfigurationFacade.getInstance(project).interpreter?.pathToPhpExecutable
        } catch (e: Exception) {
            LOG.warn("ECS format: could not resolve PHP interpreter: ${e.message}")
            null
        }
    }

    private fun resolveToolPath(project: Project): String? {
        val config = EcsConfigurationManager.getInstance(project).localSettings
        val toolPath = config.toolPath
        val basePath = project.basePath

        val toolFile = when {
            toolPath.isNotBlank() && File(toolPath).isAbsolute -> File(toolPath)
            toolPath.isNotBlank() && basePath != null -> File(basePath, toolPath)
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
        private val timeout: Int,
        private val phpPath: String?
    ) : FormattingTask {

        @Volatile
        private var process: Process? = null

        override fun run() {
            val ioFile = request.ioFile
            if (ioFile == null) {
                LOG.warn("ECS format: file is not saved to disk, skipping")
                request.onError("ECS", "Cannot format: file is not saved to disk")
                return
            }

            val tempDir = createTempDirectory("ecs_format_").toFile()
            val tempFile = File(tempDir, ioFile.name)
            try {
                tempFile.writeText(request.documentText)

                val command = mutableListOf<String>()
                if (phpPath != null) {
                    command.add(phpPath)
                }
                command.addAll(listOf(toolPath, "check", "--fix", "--no-progress-bar", "-n"))
                if (configPath.isNotEmpty()) {
                    command.add("--config=$configPath")
                }
                command.add(tempFile.absolutePath)

                LOG.info("ECS format: executing command: ${command.joinToString(" ")}")
                LOG.info("ECS format: working directory: $workingDir, timeout: ${timeout}ms")

                val processBuilder = ProcessBuilder(command)
                    .directory(File(workingDir))
                    .redirectErrorStream(true)

                // Inherit the user's shell environment (includes PATH with php)
                processBuilder.environment().putAll(EnvironmentUtil.getEnvironmentMap())

                val startTime = System.currentTimeMillis()
                process = processBuilder.start()

                val output = process!!.inputStream.bufferedReader().readText()
                val finished = process!!.waitFor(timeout.toLong(), TimeUnit.MILLISECONDS)
                val elapsed = System.currentTimeMillis() - startTime

                if (!finished) {
                    LOG.warn("ECS format: TIMEOUT after ${elapsed}ms (limit: ${timeout}ms) for file ${ioFile.name}")
                    LOG.warn("ECS format: process output before timeout: ${output.take(1000)}")
                    process?.destroyForcibly()
                    request.onError("ECS", "ECS format timed out after ${timeout}ms")
                    return
                }

                val exitCode = process!!.exitValue()
                LOG.info("ECS format: finished in ${elapsed}ms, exit code: $exitCode, file: ${ioFile.name}")
                if (output.isNotEmpty()) {
                    LOG.info("ECS format: process output: ${output.take(1000)}")
                }

                val formattedText = tempFile.readText()
                request.onTextReady(formattedText)
            } catch (e: Exception) {
                LOG.warn("ECS format: exception for file ${ioFile.name}: ${e.message}", e)
                request.onError("ECS", e.message ?: "Unknown error during formatting")
            } finally {
                tempFile.delete()
                tempDir.delete()
            }
        }

        override fun cancel(): Boolean {
            LOG.warn("ECS format: task cancelled, destroying process")
            process?.destroyForcibly()
            return true
        }
    }
}
