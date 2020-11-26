FROM gradle:6.7-jdk11

ARG user=gradle
ARG project_dir="/home/gradle/project/"

COPY --chown=$user build.gradle settings.gradle $project_dir
COPY --chown=$user src $project_dir/src

USER $user
WORKDIR $project_dir

RUN gradle --no-daemon clean check installDist

FROM adoptopenjdk:11-jre-hotspot

ARG distribution_dir="/home/gradle/project/build/install"

COPY --from=0 $distribution_dir /opt/

ENTRYPOINT ["sh", "/opt/port-bsdiff/bin/port-bsdiff"]
