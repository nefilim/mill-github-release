package io.github.nefilim.mill.githubrelease

import mill.T
import mill.define.{Command, Input, Module}
import mill.scalalib.PublishModule
import upickle.default.{ReadWriter => RW, macroRW}

trait GitHubReleaseModule extends Module { this: PublishModule =>
  def apiToken: String = Option(System.getenv("GITHUB_TOKEN")).getOrElse("")
  def repo: String = Option(System.getenv("GITHUB_REPOSITORY")).getOrElse("")
  def tagPrefix: String = "v"
  def tagName: Input[String] = T.input { s"${tagPrefix}${publishVersion()}" }
  def targetCommitish: String = "main"
  def releaseName: Input[String] = tagName
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
    if (tagName().isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.tagName] is not configured")
    if (apiToken == null || apiToken.isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.apiToken] is not configured")

    requests.post(
      createReleaseURL,
      headers = Map(
        "Accept" -> "application/vnd.github+json",
        "Authorization" -> s"Bearer $apiToken",
        "X-GitHub-Api-Version" -> "2022-11-28",
      ),
      data = upickle.default.stream(Release(
        tag_name = tagName(),
        target_commitsh = targetCommitish,
        name = releaseName(),
        body = body,
        draft = draft,
        prerelease = preRelease,
        generate_release_notes = generateReleaseNotes,
        make_latest = makeLatestRelease,
      ))
    )
    ()
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
