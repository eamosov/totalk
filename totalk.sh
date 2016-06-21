#!/bin/bash
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintCommandLineFlags"
JAVA_OPTS="${JAVA_OPTS} -XX:+AggressiveOpts"
JAVA_CMD="java"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

#defaults

NODE="totalk-backend"
VERSION=0.0.1
MAIN="com.tobox.totalk.TotalkApplication"
NODE_INDEX=1

XMS=200m
XMX=500m

if [ -z $HOST_IP ]; then
	HOST_IP="0.0.0.0"
fi

NOPTS=`getopt i:x:h: $*`
set -- $NOPTS
until [ -z "$1" ]
do
	case "$1" in
		-i)
			shift
			NODE_INDEX=$1
			;;
		-x)
			shift
			XMX=$1
			;;
		-h)
			shift
			HOST_IP=$1
			;;
		--)
			shift
			break
			;;
	esac
	shift
done


APP_ARGS="${APP_ARGS} $@"


CLASS_PATH_FILE="${DIR}/apps/${NODE}-${VERSION}.cp.txt"
JAR="apps/${NODE}-${VERSION}.jar"
LOG_FILE="${NODE}-${NODE_INDEX}.log"

BASE_PORT=$((10000 + NODE_INDEX * 1000))

HTTP_PORT=$BASE_PORT
SSL_PORT=$((BASE_PORT + 1))
JDEBUG_PORT=$((BASE_PORT + 3))
THRIFT_SYNC_TCP=$((BASE_PORT + 4))
THRIFT_ASYNC_TCP=$((BASE_PORT + 5))
JMX_PORT=$((BASE_PORT + 6))

echo "HOST_IP: ${HOST_IP}"
echo "http: ${HTTP_PORT}"
echo "https: ${SSL_PORT}"
echo "Java debug: $JDEBUG_PORT"
echo "TCP sync: ${THRIFT_SYNC_TCP}"
echo "TCP async: ${THRIFT_ASYNC_TCP}"
echo "JMX: ${JMX_PORT}"

echo LOG_FILE=$LOG_FILE

JAVA_OPTS="${JAVA_OPTS} -Xmx${XMX} -Xms${XMS}  -DLOG_FILE=$LOG_FILE -Droot-level=INFO"
CPTXT="`cat $CLASS_PATH_FILE`"


CP="./:$CPTXT:apps/`basename $JAR`"

if [ "z$JAVA_HOME" != "z" ]; then
	echo "JAVA_HOME=$JAVA_HOME"
	JAVA_CMD="$JAVA_HOME/bin/java"
fi

APP_ARGS="${APP_ARGS} --node.name=${NODE}-${NODE_INDEX}"
APP_ARGS="${APP_ARGS} --thrift.host=$HOST_IP --thrift.async.host=$HOST_IP --jetty.host=$HOST_IP --jmx.serviceUrl=service:jmx:jmxmp://${HOST_IP}:${JMX_PORT}/"
APP_ARGS="${APP_ARGS} --thrift.port=${THRIFT_SYNC_TCP}  --thrift.async.port=${THRIFT_ASYNC_TCP}"
APP_ARGS="${APP_ARGS} --jetty.port=${HTTP_PORT} --jetty.ssl.port=${SSL_PORT}"

JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,address=${JDEBUG_PORT},server=y,suspend=n"
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/local/lib"
JAVA_OPTS="${JAVA_OPTS} -javaagent:libs/jetty-alpn-agent-2.0.0.jar"

pushd $DIR
exec $JAVA_CMD -cp $CP $JAVA_OPTS $MAIN $APP_ARGS
popd

