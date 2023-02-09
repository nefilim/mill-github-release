package io.github.nefilim.mill.githubrelease

import mill.T
import mill.define.{Command, Input, Module}
import mill.scalalib.PublishModule

trait GitHubReleaseModule extends Module { this: PublishModule =>
  def apiToken: String = Option(System.getenv("GITHUB_TOKEN")).getOrElse("")
  def repo: String = Option(System.getenv("GITHUB_REPOSITORY")).getOrElse("")
  def tagPrefix: String = "v"
  def tagName: Input[String] = T.input { s"${tagPrefix}${publishVersion()}" }
  def targetCommitish: String = "main"
  def releaseName: Input[String] = tagName
  def body: Option[String] = None
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
      data = upickle.default.stream(
        Map(
          "tag_name" -> tagName(),
          "target_commitish" -> targetCommitish,
          "name" -> releaseName(),
          "draft" -> draft.toString,
          "prerelease" -> preRelease.toString,
          "generate_release_notes" -> generateReleaseNotes.toString,
          "make_latest" -> makeLatestRelease.toString,
        ) ++ body.map(b => Map("body" -> b)).getOrElse(Map.empty)
      )
    )
  }
}
