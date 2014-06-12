#!/bin/bash

mkdir -p fixtures/application||0
mkdir -p fixtures/applicationSystem||0

mongoexport "$@" -c applicationSystem --query '{_id:"1.2.246.562.5.2013080813081926341927"}' --out fixtures/applicationSystem/2013080813081926341927.json
mongoexport "$@" -c application --query '{oid: "1.2.246.562.11.00000876904"}' --out fixtures/application/00000876904.json
