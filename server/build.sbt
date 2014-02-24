name := "ChatWorld"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

// closureCompilerOptions ++= Seq(
//   // "advancedOptimizations",
//   "simpleOptimizations",
//   "checkCaja",
//   "checkControlStructures",
//   "checkTypes",
//   "checkSymbols"
// )
