#!/bin/bash

echo "Checking for program updates..."

# Check if we're connected to a repo.
git status
if [ $? -ne 0 ]; then
    echo "Error: This program is not connect to a repository. Cannot check for updates..."
    exit 1
fi

# Next, check what branch we're on.
CURRENT_BRANCH=$(git branch –show-current)
if [ "${CURRENT_BRANCH}" == "main" ]; then
    echo "Warning: The repository is not on an official version. Keeping HEAD updated with \"main\"..."
else
    
fi