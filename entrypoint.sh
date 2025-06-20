#!/bin/bash

chown -R "${PUID}":"${PGID}" /app /log
chown "${PUID}":"${PGID}" /data

cd /app

umask "${UMASK}"

exec gosu "${PUID}":"${PGID}" java $JAVA_OPTS -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+PrintGCDetails -Xloggc:/log/gc.log -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/log -jar /app/openliststrm.jar
