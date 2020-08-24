# https://docs.docker.com/engine/reference/builder/
FROM adoptopenjdk/openjdk8:alpine-jre
LABEL maintainer="sunyuchen1990@gmail.com"

WORKDIR /opt/lore-blog/webapp/

ENV TIMEZONE Asia/Shanghai

# 修改镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
# 设置时区
RUN ln -snf /usr/share/zoneinfo/${TIMEZONE} /etc/localtime && echo "${TIMEZONE}" > /etc/timezone
RUN apk add fontconfig && apk add --update ttf-dejavu && fc-cache --force

COPY ./build/libs/*.jar lore-blog.jar
# jdbc和jwt的密钥文件, 根据实际设置位置做相应修改
COPY ./jdbcSecretKey jdbcSecretKey
COPY ./jwtSecretKey jwtSecretKey
COPY ./change-passwd.sql change-passwd.sql

EXPOSE 8080

CMD [ "sh", "-c", "java -XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-Djava.security.egd=file:/dev/./urandom \
-jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 lore-blog.jar" ]
