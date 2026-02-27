package dev.loconox.phpstorm.ecs

import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class EcsParsedMessage(
    val line: Int,
    val message: String,
)

object EcsOutputParser {

    /**
     * Parses the full ECS JSON output and returns a flat list of messages.
     * Handles both "errors" and "diffs" entries inside the "files" object.
     * Non-JSON prefixes (e.g. PHP notices) are skipped automatically.
     *
     * @return parsed messages, or an empty list when the output is blank / unparseable.
     */
    fun parseOutput(output: String): List<EcsParsedMessage> {
        val trimmed = output.trim()
        if (trimmed.isEmpty()) return emptyList()

        val jsonStart = findJsonStart(trimmed)
        if (jsonStart < 0) return emptyList()

        return try {
            val json = JsonParser.parseString(trimmed.substring(jsonStart)).asJsonObject
            val files = json.get("files") ?: return emptyList()
            if (!files.isJsonObject) return emptyList()

            val messages = mutableListOf<EcsParsedMessage>()
            for ((_, fileData) in files.asJsonObject.entrySet()) {
                val fileObj = fileData.asJsonObject
                messages.addAll(parseErrors(fileObj))
                messages.addAll(parseDiffs(fileObj))
            }
            messages
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseErrors(fileObj: JsonObject): List<EcsParsedMessage> {
        val errors = fileObj.getAsJsonArray("errors") ?: return emptyList()
        return errors.map { errorElement ->
            val errorObj = errorElement.asJsonObject
            val line = errorObj.get("line")?.asInt ?: 1
            val message = errorObj.get("message")?.asString ?: "ECS error"
            val sourceClass = errorObj.get("source_class")?.asString
            val label = if (sourceClass != null) {
                "${shortCheckerName(sourceClass)}: $message"
            } else {
                message
            }
            EcsParsedMessage(line, label)
        }
    }

    fun parseDiffs(fileObj: JsonObject): List<EcsParsedMessage> {
        val diffs = fileObj.getAsJsonArray("diffs") ?: return emptyList()
        val messages = mutableListOf<EcsParsedMessage>()

        for (diffElement in diffs) {
            val diffObj = diffElement.asJsonObject
            val diff = diffObj.get("diff")?.asString ?: continue
            val checkers = diffObj.getAsJsonArray("applied_checkers")
                ?.map { shortCheckerName(it.asString) }
                ?: listOf("ECS")

            val message = checkers.joinToString(", ")
            val lineNumbers = extractLineNumbersFromDiff(diff)

            if (lineNumbers.isEmpty()) {
                messages.add(EcsParsedMessage(1, message))
            } else {
                for (lineNumber in lineNumbers) {
                    messages.add(EcsParsedMessage(lineNumber, message))
                }
            }
        }
        return messages
    }

    fun extractLineNumbersFromDiff(diff: String): List<Int> {
        val hunkRegex = """@@ -(\d+)""".toRegex()
        return hunkRegex.findAll(diff).map { it.groupValues[1].toInt() }.toList()
    }

    fun shortCheckerName(fqcn: String): String {
        return fqcn.substringAfterLast("\\")
    }

    fun findJsonStart(output: String): Int = output.indexOf('{')
}