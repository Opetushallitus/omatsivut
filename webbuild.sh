#!/bin/bash -e
find node_modules -name ".git" | rev | cut -c6- | rev | xargs rm -fr
(cd hakemuseditori && npm install && ./gulp)
npm install
./gulp $@
