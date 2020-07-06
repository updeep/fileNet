#!/bin/sh

cd "$(dirname "$0")"

### resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
cd ..
APPS_HOME=`pwd`
export APPS_HOME

### Set JavaHome if it exists
if [ -f "${JAVA_HOME}/bin/java" ]; then
   JAVA=${JAVA_HOME}/bin/java
else
   JAVA=java
fi
export JAVA

echo "Using JAVA_HOME: ${JAVA_HOME}"
echo "Using APPS_HOME: $APPS_HOME"

JAVA_OPTS=-server
JAVA_OPTS_SCRIPT="-XX:+HeapDumpOnOutOfMemoryError -Djava.awt.headless=true"

### Use the Hotspot garbage-first collector.
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"

### Have the JVM do less remembered set work during STW, instead
### preferring concurrent GC. Reduces p99.9 latency.
JAVA_OPTS="$JAVA_OPTS -XX:G1RSetUpdatingPauseTimePercent=5"

### Main G1GC tunable: lowering the pause target will lower throughput and vise versa.
### 200ms is the JVM default and lowest viable setting
### 1000ms increases throughput. Keep it smaller than the timeouts.
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=500"

### Optional G1 Settings
### Save CPU time on large (>= 16GB) heaps by delaying region scanning
### until the heap is 70% full. The default in Hotspot 8u40 is 40%.
#JAVA_OPTS="$JAVA_OPTS -XX:InitiatingHeapOccupancyPercent=70"

### For systems with > 8 cores, the default ParallelGCThreads is 5/8 the number of logical cores.
### Otherwise equal to the number of cores when 8 or less.
### Machines with > 10 cores should try setting these to <= full cores.
#JAVA_OPTS="$JAVA_OPTS -XX:ParallelGCThreads=16"

### By default, ConcGCThreads is 1/4 of ParallelGCThreads.
### Setting both to the same value can reduce STW durations.
#JAVA_OPTS="$JAVA_OPTS -XX:ConcGCThreads=16"

### GC logging options -- uncomment to enable

#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDateStamps"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintHeapAtGC"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintTenuringDistribution"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
#JAVA_OPTS="$JAVA_OPTS -XX:+PrintPromotionFailure"
#JAVA_OPTS="$JAVA_OPTS -XX:PrintFLSStatistics=1"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseGCLogFileRotation"
#JAVA_OPTS="$JAVA_OPTS -XX:NumberOfGCLogFiles=10"
#JAVA_OPTS="$JAVA_OPTS -XX:GCLogFileSize=10M"

#JAVA_OPTS="$JAVA_OPTS -Xmx128M"
#JAVA_OPTS="$JAVA_OPTS -Xms128M"

### Console Log Chinese Support
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"

### Define package name
APP_NAME=file-service
APP_SUFFIX=.jar
APP_JAR=$APP_NAME$APP_SUFFIX

### Console Log Parameters
LOGS_NAME="$APP_NAME.`date +%Y%m%d`.`date +%H%M%S`.log"
LOGS_FILE="$APPS_HOME/logs/$LOGS_NAME"
export LOGS_FILE
echo "Using LOGS_FILE: $LOGS_FILE"

### Close currently running services
pid=`ps aux | grep $APP_JAR | grep -v grep | awk '{print $2}'`
if [ "$pid"x != ""x ]; then
    kill -s 9 $pid
    echo `date +%F-%T` "- kill success... pid =" $pid
fi

CTL="$JAVA $JAVA_OPTS $JAVA_OPTS_SCRIPT -jar $APPS_HOME/$APP_JAR"
echo $CTL
export CTL
### Start service, run in the background
nohup ${CTL} > ${LOGS_FILE} 2>&1 &

### logging
pid=`ps aux | grep $APP_JAR | grep -v grep | awk '{print $2}'`
echo `date +%F-%T` "- service is startup success... pid =" $pid
tail -f $LOGS_FILE
exit