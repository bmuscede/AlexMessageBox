#!/bin/bash

CURRENT_DIR="$(readlink -f $(dirname ${BASH_SOURCE[0]})/..)"

# First, get a list of all files and attempt to compile.
echo "Building the current project. Please wait..."
FILES=$(find ${CURRENT_DIR} -type f -name "*.java")
javac -d ${CURRENT_DIR}/build ${FILES} &> /dev/null

if [ $? -ne 0 ]; then
    echo "There was a problem building this project."
    exit 1
fi

# Next, create the Manifest.
if [ ! -f ${CURRENT_DIR}/manifest.txt ]; then
    echo "Manifest-Version: 1.0" > ${CURRENT_DIR}/manifest.txt
    echo "Created-By: Bryan Muscedere" >> ${CURRENT_DIR}/manifest.txt
    echo "Main-Class: ca.muscedere.window.MainFrame" >> ${CURRENT_DIR}/manifest.txt
fi

# Move the resources under the build directory.
cp -r ${CURRENT_DIR}/res/img ${CURRENT_DIR}/build/img
cp -r ${CURRENT_DIR}/res/sound ${CURRENT_DIR}/build/sound

# Once complete, bundle the JAR.
PAST_DIR=$(pwd)
cd ${CURRENT_DIR}/build
JAR_FILES=$(find . -type f)
jar cvfm MessageBox.jar ${CURRENT_DIR}/manifest.txt ${JAR_FILES} &> /dev/null
STATUS_CODE=$?
cd ${PAST_DIR}

# Check the result.
if [ ${STATUS_CODE} -ne 0 ]; then
    echo "Could not build the JAR file."
    exit 1
fi

# Last move to the binary directory.
mkdir -p ${CURRENT_DIR}/bin
mv ${CURRENT_DIR}/build/MessageBox.jar ${CURRENT_DIR}/bin/MessageBox.jar