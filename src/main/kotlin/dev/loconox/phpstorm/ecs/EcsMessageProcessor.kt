package dev.loconox.phpstorm.ecs

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
        if (collectedOutput.isEmpty()) {
            LOG.info("ECS annotator: receiving first line of output")
        }
        collectedOutput.appendLine(line)
    }

    override fun done() {
        super.done()

        val output = collectedOutput.toString().trim()
        LOG.info("ECS annotator: done() called, total output length: ${output.length} chars")
        if (output.isEmpty()) {
            LOG.warn("ECS produced no output")
            return
        }

        val messages = EcsOutputParser.parseOutput(output)
        if (messages.isEmpty() && EcsOutputParser.findJsonStart(output) < 0) {
            LOG.warn("ECS output contains no JSON: ${output.take(200)}")
        }

        for (msg in messages) {
            addMessage(QualityToolMessage(this, msg.line, QualityToolMessage.Severity.WARNING, msg.message))
        }
    }

    companion object {
        private val LOG = Logger.getInstance(EcsMessageProcessor::class.java)
    }
}
