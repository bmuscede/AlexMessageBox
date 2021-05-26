#!/bin/bash

CURRENT_DIR="$(readlink -f $(dirname ${BASH_SOURCE[0]})/..)"
SCRIPT_DIR="${CURRENT_DIR}/scripts"
source ${SCRIPT_DIR}/include.sh

# Check if we're connected to a repo.
git status &> /dev/null
if [ $? -ne 0 ]; then
    _error "Error: This program is not connect to a repository. Cannot check for updates..."
    exit 1
fi

# Next, check what branch we're on.
CURRENT_BRANCH=$(git branch --show-current)
if [ "${CURRENT_BRANCH}" == "main" ]; then
    _warning "Warning: The repository is not on an official version. Keeping HEAD updated with \"main\"..."
    git remote update &> /dev/null

    UPSTREAM=${1:-'@{u}'}
    LOCAL=$(git rev-parse @)
    REMOTE=$(git rev-parse "$UPSTREAM")
    BASE=$(git merge-base @ "$UPSTREAM")

    if [ $LOCAL = $REMOTE ]; then
        _success "No updates found, continuing..."
    elif [ $LOCAL = $BASE ]; then
        _info "Updates found! Updating program..."

        # Start by purging all local changes then we pull..
        git reset --hard &> /dev/null
        git pull &> /dev/null
        _success "Done! Updates succeeded."
        exit ${UPDATE_SUCCESS}
    else
        _warning "Warning: Unknown update condition detected. Continuing without updating..."
    fi
else
    git remote update &> /dev/null
    RELEASE_BRANCH=$(_getLatestRelease)

    if [ ${RELEASE_BRANCH} != ${CURRENT_BRANCH} ]; then
        _info "Updates found! Updating the program..."

        # Start by purging local changes then we pull...
        git reset --hard &> /dev/null
        git checkout ${RELEASE_BRANCH} &> /dev/null
        _success "Done! Updates succeeded."
        exit ${UPDATE_SUCCESS}
    else
        _success "No updates found, continuing..."
    fi
fi

exit ${NO_UPDATE}