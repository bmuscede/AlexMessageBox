#!/bin/bash

CURRENT_DIR="$(readlink -f $(dirname ${BASH_SOURCE[0]})/..)"
SCRIPT_DIR="${CURRENT_DIR}/scripts"
source ${SCRIPT_DIR}/include.sh

# Fetch all the libraries.
LIBS=$(ls ${CURRENT_DIR}/lib)
CLASSPATH="."
for LIB in ${LIBS}; do
    _isWindows
    if [ $? -eq 1 ]; then
        CLASSPATH="${CLASSPATH};"
    else
        CLASSPATH="${CLASSPATH}:"
    fi
    CLASSPATH="${CLASSPATH}lib/${LIB}"
done

# Clean before we build.
_info "Cleaning current build directory..."
rm ${CURRENT_DIR}/bin/MessageBox.jar &> /dev/null
rm -r ${CURRENT_DIR}/bin/lib &> /dev/null
rm -r ${CURRENT_DIR}/build &> /dev/null

# First, get a list of all files and attempt to compile.
_info "Compiling Java files..."
FILES=$(find ${CURRENT_DIR} -type f -name "*.java")
javac -cp "${CLASSPATH}" -d ${CURRENT_DIR}/build ${FILES} &> /dev/null

if [ $? -ne 0 ]; then
    _error "Error: There was a problem compiling Java files to Class files."
    exit 1
fi

# Next, create the Manifest.
_info "Generating manifest..."
rm ${CURRENT_DIR}/manifest.txt
echo "Manifest-Version: 1.0" > ${CURRENT_DIR}/manifest.txt
echo "Created-By: Bryan Muscedere" >> ${CURRENT_DIR}/manifest.txt
echo "Main-Class: ca.muscedere.window.MainFrame" >> ${CURRENT_DIR}/manifest.txt

# Move the resources under the build directory.
_info "Building JAR file for first run..."
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
    _error "Error: Could not build the JAR file."
    exit 1
fi

# Last move to the binary directory.
mkdir -p ${CURRENT_DIR}/bin
mv ${CURRENT_DIR}/build/MessageBox.jar ${CURRENT_DIR}/bin/MessageBox.jar
cp -r ${CURRENT_DIR}/lib ${CURRENT_DIR}/bin/lib

# Clean after.
rm -r build &> /dev/null