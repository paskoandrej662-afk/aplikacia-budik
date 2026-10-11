-- Testy fázy 1: profil vznikne pri registrácii a cudzí profil nie je vidieť ani meniť.
begin;
-- Testy bežia ako správca databázy (aj keď ich spúšťa dočasný účet nástroja Supabase).
set local role postgres;
create extension if not exists pgtap with schema extensions;
-- Nástroj na testy (pgTAP) môže byť v inej schéme, preto ju pridáme do cesty hľadania.
select set_config('search_path', current_setting('search_path') || ', ' || n.nspname, true)
  from pg_extension e join pg_namespace n on n.oid = e.extnamespace where e.extname = 'pgtap';
select plan(7);

-- Dvaja skúšobní používatelia.
insert into auth.users (id, email, raw_user_meta_data)
values
  ('11111111-1111-1111-1111-111111111111', 'anna@skuska.sk', '{"meno": "Anna", "jazyk": "cs"}'),
  ('22222222-2222-2222-2222-222222222222', 'boris@skuska.sk', '{}');

select is(
  (select meno from public.profily where id = '11111111-1111-1111-1111-111111111111'),
  'Anna', 'Po registrácii sa vytvorí profil s menom');

select is(
  (select jazyk from public.profily where id = '22222222-2222-2222-2222-222222222222'),
  'sk', 'Bez zvoleného jazyka je slovenčina');

-- Odteraz sme prihlásení ako Anna.
set local role authenticated;
set local request.jwt.claims = '{"sub": "11111111-1111-1111-1111-111111111111", "role": "authenticated"}';

select is((select count(*) from public.profily)::int, 1, 'Anna vidí iba svoj profil');

update public.profily set meno = 'Hacker' where id = '22222222-2222-2222-2222-222222222222';
set local role postgres;
select is(
  (select meno from public.profily where id = '22222222-2222-2222-2222-222222222222'),
  null, 'Anna nezmení cudzí profil');

set local role authenticated;
select throws_ok(
  $$update public.profily set kod_pozvanky = 'VLASTNY' where id = '11111111-1111-1111-1111-111111111111'$$,
  '42501', null, 'Kód pozvánky si používateľ zmeniť nesmie');

select lives_ok(
  $$insert into public.suhlasy (pouzivatel_id, druh, verzia, udeleny)
    values ('11111111-1111-1111-1111-111111111111', 'podmienky', '1', true)$$,
  'Anna si zapíše svoj súhlas');

select throws_ok(
  $$insert into public.suhlasy (pouzivatel_id, druh, verzia, udeleny)
    values ('22222222-2222-2222-2222-222222222222', 'podmienky', '1', true)$$,
  '42501', null, 'Anna nezapíše súhlas za niekoho iného');

select * from finish();
rollback;
