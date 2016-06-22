#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

APP_ARGS="${APP_ARGS} $@"

CP="./"
CP="$CP:`cat $DIR/apps/totalk-migrations-0.0.1.cp.txt`"
CP="$CP:apps/totalk-migrations-0.0.1.jar"

JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+DisableExplicitGC -XX:+PrintCommandLineFlags"

pushd $DIR
exec java $JAVA_OPTS -cp $CP com.tobox.totalk.migrations.ToTalkMigrationsApp $APP_ARGS 2>&1
