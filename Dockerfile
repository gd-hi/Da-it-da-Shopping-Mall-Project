# 사용할 Java 이미지 지정
FROM openjdk:17-jdk-alpine
# .jar 파일을 컨테이너에 복사
COPY target/*.jar app.jar
# 컨테이너 실행 시 .jar 파일 실행
ENTRYPOINT ["java","-jar","/app.jar"]
