FROM eclipse-temurin:8u412-b08-jre-jammy
LABEL title="openlist-strm"
LABEL description="将openlist的视频文件生成媒体播放设备可播放的strm文件"
LABEL authors="JackDing"
RUN apt-get update && apt-get install -y gosu && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY ./target/application.jar /app/openliststrm.jar
COPY --chmod=755 entrypoint.sh /entrypoint.sh
ENV TZ=Asia/Shanghai
ENV openlistServerUrl=""
ENV openlistServerToken=""
ENV openlistScanPath=""
ENV isDownSub="0"
ENV slowMode="1"
ENV encode="1"
ENV tgToken=""
ENV tgUserId=""
ENV JAVA_OPTS="-Xms32m -Xmx512m"
ENV srcDir=""
ENV dstDir=""
ENV replaceDir=""
ENV strmAfterSync="1"
ENV runAfterStartup="1"
ENV minFileSize="100"
ENV logLevel=""
ENV maxIdleConnections="5"
ENV refresh="1"
ENV scheduledCron="0 0 6,18 * * ?"
ENV PUID=0
ENV PGID=0
ENV UMASK=022
ENTRYPOINT [ "/entrypoint.sh" ]
VOLUME /data
VOLUME /log