#!/bin/bash

################################################
#            Alex's Message Box                #
#     Start Script for Alex's Message Box      #
#                                              #
# This script checks for updates, creates the  #
# JAR, and then starts the message box.        # 
#################################################

CURRENT_DIR="$(readlink -f $(dirname ${BASH_SOURCE[0]}))"
SCRIPT_DIR="${CURRENT_DIR}/scripts"

SKIP_UPDATES=0
SKIP_BUILD=0

source ${SCRIPT_DIR}/include.sh

# Start by printing the header and parsing the program arguments.
_printHeader
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --skip-update)  SKIP_UPDATES=1 ;;
        --skip-build)   SKIP_BUILD=1 ;;
        --help)         _showHelp; exit 0 ;;
        *)              echo "Error: Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

# Check for program updates.
if [ ${SKIP_UPDATES} -eq 0 ]; then
    bash ${SCRIPT_DIR}/check_for_updates.sh
fi

# Generate the JAR.
if [ ${SKIP_BUILD} -eq 0 ]; then
    bash ${SCRIPT_DIR}/create_jar.sh
    if [ $? -ne 0 ]; then
        echo "Error: There was a problem creating the JAR file for this program. Exiting..."
        exit 1
    fi
fi

# Finally start the program.
echo "Starting program..."
java -jar ${CURRENT_DIR}/bin/MessageBox.jar