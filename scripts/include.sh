#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

function _printHeader() {   
    echo '
  ___  _           _      ___  ___                                ______           
 / _ \| |         ( )     |  \/  |                                | ___ \          
/ /_\ \ | _____  _|/ ___  | .  . | ___  ___ ___  __ _  __ _  ___  | |_/ / _____  __
|  _  | |/ _ \ \/ / / __| | |\/| |/ _ \/ __/ __|/ _` |/ _` |/ _ \ | ___ \/ _ \ \/ /
| | | | |  __/>  <  \__ \ | |  | |  __/\__ \__ \ (_| | (_| |  __/ | |_/ / (_) >  < 
\_| |_/_|\___/_/\_\ |___/ \_|  |_/\___||___/___/\__,_|\__, |\___| \____/ \___/_/\_\
                                                       __/ |                       
                                                      |___/                        
'
    echo "                A Personalized Message Box for Alexandra Hamill"
    echo "                       Created By: Bryan Muscedere"
    echo ""
}

function _showHelp() {
    echo "Here's the help."
}

function _parseArguments() {
    while [[ "$#" -gt 0 ]]; do
        case $1 in
            --skip-update)  SKIP_UPDATE=1 ;;
            --skip-build)   SKIP_BUILD=1 ;;
            --help)         _showHelp; exit 0 ;;
            *)              echo "Error: Unknown parameter passed: $1"; exit 1 ;;
        esac
        shift
    done
}

function _getOS() {
    UNAME="$(uname -s)"
    case "${UNAME}" in
        Linux*)     MACHINE=Linux;;
        Darwin*)    MACHINE=Mac;;
        CYGWIN*)    MACHINE=Cygwin;;
        MINGW*)     MACHINE=MinGw;;
        *)          MACHINE="UNKNOWN:${UNAME}"
    esac

    echo ${MACHINE}
}

function _isWindows() {
    OS=$(_getOS)
    if [ "${OS}" == "Cygwin" ] || [ "${OS}" == "MinGw" ]; then
        return 1
    fi

    return 0
}

function _info() {
    echo -e "${BLUE}${1}${NC}"
}

function _success() {
    echo -e "${GREEN}${1}${NC}"
}

function _warning() {
    echo -e "${YELLOW}${1}${NC}"
}

function _error() {
    echo -e "${RED}${1}${NC}"
}

function _getLatestRelease() {
    curl --silent "https://api.github.com/repos/$1/releases/latest" |
        grep '"tag_name":' |                                           
        sed -E 's/.*"([^"]+)".*/\1/'                                    
}
