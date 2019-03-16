create table sessions (
    id uuid primary key not null,
    oppija_numero character varying not null,
    viimeksi_luettu timestamptz not null default now()
);
