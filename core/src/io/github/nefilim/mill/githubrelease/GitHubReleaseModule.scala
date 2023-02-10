package io.github.nefilim.mill.githubrelease

import mill.T
import mill.define.{Command, Input, Module}
import upickle.default.{ReadWriter => RW, macroRW}

trait GitHubReleaseModule extends Module {
  def apiToken: String = Option(System.getenv("GITHUB_TOKEN")).getOrElse("")
  def repo: String = Option(System.getenv("GITHUB_REPOSITORY")).getOrElse("")
  def tagName: String
  def targetCommitish: String = "main"
  def releaseName: String = tagName
  def body: String = ""
  def draft: Boolean = false
  def preRelease: Boolean = false
  def generateReleaseNotes: Boolean = true
  def makeLatestRelease: Boolean = true
  def apiBaseURL: String = "https://api.github.com"
  def createReleaseURL: String = s"${apiBaseURL.trim.stripSuffix("/")}/repos/$repo/releases"

  def createGitHubRelease(): Command[Unit] = T.command {
    if (repo.isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.repo] is not configured")
    if (tagName.isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.tagName] is not configured")
    if (apiToken == null || apiToken.isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.apiToken] is not configured")
    if (createReleaseURL == null || createReleaseURL.isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.createReleaseURL] is not configured")

    requests.post(
      createReleaseURL,
      headers = Map(
        "Accept" -> "application/vnd.github+json",
        "Authorization" -> s"Bearer $apiToken",
        "X-GitHub-Api-Version" -> "2022-11-28",
      ),
      data = upickle.default.stream(Release(
        tag_name = tagName,
        target_commitsh = targetCommitish,
        name = releaseName,
        body = body,
        draft = draft,
        prerelease = preRelease,
        generate_release_notes = generateReleaseNotes,
        make_latest = makeLatestRelease,
      ))
    )
    ()
  }

  def logConfig(): Unit = T.command {
    T.log.info(
      s"""
        |repo: [$repo]
        |tagName: [$tagName]
        |targetCommitish: [$targetCommitish]
        |body: [$body]
        |draft: [$draft]
        |preRelease: [$preRelease]
        |generateReleaseNotes: [$generateReleaseNotes]
        |makeLatestRelease: [$makeLatestRelease]
        |apiBaseURL: [$apiBaseURL]
        |createReleaseURL: [$createReleaseURL]
        |""".stripMargin)
  }
}

case class Release(
  tag_name: String,
  target_commitsh: String,
  name: String,
  body: String,
  draft: Boolean = false,
  prerelease: Boolean = false,
  generate_release_notes: Boolean = true,
  make_latest: Boolean = true,
)
object Release {
  implicit val rw: RW[Release] = macroRW
}
