#!/bin/bash

APP_ARGS="${APP_ARGS} $@"

CP="./"
CP="$CP:`cat apps/totalk-migrations-0.0.1.cp.txt`"
CP="$CP:apps/totalk-migrations-0.0.1.jar"

JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+DisableExplicitGC -XX:+PrintCommandLineFlags"

exec java $JAVA_OPTS -cp $CP com.tobox.totalk.migrations.ToTalkMigrationsApp $APP_ARGS 2>&1
