# -- Dockerfile --
# 这个文件负责构建包含你的程序的 Docker 容器

# 使用 Java 12
FROM openjdk:12
# 编译程序
WORKDIR /app/
# 向容器内复制文件
COPY src ./src
RUN javac -sourcepath src ./src/Analyze.java -Xlint:unchecked -d ./
