## Interview test task implementation

Written with Kotlin in to versions: desktop and servlet

### Pre-build
You need Maven to build and launch this application.

Before building a project pls install **WolframAlpha-1.1.jar** from the root of the project as maven plugin with groupId, artifactId and version as follows

```sh

mvn install:install-file \
   -Dfile=WolframAlpha-1.1.jar \
   -DgroupId=wolframalpha \
   -DartifactId=fullapi \
   -Dversion=1.1 \
   -Dpackaging=jar \
   -DgeneratePom=true

```

### Build

```sh

mvn compile

```

### Test

```sh

mvn test

```

### Launch desktop JavaFX app

```sh

WFA_ID=[Your wolphram alpha appID] mvn jfx:run

```

### Launch servlet

```
WFA_ID=[Your wolphram alpha appID] mvn jetty:run -P servlet

```

Open http://localhost:8080 page