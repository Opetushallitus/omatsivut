#!/usr/bin/env bash

set -euo pipefail

echo "Configuring PostgreSQL to log database modification statements…"

cat <<END >> /etc/postgresql/postgresql.conf
log_destination = 'stderr'
log_line_prefix = '%t %u '
log_statement = 'mod'
log_timezone = 'Europe/Helsinki'
max_connections = 20
shared_buffers = 128MB
datestyle = 'iso, mdy'
timezone = 'Europe/Helsinki'
lc_messages = 'en_US.UTF-8'
lc_monetary = 'en_US.UTF-8'
lc_numeric = 'en_US.UTF-8'
lc_time = 'en_US.UTF-8'
default_text_search_config = 'pg_catalog.english'
fsync = off
synchronous_commit = off
full_page_writes = off
END