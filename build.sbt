organization := "raimohanska"

name := "rump-android"

version := "1.0.1"

crossPaths := false

publishTo <<= (version) { version: String =>
      Some(Resolver.file("file", new File("../raimohanska-mvn-repo") / {
        if  (version.trim.endsWith("SNAPSHOT"))  "snapshots"
        else                                     "releases/" }    ))
}


