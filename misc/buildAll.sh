#!/bin/bash

searchDir=$PWD

buildFiles=$(find ${searchDir} -name "build.gradle.kts")

echo
echo "Building Example projects"
echo "========================="
echo
for buildFile in ${buildFiles}; do
    project=$(dirname ${buildFile})
    echo "Building $(basename ${project})..."
    echo "-----------------------------------------------"
    cd ${project}
    if ! ./gradlew clean build --no-daemon --stacktrace; then
      exit 1
    fi
    echo
done
