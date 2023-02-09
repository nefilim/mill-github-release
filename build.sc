import mill._
import mill.scalalib._
import mill.scalalib.publish._
import $ivy.`io.chris-kipp::mill-ci-release::0.1.5`
import io.kipp.mill.ci.release.CiReleaseModule
import io.kipp.mill.ci.release.SonatypeHost

object Versions {
  val Munit = "0.7.29"
}

trait MillPlatformDependencies {
  def millPlatform: String
  def millVersion: String
  def scalaVersion: String

  val millMain = ivy"com.lihaoyi::mill-main:$millVersion"
  val millScala = ivy"com.lihaoyi::mill-scalalib:$millVersion"
  val munit = ivy"org.scalameta::munit::${Versions.Munit}"
}

object Dependencies {
  object v0_11 extends MillPlatformDependencies {
    override def millPlatform = millVersion // only valid for exact milestones!
    override def millVersion = "0.11.0-M3"
    override def scalaVersion = "2.13.10"
  }

  object v0_10 extends MillPlatformDependencies {
    override def millPlatform = "0.10"
    override def millVersion = "0.10.11" // scala-steward:off
    override def scalaVersion = "2.13.10"
  }
}
val crossCompileDependencies = Seq(Dependencies.v0_10, Dependencies.v0_11)
val millAPIVersions = crossCompileDependencies.groupBy(_.millPlatform)

trait PluginBaseModule extends CrossScalaModule with CiReleaseModule {
  def millAPIVersion: String
  def dependencies: MillPlatformDependencies = millAPIVersions.apply(millAPIVersion).head
  def crossScalaVersion = dependencies.scalaVersion

  override def artifactSuffix: T[String] = s"_mill${dependencies.millPlatform}_${artifactScalaVersion()}"

  override def ivyDeps = T {
    Agg(
      ivy"${scalaOrganization()}:scala-library:${scalaVersion()}",
    )
  }

  override def versionScheme: T[Option[VersionScheme]] = T(Option(VersionScheme.EarlySemVer))

  override def javacOptions = Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8")
  override def scalacOptions = Seq("-target:jvm-1.8", "-encoding", "UTF-8")

  override def sonatypeHost = Some(SonatypeHost.s01)
  def pomSettings = T {
    PomSettings(
      description = "Mill plugin create a GitHub Release",
      organization = "io.github.nefilim.mill",
      url = "https://github.com/nefilim/mill-github-release",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("nefilim", "mill-github-release"),
      developers = Seq(Developer("nefilim", "Peter vR", "https.//github.com/nefilim"))
    )
  }
}

object core extends Cross[CoreCross](millAPIVersions.keys.toSeq: _*)
class CoreCross(override val millAPIVersion: String) extends PluginBaseModule {
  override def artifactName = "mill-github-release"

  // don't generate IntelliJ configuration if ...
  override def skipIdea: Boolean = dependencies != crossCompileDependencies.head

  override def compileIvyDeps = Agg(
    dependencies.millMain,
    dependencies.millScala,
  )

  object test extends Tests with TestModule.Munit {
    override def ivyDeps = Agg(
      dependencies.munit,
      dependencies.millMain,
      dependencies.millScala,
    )
  }
}
