-- Fáza 1: profily používateľov, súhlasy a zariadenia.
-- Každá tabuľka má zapnuté riadkové zabezpečenie (RLS): používateľ vidí iba svoje údaje.

-- Profil vznikne automaticky pri registrácii (pozri funkciu vytvor_profil nižšie).
create table public.profily (
  id            uuid primary key references auth.users (id) on delete cascade,
  meno          text check (char_length(meno) <= 50),
  jazyk         text not null default 'sk' check (jazyk in ('sk', 'cs', 'en')),
  -- Odpovede z úvodného kvízu. Každú otázku sa dá preskočiť, preto je všetko nepovinné.
  kviz          jsonb not null default '{}'::jsonb,
  -- Kód, ktorým človek pozýva kamarátov (odmena za sériu).
  kod_pozvanky  text unique not null default upper(substr(md5(gen_random_uuid()::text), 1, 8)),
  vytvorene     timestamptz not null default now(),
  upravene      timestamptz not null default now()
);

alter table public.profily enable row level security;

create policy "Vidím svoj profil" on public.profily
  for select using (auth.uid() = id);

-- Upraviť sa dá iba meno, jazyk a kvíz. Kód pozvánky mení iba server.
create policy "Upravujem svoj profil" on public.profily
  for update using (auth.uid() = id) with check (auth.uid() = id);

revoke update on public.profily from authenticated;
grant update (meno, jazyk, kviz) on public.profily to authenticated;

-- Pri každej úprave sa zapíše čas úpravy.
create function public.nastav_cas_upravy()
returns trigger language plpgsql as $$
begin
  new.upravene := now();
  return new;
end;
$$;

create trigger profily_cas_upravy
  before update on public.profily
  for each row execute function public.nastav_cas_upravy();

-- Po registrácii (e-mail, Google aj Apple) sa automaticky vytvorí profil.
create function public.vytvor_profil()
returns trigger language plpgsql security definer set search_path = '' as $$
begin
  insert into public.profily (id, meno, jazyk)
  values (
    new.id,
    nullif(new.raw_user_meta_data ->> 'meno', ''),
    coalesce(nullif(new.raw_user_meta_data ->> 'jazyk', ''), 'sk')
  );
  return new;
end;
$$;

create trigger po_registracii
  after insert on auth.users
  for each row execute function public.vytvor_profil();


-- Súhlasy podľa GDPR. Nič sa neprepisuje: odvolanie súhlasu je nový záznam.
create table public.suhlasy (
  id              bigint generated always as identity primary key,
  pouzivatel_id   uuid not null references auth.users (id) on delete cascade,
  druh            text not null check (druh in ('podmienky', 'ochrana_sukromia', 'marketing')),
  verzia          text not null,
  udeleny         boolean not null,
  cas             timestamptz not null default now()
);

create index suhlasy_pouzivatel on public.suhlasy (pouzivatel_id, druh, cas desc);

alter table public.suhlasy enable row level security;

create policy "Vidím svoje súhlasy" on public.suhlasy
  for select using (auth.uid() = pouzivatel_id);

create policy "Zapisujem svoje súhlasy" on public.suhlasy
  for insert with check (auth.uid() = pouzivatel_id);


-- Telefóny používateľa, aby mu server vedel poslať správu (Firebase Cloud Messaging).
create table public.zariadenia (
  id              bigint generated always as identity primary key,
  pouzivatel_id   uuid not null references auth.users (id) on delete cascade,
  platforma       text not null check (platforma in ('android', 'ios')),
  token_sprav     text not null unique,
  naposledy       timestamptz not null default now()
);

alter table public.zariadenia enable row level security;

create policy "Spravujem svoje zariadenia" on public.zariadenia
  for all using (auth.uid() = pouzivatel_id) with check (auth.uid() = pouzivatel_id);
