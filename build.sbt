import org.jetbrains.sbtidea.{AutoJbr, JbrPlatform}

lazy val scala213           = "2.13.16"
lazy val scalaPluginVersion = "2025.2.593:Nightly"
lazy val minorVersion       = "0"
lazy val buildVersion       = sys.env.getOrElse("ZIO_INTELLIJ_BUILD_NUMBER", minorVersion)
lazy val pluginVersion      = s"2025.2.1.$buildVersion"

ThisBuild / intellijPluginName := "zio-intellij"
ThisBuild / intellijBuild := "252.23892.409"
ThisBuild / jbrInfo := AutoJbr(explicitPlatform = Some(JbrPlatform.osx_aarch64))

Global / intellijAttachSources := true

addCommandAlias("fmt", "scalafmtAll")
addCommandAlias("check", "scalafmtCheckAll")

(Global / javacOptions) := Seq("--release", "17")

ThisBuild / scalacOptions ++= Seq(
  "-explaintypes",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xlint:serial",
  "-Ymacro-annotations",
  "-Xfatal-warnings",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:existentials",
  "-Wconf:msg=legacy-binding:s"
)

lazy val root =
  newProject("zio-intellij", file("."))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      patchPluginXml := pluginXmlOptions { xml =>
        xml.version = version.value
        xml.changeNotes = sys.env.getOrElse(
          "ZIO_INTELLIJ_CHANGE_NOTES",
          s"""<![CDATA[
        <b>What's new?</b>
        <ul>
          <li>IntelliJ IDEA 2025.1 support!</li>
        </ul>
        <b>Note:</b> The ZIO project wizard is temporarily disabled due to incompatibility issues.
        ]]>"""
        )
      }
    )
    .dependsOn(macros)

lazy val macros = newProject("macros", file("macros"))
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scala213 intransitive ()
    )
  )

def newProject(projectName: String, base: File): Project =
  Project(projectName, base).settings(
    name := projectName,
    scalaVersion := scala213,
    version := pluginVersion,
    libraryDependencies ++= Seq(
      "junit"             % "junit"             % "4.13.2" % Test,
      "pl.pragmatists"    % "JUnitParams"       % "1.1.1"  % Test,
      "com.github.sbt"    % "junit-interface"   % "0.13.3" % Test,
      "org.junit.jupiter" % "junit-jupiter-api" % "5.13.4" % Test,
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-s", "-a", "+c", "+q"),
    intellijPlugins := Seq(
      "com.intellij.java".toPlugin,
      s"org.intellij.scala:$scalaPluginVersion".toPlugin,
      "JUnit".toPlugin
    ),
    intellijMainJars := intellijMainJars.value.filterNot(file => excludeJarsFromPlatformDependencies(file)),
//    intellijPluginJars := intellijPluginJars.value.map { case PluginJars(descriptor, root, cp) =>
//      PluginJars(descriptor, root, cp.filterNot(_.getName.contains("junit-jupiter-api")))
//    },
    (Test / scalacOptions) += "-Xmacro-settings:enable-expression-tracers"
  )

def excludeJarsFromPlatformDependencies: Attributed[File] => Boolean = { file =>
  val fileName = file.data.getName
  // We explicitly specify dependency on JUnit 4 library.
  // See also https://youtrack.jetbrains.com/issue/IDEA-315065/The-IDE-runtime-classpath-contains-conflicting-JUnit-classes-from-lib-junit.jar-vs-lib-junit4.jar#focus=Comments-27-6987325.0-0
  fileName == "junit4.jar"
}
