Partial port of bsdiff 4.3 to Java, using Apache Commons Compress:
- bspatch ported
- bsdiff TODO

Current version requires Java 1.8.

Original bsdiff by Colin Percival:
    http://www.daemonology.net/bsdiff/

Licensed under the same license as the original bsdiff.

### Build

    docker run \
        -u gradle \
        -v "$PWD":/home/gradle/project \
        -w /home/gradle/project \
        --rm \
        gradle:6.7-jdk11 \
        gradle build

### Upgrading Gradle

    docker run \
        -u gradle \
        -v "$PWD":/home/gradle/project \
        -w /home/gradle/project \
        --rm \
        gradle:6.7-jdk11 \
        gradle wrapper --gradle-version 6.7.1
