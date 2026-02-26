package com.yousign.phpstorm.ecs

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.php.tools.quality.QualityToolAnnotatorInfo
import com.jetbrains.php.tools.quality.QualityToolMessage
import com.jetbrains.php.tools.quality.QualityToolType
import com.jetbrains.php.tools.quality.QualityToolXmlMessageProcessor

class EcsMessageProcessor(info: QualityToolAnnotatorInfo<EcsValidationInspection>) :
    QualityToolXmlMessageProcessor(info) {

    private val collectedOutput = StringBuilder()

    override fun getQualityToolType(): QualityToolType<*> = EcsQualityToolType()

    override fun getMessageStart(line: String): Int = -1

    override fun getMessageEnd(line: String): Int = -1

    override fun getXmlMessageHandler(): XMLMessageHandler {
        return object : XMLMessageHandler() {
            override fun isStatusValid(): Boolean = true
            override fun parseTag(tag: String, attributes: org.xml.sax.Attributes) {}
        }
    }

    override fun parseLine(line: String) {
        collectedOutput.appendLine(line)
    }

    override fun done() {
        super.done()

        val output = collectedOutput.toString().trim()
        if (output.isEmpty()) {
            LOG.warn("ECS produced no output")
            return
        }

        // Find the start of the JSON object — ECS may output PHP notices before it
        val jsonStart = output.indexOf('{')
        if (jsonStart < 0) {
            LOG.warn("ECS output contains no JSON: ${output.take(200)}")
            return
        }

        try {
            val jsonOutput = output.substring(jsonStart)
            val json = JsonParser.parseString(jsonOutput).asJsonObject
            val files = json.get("files") ?: return
            if (!files.isJsonObject) return

            for ((_, fileData) in files.asJsonObject.entrySet()) {
                val fileObj = fileData.asJsonObject

                parseErrors(fileObj)
                parseDiffs(fileObj)
            }
        } catch (e: Exception) {
            LOG.warn("ECS output parse error: ${e.message}. Output: ${output.take(500)}")
        }
    }

    companion object {
        private val LOG = Logger.getInstance(EcsMessageProcessor::class.java)
    }

    private fun parseErrors(fileObj: com.google.gson.JsonObject) {
        val errors = fileObj.getAsJsonArray("errors") ?: return
        for (errorElement in errors) {
            val errorObj = errorElement.asJsonObject
            val line = errorObj.get("line")?.asInt ?: 1
            val message = errorObj.get("message")?.asString ?: "ECS error"
            val sourceClass = errorObj.get("source_class")?.asString
            val label = if (sourceClass != null) {
                "${shortCheckerName(sourceClass)}: $message"
            } else {
                message
            }
            addMessage(QualityToolMessage(this, line, QualityToolMessage.Severity.WARNING, label))
        }
    }

    private fun parseDiffs(fileObj: com.google.gson.JsonObject) {
        val diffs = fileObj.getAsJsonArray("diffs") ?: return
        for (diffElement in diffs) {
            val diffObj = diffElement.asJsonObject
            val diff = diffObj.get("diff")?.asString ?: continue
            val checkers = diffObj.getAsJsonArray("applied_checkers")
                ?.map { shortCheckerName(it.asString) }
                ?: listOf("ECS")

            val message = checkers.joinToString(", ")
            val lineNumbers = extractLineNumbersFromDiff(diff)

            if (lineNumbers.isEmpty()) {
                addMessage(QualityToolMessage(this, 1, QualityToolMessage.Severity.WARNING, message))
            } else {
                for (lineNumber in lineNumbers) {
                    addMessage(QualityToolMessage(this, lineNumber, QualityToolMessage.Severity.WARNING, message))
                }
            }
        }
    }

    private fun extractLineNumbersFromDiff(diff: String): List<Int> {
        val hunkRegex = """@@ -(\d+)""".toRegex()
        return hunkRegex.findAll(diff).map { it.groupValues[1].toInt() }.toList()
    }

    private fun shortCheckerName(fqcn: String): String {
        return fqcn.substringAfterLast("\\")
    }
}
