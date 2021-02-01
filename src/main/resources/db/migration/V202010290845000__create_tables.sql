drop table if exists sessions;
create table sessions
(
    id              uuid primary key  not null,
    ticket          character varying not null,
    hetu            character varying not null,
    oppija_numero   character varying not null,
    oppija_nimi     character varying not null,
    viimeksi_luettu timestamptz       not null default now()
);

create index sessions_timestamp on sessions (viimeksi_luettu);

/* based on https://github.com/kagkarlsson/db-scheduler/blob/master/src/test/resources/postgresql_tables.sql */
drop table if exists scheduled_tasks;
create table scheduled_tasks
(
    task_name            text                     not null,
    task_instance        text                     not null,
    task_data            bytea,
    execution_time       timestamp with time zone not null,
    picked               BOOLEAN                  not null,
    picked_by            text,
    last_success         timestamp with time zone,
    last_failure         timestamp with time zone,
    consecutive_failures INT,
    last_heartbeat       timestamp with time zone,
    version              BIGINT                   not null,
    PRIMARY KEY (task_name, task_instance)
)
