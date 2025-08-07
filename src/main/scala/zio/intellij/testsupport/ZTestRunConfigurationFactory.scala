package zio.intellij.testsupport

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.scala.project.ProjectExt
import org.jetbrains.plugins.scala.testingSupport.test.AbstractTestRunConfigurationFactory
import zio.intellij.utils.ModuleSyntax

import javax.swing.Icon

final class ZTestRunConfigurationFactory(configurationType: ZTestConfigurationType)
    extends AbstractTestRunConfigurationFactory(configurationType) {

  override def getIcon: Icon = zio.intellij.ZioIcon

  override def id: String = "ZIO Test"

  override def createTemplateConfiguration(project: Project): RunConfiguration = new Zio2TestRunConfiguration(project, this)

}
