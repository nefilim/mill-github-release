package io.github.nefilim.mill.githubrelease

import mill.T
import mill.define.{Command, Module}
import upickle.default.{macroRW, ReadWriter => RW}

trait GitHubReleaseModule extends Module {
  def apiToken: T[String] = T { T.env.getOrElse("GITHUB_TOKEN", "") }
  def repo: T[String] = T { T.env.getOrElse("GITHUB_REPOSITORY", "") }
  def tagName: T[String]
  def targetCommitish: T[String] = T.input { "main" }
  def releaseName: T[String] = tagName
  def body: T[String] = T.input { "" } // upickle is not able to drop Option None values with their keys :(((
  def draft: T[Boolean] = T.input { false }
  def preRelease: T[Boolean] = T.input { false }
  def generateReleaseNotes: T[Boolean] = T.input { true }
  def makeLatestRelease: T[Boolean] = T.input { true }
  def apiBaseURL: T[String] = T { "https://api.github.com" }
  def createReleaseURL: T[String] = T.input { s"${apiBaseURL().trim.stripSuffix("/")}/repos/${repo()}/releases" }

  def createGitHubRelease(): Command[Unit] = T.command {
    if (repo().isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.repo] is not configured")
    if (tagName().isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.tagName] is not configured")
    if (apiToken().isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.apiToken] is not configured")
    if (createReleaseURL().isBlank)
      throw new IllegalArgumentException("[GitHubReleaseModule.createReleaseURL] is not configured")

    requests.post(
      createReleaseURL(),
      headers = Map(
        "Accept" -> "application/vnd.github+json",
        "Authorization" -> s"Bearer $apiToken",
        "X-GitHub-Api-Version" -> "2022-11-28",
      ),
      data = upickle.default.stream(Release(
        tag_name = tagName(),
        target_commitsh = targetCommitish(),
        name = releaseName(),
        body = body(),
        draft = draft(),
        prerelease = preRelease(),
        generate_release_notes = generateReleaseNotes(),
        make_latest = makeLatestRelease(),
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
