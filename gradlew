#!/bin/sh
baseDir=$(dirname -- "$( readlink -f -- "$0"; )")
cd "$baseDir" || exit 1
case "$(uname)" in
    CYGWIN*) sep=";" ;;
    MINGW*|MSYS*) sep=";" ;;
    *)        sep=":" ;;
esac
CLASSPATH="$baseDir/gradle/wrapper/gradle-wrapper.jar"
export CLASSPATH
exec java $DEFAULT_JVM_OPTS $JAVA_OPTS \
    -Dorg.gradle.appname="gradlew" \
    classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
