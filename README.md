# Mill GitHub Release

## Quickstart

```scala
import $ivy.`io.github.nefilim.mill::mill-github-release::0.0.8`

object releaseModule extends GitHubReleaseModule {
  override def tagName = T.input { s"v${versionCalculator.calculatedVersion()}" }
  override def apiBaseURL = "https://github.company.co/api/v3"
}
```