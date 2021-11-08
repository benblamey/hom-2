FROM openjdk:17-jdk

COPY build/libs/haste-o-matic-impl-2.stream-worker2-1.0-SNAPSHOT.jar output.jar


# ENTRYPOINT ["java","-jar","output.jar"]

# This is left so that different main classes can be specified, and different properties set
ENTRYPOINT ["java"]