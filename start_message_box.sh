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

source ${SCRIPT_DIR}/include.sh

# Start by printing the header and parsing the program arguments.
_printHeader

# Check for program updates.
if [ ${SHOULD_CHECK_UPDATES} -eq 1 ]; then
    source ${SCRIPT_DIR}/check_for_updates.sh

fi

# Generate the JAR.

# Finally start the program.