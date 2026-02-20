package com.yousign.phpstorm.ecs

import com.jetbrains.php.tools.quality.*

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
        val output = collectedOutput.toString()
        if (output.isBlank()) return

        val lineRegex = """"line"\s*:\s*(\d+)""".toRegex()
        val messageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()

        val lines = lineRegex.findAll(output).map { it.groupValues[1].toInt() }.toList()
        val messages = messageRegex.findAll(output).map { it.groupValues[1] }.toList()

        for (i in messages.indices) {
            val lineNumber = lines.getOrElse(i) { 1 }
            QualityToolMessage(
                this,
                lineNumber,
                QualityToolMessage.Severity.WARNING,
                messages[i]
            )
        }
    }
}
