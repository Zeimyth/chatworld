import com.benmccann.playplovr.PlayPlovrPlugin._

name := "ChatWorld"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	jdbc,
	anorm,
	cache,
	"com.github.nscala-time" %% "nscala-time" % "0.8.0"
)

play.Project.playScalaSettings ++ defaultPlovrSettings ++ Seq(
	// my Play custom settings
	// project-specific plovr settings
	plovrTargets <<= baseDirectory { base => Seq(
		base / "plovr" /  "plovr.json" -> "public/javascripts/compiled.js"
	)}
)
