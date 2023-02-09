package io.github.nefilim.mill.githubrelease

import mill.T
import mill.define.{Command, Input, Module}
import mill.scalalib.PublishModule

trait GitHubReleaseModule extends Module { this: PublishModule =>
  def apiToken: String
  def repoOwner: String
  def repo: String
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
  def createReleaseURL: String = s"${apiBaseURL.trim.stripSuffix("/")}/repos/$repoOwner/$repo/releases"

  def createGitHubRelease(): Command[Unit] = T.command {
    val r = requests.post(
      createReleaseURL,
      headers = Map(
        "Accept" -> "application/vnd.github+json",
        "Authorization" -> s"Bearer: $apiToken",
        "X-GitHub-Api-Version" -> "2022-11-28",
      ),
      data = Map(
        "tag_name" -> tagName(),
        "target_commitish" -> targetCommitish,
        "name" -> releaseName(),
        "draft" -> draft.toString,
        "prerelease" -> preRelease.toString,
        "generate_release_notes" -> generateReleaseNotes.toString,
        "make_latest" -> makeLatestRelease.toString,
      ) ++ body.map(b => Map("body" -> b)).getOrElse(Map.empty)
    )
  }
}
