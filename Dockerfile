FROM openjdk:17-jdk-slim

# 시간대 데이터 설치 (slim 이미지에는 기본적으로 없을 수 있음)
RUN apt-get update && apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apt-get clean

ENV TZ=Asia/Seoul

COPY build/libs/app.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]