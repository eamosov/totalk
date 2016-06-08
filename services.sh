#!/bin/bash
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintCommandLineFlags"
JAVA_OPTS="${JAVA_OPTS} -XX:+AggressiveOpts"
JAVA_CMD="java"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RUNTIME_DIR="$DIR/../runtime/java"

STOP=

#defaults

NODE="totalk-backend"
VERSION=0.0.1
MAIN="com.tobox.totalk.TotalkApplication"

CLUSTER_NAME="cluster_$$"
MCAST_PORT=45588
XMS=200m
XMX=500m
BIGMEMORY=100m

if [ -f $DIR/services.local.sh ]; then
	source $DIR/services.local.sh
fi

NODE_INDEX=1
JMX_BASE=10000
COPY_CP=1


NOPTS=`getopt m:sn:i:b:l:h:t:kx: $*`
set -- $NOPTS
until [ -z "$1" ]
do
	case "$1" in
		-n)
			shift
			NODE=$1
			case "$NODE" in
				totalk-backend)
					echo "select node: totalk-backend"
					VERSION="0.0.1"
					;;

				*)
					echo "invalid node type"
					exit -1
					;;
			esac	
			;;
		-m)
			shift
			BIGMEMORY=$1
			;;
		-i)
			shift
			NODE_INDEX=$1
			;;
		-l)
			shift
			CLUSTER_NAME=$1
			;;
		-h)
			shift
			BIND_HOST=$1
			;;
		-x)
			shift
			XMX=$1
			;;
		-k)
			COPY_CP=0
			;;
		--)
			shift
			break
			;;
	esac
	shift
done


APP_ARGS="${APP_ARGS} $@"


if [ -z $NODE_INDEX ]; then
	echo "need NODE_INDEX. Use -i option."
	exit -1
fi

PID="/tmp/${NODE}-${NODE_INDEX}.pid"


CLASS_PATH_FILE="${DIR}/apps/${NODE}-${VERSION}.cp.txt"
JAR="${DIR}/apps/${NODE}-${VERSION}.jar"
NODE="${NODE}-${NODE_INDEX}"

if [ "$COPY_CP" = "1" ]; then
	CP_ROOT="${RUNTIME_DIR}/classes-${NODE}"
else
	CP_ROOT=$DIR/libs
fi;

OUTFILE="${RUNTIME_DIR}/${NODE}.out"
LOG_FILE="${NODE}.log"

BASE_PORT=$((JMX_BASE + NODE_INDEX * 1000))

HTTP_PORT=$BASE_PORT
SSL_PORT=$((BASE_PORT + 1))
JDEBUG_PORT=$((BASE_PORT + 3))
THRIFT_SYNC_TCP=$((BASE_PORT + 4))
THRIFT_ASYNC_TCP=$((BASE_PORT + 5))
JMX_PORT=$((BASE_PORT + 6))

echo "http: ${HTTP_PORT}"
echo "https: ${SSL_PORT}"
echo "Java debug: $JDEBUG_PORT"
echo "TCP sync: ${THRIFT_SYNC_TCP}"
echo "TCP async: ${THRIFT_ASYNC_TCP}"
echo "JMX: ${JMX_PORT}"

echo CP_ROOT=$CP_ROOT
echo PID=$PID
echo OUTFILE=$OUTFILE
echo LOG_FILE=$LOG_FILE

JAVA_OPTS="${JAVA_OPTS} -Xmx${XMX} -Xms${XMS}  -DLOG_FILE=$LOG_FILE -Droot-level=INFO"
CPTXT="`cat $CLASS_PATH_FILE`"

if [ ! -d $RUNTIME_DIR ]; then
	mkdir -p $RUNTIME_DIR
fi

if [ ! -d $CP_ROOT ]; then
       mkdir $CP_ROOT
fi

CP="$RUNTIME_DIR"
if [ $COPY_CP = 1 ]; then

	for a in ${CPTXT//:/ } ; do
		CP="$CP:${CP_ROOT//$RUNTIME_DIR/.}/`basename $a`"
	done
	CP="$CP:${CP_ROOT//$RUNTIME_DIR/.}/`basename $JAR`"

	JARS=$JAR
	for a in ${CPTXT//:/ } ; do
	    JARS="${JARS} $DIR/$a"
	done

	rsync -a --delete -L $JARS $CP_ROOT

else
	for a in ${CPTXT//:/ } ; do
		CP="$CP:../../java/$a"
	done

	CP="$CP:../../java/apps/`basename $JAR`"
fi

if [ "z$JAVA_HOME" != "z" ]; then
	echo "JAVA_HOME=$JAVA_HOME"
	JAVA_CMD="$JAVA_HOME/bin/java"
fi

if [ ! -z $NODE ]; then
	APP_ARGS="${APP_ARGS} --node.name=${NODE}"
fi

APP_ARGS="${APP_ARGS} --jmx.serviceUrl=service:jmx:jmxmp://0.0.0.0:${JMX_PORT}/"
APP_ARGS="${APP_ARGS} --jgroups.cluster.name=${CLUSTER_NAME}"
APP_ARGS="${APP_ARGS} --thrift.port=${THRIFT_SYNC_TCP}  --thrift.async.port=${THRIFT_ASYNC_TCP}"
APP_ARGS="${APP_ARGS} --jetty.port=${HTTP_PORT} --jetty.ssl.port=${SSL_PORT}"
#APP_ARGS="${APP_ARGS} --nosms"

if [ ! -z $BIND_HOST ]; then
    APP_ARGS="${APP_ARGS} --jetty.host=${BIND_HOST}"
fi

JAVA_OPTS="${JAVA_OPTS} -Dehcache.maxBytesLocalHeap=${BIGMEMORY}"
JAVA_OPTS="${JAVA_OPTS} -Djgroups.udp.mcast_port=${MCAST_PORT} -Dehcache.jgroups.udp.mcast_port=$((MCAST_PORT + 1))"
JAVA_OPTS="${JAVA_OPTS} -Xdebug -Xrunjdwp:transport=dt_socket,address=${JDEBUG_PORT},server=y,suspend=n"
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=/usr/local/lib"
JAVA_OPTS="${JAVA_OPTS} -javaagent:$CP_ROOT/jetty-alpn-agent-2.0.0.jar"

if [ ! -f ${RUNTIME_DIR}/totalk-local.properties ];then
	ln -s ../../totalk/totalk-local.properties ${RUNTIME_DIR}/totalk-local.properties
fi

#if [ ! -f ${RUNTIME_DIR}/es.properties ];then
#	ln -s ../../totalk/es.properties ${RUNTIME_DIR}/es.properties
#fi

if [ ! -f ${RUNTIME_DIR}/logback-test.xml ];then
	ln -s ../../totalk/logback-test.xml ${RUNTIME_DIR}/logback-test.xml
fi

pushd $RUNTIME_DIR
exec $JAVA_CMD -cp $CP $JAVA_OPTS $MAIN $APP_ARGS
popd

