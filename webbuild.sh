#!/bin/bash -e
cd omatsivut
find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
# make sure we get latest version
rm -fr node_modules/hakemuseditori
npm install
./gulp $@
