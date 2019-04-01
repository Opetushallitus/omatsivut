drop table if exists sessions;
create table sessions (
    id uuid primary key not null,
    hetu character varying not null,
    oppija_numero character varying not null,
    oppija_nimi character varying not null,
    viimeksi_luettu timestamptz not null default now()
);
