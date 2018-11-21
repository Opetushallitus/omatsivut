#!/usr/bin/env bash
eval "$(docker-machine env dockerVM)"
docker run -d -p 5432:5432 postgres
while ! echo exit | nc omatsivutdb 5432; do sleep 10; done
psql -homatsivutdb -p5432 -Upostgres postgres -c "CREATE ROLE OPH;"
psql -homatsivutdb -p5432 -Upostgres postgres -c "CREATE DATABASE omatsivut;"
psql -homatsivutdb -p5432 -Upostgres postgres -c "CREATE DATABASE omatsivuttest;"
