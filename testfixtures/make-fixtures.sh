#!/bin/bash

mkdir -p application||0
mkdir -p applicationSystem||0

mongoexport "$@" -d hakulomake -c applicationSystem --query '{_id:"1.2.246.562.5.2013080813081926341927"}' --out applicationSystem/2013080813081926341927.json
mongoexport "$@" -d hakulomake -c application --query '{oid: "1.2.246.562.11.00000876904"}' --out application/00000876904.json
