#!/bin/bash -e
find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
npm install --no-progress
./gulp $@
