# Weup – budík so stávkou

Pracovný názov appky je **Weup**, interný názov `sk.weup.app` (dá sa zmeniť, kým appku nezverejníme v Google Play).

## Ako s majiteľom projektu komunikovať

- Majiteľ nie je programátor. Všetko vysvetľuj jednoducho **po slovensky**, bez anglických výrazov („prihlásenie“, nie „login“).
- Súbory, priečinky, triedy, tabuľky a stĺpce pomenúvaj **po slovensky bez diakritiky** (`prihlasenie/PrihlasenieObrazovka.kt`). Po anglicky ostávajú iba názvy, ktoré predpisuje systém (`AndroidManifest.xml`, `build.gradle.kts`, `Activity`).
- Komentáre v kóde a popisy zmien (pull requestov) píš po slovensky.
- Rozhodnutia od majiteľa pýtaj dotazníkom s odporúčanou možnosťou.
- Rieš funkčnosť appky, nie ceny služieb, firmu ani právo.
- Jedna funkcia = jedna vetva = jeden pull request s popisom, čo sa urobilo a ako to vyskúšať.

## Ako appka funguje

Tri časti: **appka v telefóne**, **server (Supabase)** a **webová administrácia**.
Platby rieši Stripe (počas vývoja iba testovací režim), fotky overuje AI model volaný zo servera.

**Hlavné pravidlo:** o peniazoch a o tom, či človek vstal, rozhoduje **iba server**. Telefón zvoní, zbiera dôkazy a posiela ich na server. Čas sa berie vždy zo servera. Ak platný dôkaz nepríde do termínu, stávka prepadne (aj keď ráno nejde internet – ostáva odvolanie).

## Rozhodnutia o fungovaní (schválené 11. 10. 2026)

### Ráno
- Dva dôkazy za sebou, každý iným spôsobom. Druhý začne až po splnení prvého. Každý má vlastný časový limit v minútach.
- Spôsoby dôkazu (vyberá sa večer, každý má vysvetľujúcu animáciu):
  - **QR kód** – unikátny pre používateľa, vytlačí si ho a nalepí napr. na chladničku.
  - **Fotka predmetu** – človek večer napíše, čo odfotí (napr. „pes“). AI na serveri overí, či je to na fotke. Príliš všeobecné zadanie (strop, stena, ruka) odmietne už večer. Fotka sa po overení zmaže.
  - **Drepy pred kamerou** – appka počíta opakovania priamo v telefóne (ML Kit, rozpoznanie pózy). Video sa nikam neposiela.
  - **Strážca** – mobil vo vrecku. Kým sa človek hýbe, je všetko v poriadku. Ak mobil 1 minútu leží bez pohybu, zazvoní budík. Bez polohy (GPS).
- Iba živá kamera, nič z galérie.
- **Opakované zvonenie:** budík sa dá vypnúť tlačidlom. Potom má človek 1 minútu na začatie úlohy, inak budík zazvoní znova. Ďalšie vypnutie platí už iba 30 sekúnd, a tak stále dookola. Platí pri oboch dôkazoch aj vtedy, keď človek úlohu začne a 1 minútu v nej nespraví pokrok.
- Na zamknutej obrazovke beží živá notifikácia s odpočtom (na Androide 16 živé aktualizácie).

### Peniaze
- Peňaženka: vybrateľné peniaze, bonusový kredit (nevybrateľný) a peniaze zablokované v stávkach.
- Každý pohyb peňazí je záznam v knihe pohybov. Zostatok sa iba počíta, nikdy sa needituje. Sumy sú vždy v centoch (celé čísla).
- Minimálna stávka 4 €, ponuka 4 / 5 / 10 / 20 € alebo vlastná suma.
- Stávka sa berie najprv z bonusového kreditu, potom z vybrateľných peňazí.
- Stávku sa dá zrušiť alebo zmeniť najneskôr 6 hodín pred budíkom.
- Málo peňazí: budík raz zazvoní aj bez stávky. Na druhý deň appka niekoľkokrát upozorní, že zajtra nezazvoní. Ak človek peňaženku nenabije, budík nezazvoní.
- Výber peňazí: iba na pôvodnú kartu, poplatok 0,50 €, 3 – 5 pracovných dní.
- Odvolanie do 14 dní (text + fotka), prvé posúdenie AI, sporné prípady do administrácie.

### Séria a odmeny
- Budík zvoní iba v dni, ktoré si človek nastaví. Dni bez budíka sériu neprerušia.
- **Séria = 31 splnených dní** so stávkou aspoň 4 €. Prepadnutý deň ju vynuluje, schválené odvolanie nie.
- Odmena: **10 € bonusový kredit** (iba na použitie v appke) **alebo predplatné**, ktoré dostane po pozvaní kamaráta, ktorý vloží aspoň 4 €. Pri ponuke odkaz „*Platia podmienky“. Doručenie predplatného zatiaľ ručne cez administráciu.

