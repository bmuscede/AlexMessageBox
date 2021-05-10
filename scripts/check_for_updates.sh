#!/bin/bash

function _getLatestRelease() {
    curl --silent "https://api.github.com/repos/$1/releases/latest" |
        grep '"tag_name":' |                                           
        sed -E 's/.*"([^"]+)".*/\1/'                                    
}

echo "Checking for program updates..."

# Check if we're connected to a repo.
git status &> /dev/null
if [ $? -ne 0 ]; then
    echo "Error: This program is not connect to a repository. Cannot check for updates..."
    exit 1
fi

# Next, check what branch we're on.
CURRENT_BRANCH=$(git branch --show-current)
if [ "${CURRENT_BRANCH}" == "main" ]; then
    echo "Warning: The repository is not on an official version. Keeping HEAD updated with \"main\"..."
    git remote update &> /dev/null

    UPSTREAM=${1:-'@{u}'}
    LOCAL=$(git rev-parse @)
    REMOTE=$(git rev-parse "$UPSTREAM")
    BASE=$(git merge-base @ "$UPSTREAM")

    if [ $LOCAL = $REMOTE ]; then
        echo "No updates found, continuing..."
    elif [ $LOCAL = $BASE ]; then
        echo "Updates found! Updating program..."

        # Start by purging all local changes then we pull..
        git reset --hard &> /dev/null
        git pull &> /dev/null
        echo "Done! Updates succeeded."
    else
        echo "Warning: Unknown update condition detected. Continuing without updating..."
    fi
else
    git remote update &> /dev/null
    RELEASE_BRANCH=$(_getLatestRelease)

    if [ ${RELEASE_BRANCH} != ${CURRENT_BRANCH} ]; then
        echo "Updates found! Updating the program..."

        # Start by purging local changes then we pull...
        git reset --hard &> /dev/null
        git checkout ${RELEASE_BRANCH} &> /dev/null
    else
        echo "No updates found, continuing..."
    fi
fi