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
UPDATE_STATUS=${NO_UPDATE}
if [ ${SKIP_UPDATES} -eq 0 ]; then
    _info "Checking for program updates..."
    bash ${SCRIPT_DIR}/check_for_updates.sh
    UPDATE_STATUS=${?}
else
    _warning "Skipping program update check!"
fi
echo ""

# Generate the JAR.
if [ ${SKIP_BUILD} -eq 0 ]; then
    _info "Building the current project..."
    if [ ${UPDATE_STATUS} -eq ${NO_UPDATE} ]; then
        _success "Program was not updated. No need to build!"
    else
        bash ${SCRIPT_DIR}/create_jar.sh
        if [ $? -ne 0 ]; then
            _error "Error: There was a problem creating the JAR file for this program. Exiting..."
            exit 1
        fi
    fi
else
    _warning "Skipping program build!"
fi
echo ""

# Finally start the program.
_info "Starting program..."
while true; do
    java -jar ${CURRENT_DIR}/bin/MessageBox.jar
    while true; do
        read -p "It appears the message box exited. Do you want to restart it? (Y/N): " yn
        case $yn in
            [Yy]* ) EXIT_REQ=0; break;;
            [Nn]* ) EXIT_REQ=1; break;;
            * ) _error "Please answer yes or no.";;
        esac
    done

    if [ ${EXIT_REQ} -eq 1 ]; then
        _info "Goodbye! Dropping to shell."
        exit 0
    fi
done