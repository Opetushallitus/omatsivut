create table sessions (
    id uuid primary key not null,
    oppija_numero character varying not null,
    viimeksi_luettu timestamptz not null default now()
);

create index sessions_oppija_numero_idx on sessions (oppija_numero);
