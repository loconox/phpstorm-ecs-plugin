package com.yousign.phpstorm.ecs

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.php.tools.quality.QualityToolProjectConfigurableForm
import com.jetbrains.php.tools.quality.QualityToolType
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class EcsConfigurable(private val project: Project) : QualityToolProjectConfigurableForm(project) {

    private lateinit var configPathField: TextFieldWithBrowseButton

    override fun getQualityToolType(): QualityToolType<EcsConfiguration> = EcsQualityToolType()

    override fun createComponent(): JComponent {
        val toolPanel = super.createComponent()
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("php")
            .withTitle("ECS Configuration File")

        return panel {
            row {
                cell(toolPanel!!).align(AlignX.FILL)
            }
            row("Configuration file:") {
                textFieldWithBrowseButton(descriptor, project)
                    .align(AlignX.FILL)
                    .comment("Path to ecs.php (leave empty for default)")
                    .also { configPathField = it.component }
            }
        }
    }

    override fun isModified(): Boolean {
        val config = EcsConfigurationManager.getInstance(project).localSettings
        return super.isModified() || configPathField.text != config.getEcsConfigPath()
    }

    override fun apply() {
        super.apply()
        val config = EcsConfigurationManager.getInstance(project).localSettings
        config.setEcsConfigPath(configPathField.text)
    }

    override fun reset() {
        super.reset()
        val config = EcsConfigurationManager.getInstance(project).localSettings
        configPathField.text = config.getEcsConfigPath()
    }
}