### Dizajn
- Štýl Bolt / Uber: čisto, veľa voľného miesta, jedno hlavné tlačidlo na obrazovke, veľké písmo pre čas a sumy, zaoblené karty.
- Hlavná farba **sviežo zelená**, inak biela, čierna a sivá. Svetlý aj tmavý režim.
- Jazyky: slovenčina (hlavná), čeština, angličtina. Texty iba v súboroch prekladov, nikdy priamo v kóde.

## Štruktúra repozitára

```
CLAUDE.md          – tento súbor
android/           – Android appka (Kotlin + Jetpack Compose)        [fáza 1, po inštalácii Android Studia]
server/            – Supabase: databáza, testy, serverové funkcie
  supabase/config.toml   – nastavenia servera (prihlásenie, úložisko…)
  supabase/migrations/   – postupné vytváranie databázy, súbory sa nikdy nemenia, iba pribúdajú
  supabase/tests/        – testy databázy (pgTAP)
  supabase/functions/    – serverové funkcie (Deno)
administracia/     – webová administrácia pre majiteľa                [fáza 8]
dokumenty/         – texty pre Google Play, vysvetlenia oprávnení       [fáza 10]
```

### Android
- Zatiaľ jeden modul `app/`, vnútri rozdelený na balíky `jadro/` (dizajn, server) a `funkcie/` (prihlasenie, domov…). Na samostatné moduly ho rozdelíme, keď začne zostavovanie trvať dlho.
- Každá obrazovka: `…Obrazovka.kt` iba kreslí, `…Riadic.kt` rozhoduje, čo sa zobrazí (testuje sa).
- Spoločné časti (pripojenie na server, repozitáre) sa vytvárajú v `Zavislosti.kt`. Obrazovky pracujú s rozhraniami (napr. `PrihlasenieRepozitar`), aby ich testy vedeli nahradiť skúšobnými.
- Adresa servera a jeho verejný kľúč sú v `android/server.properties`. Tajné kľúče v appke nikdy nie sú.
- Minimálne Android 10, cieľový Android 16. Nástroje: Gradle 9.8.1, Android Gradle Plugin 9.4.1, Kotlin 2.4.21.

## Príkazy

Server beží online na **supabase.com**, projekt na vývoj `weup-vyvoj` (kód `yoojmhtbtrsaonkphdvs`, región Frankfurt). Miestny server v Dockeri nepoužívame. Výnimka: `npm run testy` si na chvíľu stiahne malý testovací nástroj cez Docker (neskôr presunieme testy na GitHub). V PowerShelli na Windowse treba písať `npm.cmd` / `npx.cmd` (obyčajné `npm` Windows blokuje). Príkazy v priečinku `server/`:

| Príkaz | Čo robí |
|---|---|
| `npm install` | stiahne nástroje servera (raz) |
| `npm run prihlas` | prihlási nástroje do účtu Supabase (raz, otvorí prehliadač) |
| `npm run pripoj -- --project-ref <kód projektu>` | prepojí priečinok s projektom na supabase.com (raz) |
| `npm run nahraj-databazu` | nahrá nové súbory z `migrations/` do databázy online |
| `npm run testy` | spustí testy databázy na online projekte (testy po sebe všetko vrátia späť) |
| `npm run nahraj-funkcie` | nahrá serverové funkcie |

Android (v priečinku `android/`, v PowerShelli):

| Príkaz | Čo robí |
|---|---|
| `.\gradlew.bat assembleDebug` | zostaví appku (súbor `app/build/outputs/apk/debug/app-debug.apk`) |
| `.\gradlew.bat testDebugUnitTest` | spustí testy logiky |
| `.\gradlew.bat installDebug` | nainštaluje appku do zapnutého emulátora |

Java sa berie z Android Studia: `JAVA_HOME=C:\Program Files\Android\Android Studio\jbr`. Emulátor zapína majiteľ v Android Studiu (Device Manager).

## Stav fáz

| Fáza | Obsah | Stav |
|---|---|---|
| 1 | Kostra projektu, server, prihlásenie (e-mail, Google, Apple) | **prebieha** – server online (7 testov), Android appka s prihlásením a registráciou e-mailom (10 testov); chýba Google (projekt v Google Cloud) a Apple (neskôr) |
| 2 | Budík cez celú obrazovku + opakované zvonenie + dôkaz QR kódom | čaká |
| 3 | Peňaženka, Stripe (test), stávky, kniha pohybov, rozhodovanie servera | čaká |
| 4 | Fotka predmetu s AI, drepy, Strážca | čaká |
| 5 | Živá notifikácia a widgety | čaká |
| 6 | Ranný blok aplikácií | čaká |
| 7 | Týždenný budík, séria 31 dní, odmeny, pozvánky | čaká |
| 8 | Výber peňazí, odvolania, administrácia | čaká |
| 9 | Úvodná obrazovka s animáciou, onboarding, preklady CZ/EN | čaká |
| 10 | Príprava na Google Play | čaká |
| neskôr | iPhone verzia | čaká |

Testovanie zatiaľ na emulátore. Budík na zamknutom telefóne, senzory a drepy sa overia neskôr na skutočnom telefóne.
