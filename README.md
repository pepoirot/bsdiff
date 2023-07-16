Partial port of bsdiff 4.3 to Java, using Apache Commons Compress:
- bspatch ported
- bsdiff TODO

The current version requires Java 11.

Original bsdiff by Colin Percival:
    http://www.daemonology.net/bsdiff/

Licensed under the same license as the original bsdiff.

### Build

    docker build .

or:

    docker run \
        -u gradle \
        -v "$PWD":/home/gradle/project \
        -w /home/gradle/project \
        --rm \
        gradle:8-jdk11 \
        gradle clean build

## Running

    docker build \
        --tag=bsdiff \
        .
    docker run \
        -it \
        -v "$PWD":/tmp/run \
        -w /tmp/run \
        --rm \
        bsdiff \
        source_file target_file source.patch

### Upgrading Gradle

    docker run \
        -u gradle \
        -v "$PWD":/home/gradle/project \
        -w /home/gradle/project \
        --rm \
        gradle:8-jdk11 \
        gradle wrapper --gradle-version 8.2.1
