:page/title Git repo – En analyse
:blog-post/author {:person/id :person/mathias}
:blog-post/published #time/ldt "2025-06-18T14:00:00"
:blog-post/tags [:git :data]
:blog-post/description

Hvordan putte et repo sin git historikk inn i datomic for å gjøre nærmere analyse

:blog-post/body

Du vet, Github sine diagrammer på [profil](https://github.com/boosja)-siden? De har jeg alltid sett på og
tenkt: "Det hadde vært gøy å lage flere av disse – se koden sin gang over tid".

Men hvordan får vi til dette? Hvor starter man?

Jo, det vi må gjøre først er å trekke ut dataen vi trenger fra git. Så må vi
putte dataen inn i en database. Ved å gjøre dette får vi alle fordelene en
database gir oss, så som å kjøre spørringer mot dataen. Sist men ikke minst må vi tegne opp
dataen på et eller annet vis.

## Steg 1 - Samle inn dataen vi trenger

For å få innblikk i et repo sin git-historikk kan vi anvende `git log`. Den gir
oss en tekstlig historikk av alt som har skjedd i repo-et siden det ble
opprettet.

Tekst kan vi ikke gjøre så mye med, så vi må parse dataen først. Ved å kjøre
`git log` får vi dette:

```
commit 4582c769dcf751b029f0934bc60afc62df0e9616
Author: Christian Johansen <christian@jaliksom.du>
Date:   Sat Jun 14 10:19:06 2025 +0200

    Oppdater Dataspex

commit eb15e5b76f28ec124944e7fbda5f06e47b6d4572
Author: Magnar Sveen <magnar@ja.serr>
Date:   Fri Jun 13 14:54:54 2025 +0200

    Fullfør enhetliggjøringen av sentinels: submission-sentinel

    Istedenfor å bruke bare form-id for å finne tilbake til en submission i
    sentinelen, så bruker vi nå :form/kind med en optional :form/key. Dette
    tilsvarer da command-sentinel og query-sentinel, og tillater flere instanser av
    samme skjema på samme side.

```

Men dette er ikke så enkelt å parse. Ikke stress, for `git log` støtter å
formatere resultatet. Med flagget `--pretty=format:` kan du legge til hvilke
felter du ønsker og i hvilken form. I tilleg kan du legge til `--numstat` som
gir deg antal linjer lagt til og fjernet for hver fil i hver commit. Da får vi
denne kommandoen:

```zsh
git log --numstat --pretty=format:%H%n%an%n%ae%n%aI%n%cn%n%ce%n%cI%n%s%n%b
```

Feltene vi har her er:

- `%H`: full git sha
- `%an`: forfatter navn
- `%ae`: forfatter epost
- `%aI`: forfatter dato
- `%cn`: committer navn
- `%ce`: committer epost
- `%cI`: committer dato
- `%s`: commit melding
- `%b`: commit beskrivelse
- `%n`: ny linje

<small>
Forskjellen mellom forfatter og committer er f.eks. når du cherrypicker.
Da blir du satt som committer, mens original forfatteren blir bevart.
</small>

Resultatet vi får ut da, blir:

```
4582c769dcf751b029f0934bc60afc62df0e9616
Christian Johansen
christian@jaliksom.du
2025-06-14T10:19:06+02:00
Christian Johansen
christian@jaliksom.du
2025-06-15T22:38:44+02:00
Oppdater Dataspex

2       2       deps.edn
7       2       src/matnyttig/ui/dev.cljs

eb15e5b76f28ec124944e7fbda5f06e47b6d4572
Magnar Sveen
magnar@ja.serr
2025-06-13T14:54:54+02:00
Magnar Sveen
magnar@ja.serr
2025-06-13T14:54:54+02:00
Fullfør enhetliggjøringen av sentinels: submission-sentinel
Istedenfor å bruke bare form-id for å finne tilbake til en submission i
sentinelen, så bruker vi nå :form/kind med en optional :form/key. Dette
tilsvarer da command-sentinel og query-sentinel, og tillater flere instanser av
samme skjema på samme side.

3       3       org/røkla.org
1       1       src/matnyttig/imperative_shell/client/director.cljs
11      9       src/matnyttig/imperative_shell/client/submission_center.cljs
3       14      src/matnyttig/sider/adressevaskside/konkluder_adressevask_skjema.cljc
3       3       src/matnyttig/sider/adressevaskside/sjekk_adresse_skjema.cljc
8       5       src/matnyttig/sider/planlegging_av_tur/ui.cljc
27      15      src/matnyttig/sider/preferanser/ui.cljc
2       2       src/matnyttig/sider/smilefjessteder/ui.cljc
1       1       src/matnyttig/sider/sok/sokeskjema.cljc
1       1       src/matnyttig/sider/sok/ui.cljc
5       5       src/matnyttig/sider/vask_av_opptatt_lokale/konkluder_opptatt_lokale_skjema.cljc
16      9       src/matnyttig/ui/submission_sentinel.cljc

```

Dette er brukbart! 🙌

Med verdiene på hver sin linje blir det veldig enkelt å parse.

## Steg 2 - Parse dataen

Dette er ganske rett frem. Nesten 😅

Vi kommer veldig langt med å splitte på `\n`. Men for den skarpe leseren kan
commit beskrivelsen være over flere linjer. Dette må håndteres på en eller annen
vis. Det samme med linje-endringene.

Men med litt fiks-fakseri ender vi opp med:

```clj
[["4582c769dcf751b029f0934bc60afc62df0e9616"
  "Christian Johansen"
  "christian@jaliksom.du"
  "2025-06-14T10:19:06+02:00"
  "Christian Johansen"
  "christian@jaliksom.du"
  "2025-06-15T22:38:44+02:00"
  "Oppdater Dataspex"
  ""
  [["2" "2" "deps.edn"] ["7" "2" "src/matnyttig/ui/dev.cljs"]]]
 ["eb15e5b76f28ec124944e7fbda5f06e47b6d4572"
  "Magnar Sveen"
  "magnar@ja.serr"
  "2025-06-13T14:54:54+02:00"
  "Magnar Sveen"
  "magnar@ja.serr"
  "2025-06-13T14:54:54+02:00"
  "Fullfør enhetliggjøringen av sentinels: submission-sentinel"
  "Istedenfor å bruke bare form-id for å finne tilbake til en submission i\nsentinelen, så bruker vi nå :form/kind med en optional :form/key. Dette\ntilsvarer da command-sentinel og query-sentinel, og tillater flere instanser av\nsamme skjema på samme side."
  [["3" "3" "\"org/r\\303\\270kla.org\""]
   ["1" "1" "src/matnyttig/imperative_shell/client/director.cljs"]
   ["11" "9" "src/matnyttig/imperative_shell/client/submission_center.cljs"]
   ["3"
    "14"
    "src/matnyttig/sider/adressevaskside/konkluder_adressevask_skjema.cljc"]
   ["3" "3" "src/matnyttig/sider/adressevaskside/sjekk_adresse_skjema.cljc"]
   ["8" "5" "src/matnyttig/sider/planlegging_av_tur/ui.cljc"]
   ["27" "15" "src/matnyttig/sider/preferanser/ui.cljc"]
   ["2" "2" "src/matnyttig/sider/smilefjessteder/ui.cljc"]
   ["1" "1" "src/matnyttig/sider/sok/sokeskjema.cljc"]
   ["1" "1" "src/matnyttig/sider/sok/ui.cljc"]
   ["5"
    "5"
    "src/matnyttig/sider/vask_av_opptatt_lokale/konkluder_opptatt_lokale_skjema.cljc"]
   ["16" "9" "src/matnyttig/ui/submission_sentinel.cljc"]]]]
```

Da er vi bare en mapping-funksjon unna en god datastruktur! 💯

## Steg 3 - Put dataen inn i en database

Ved å putte dataen inn i en database, blir den veldig enkel å jobbe med 💪

Ved å putte dataen inn i Datomic eller Datascript, blir den enda enklere å jobbe
med 🙌

Uansett hvilket diagram vi ønsker oss å tegne, har vi dataen rett ved
fingerspissene og kan forespørre den om hva vi vil på kryss og tvers 🎉

Helt strålende!

Mapping-funksjonen nevnt over tar bare hver item i hver commit og mapper til
en mer medgjørlig datastruktur – og èn databasen vil skjønne. Resultatet av
mappinga ser omtrent sånn ut:

```clj
({:commit/full-hash "4582c769dcf751b029f0934bc60afc62df0e9616"
  :commit/subject "Oppdater Dataspex"
  :commit/body ""
  :commit/author-date #inst "2025-06-14T08:19:06.000-00:00"
  :commit/commit-date #inst "2025-06-15T20:38:44.000-00:00"
  :commit/author {:person/email "christian@jaliksom.du"
                  :person/name "Christian Johansen"}
  :commit/committer {:person/email "christian@jaliksom.du"
                     :person/name "Christian Johansen"}
  :commit/filestats [{:file/name "deps.edn"
                      :file/added 2
                      :file/removed 2}
                     {:file/name "src/matnyttig/ui/dev.cljs"
                      :file/added 7
                      :file/removed 2}]}
 {:commit/full-hash "eb15e5b76f28ec124944e7fbda5f06e47b6d4572"
  :commit/subject "Fullfør enhetliggjøringen av sentinels: submission-sentinel"
  :commit/body "Istedenfor å bruke bare form-id for å finne tilbake til en submission i\nsentinelen så bruker vi nå :form/kind med en optional :form/key. Dette\ntilsvarer da command-sentinel og query-sentinel og tillater flere instanser av\nsamme skjema på samme side."
  :commit/author-date #inst "2025-06-13T12:54:54.000-00:00"
  :commit/commit-date #inst "2025-06-13T12:54:54.000-00:00"
  :commit/author {:person/email "magnar@ja.serr"
                  :person/name "Magnar Sveen"}
  :commit/committer {:person/email "magnar@ja.serr"
                     :person/name "Magnar Sveen"}
  :commit/filestats [{:file/name "org/røkla.org"
                      :file/added 3
                      :file/removed 3}
                     {:file/name "src/matnyttig/imperative_shell/client/director.cljs"
                      :file/added 1
                      :file/removed 1}
                     {:file/name "src/matnyttig/imperative_shell/client/submission_center.cljs"
                      :file/added 11
                      :file/removed 9}
                     {:file/name "src/matnyttig/sider/adressevaskside/konkluder_adressevask_skjema.cljc"
                      :file/added 3
                      :file/removed 14}
                     {:file/name "src/matnyttig/sider/adressevaskside/sjekk_adresse_skjema.cljc"
                      :file/added 3
                      :file/removed 3}
                     {:file/name "src/matnyttig/sider/planlegging_av_tur/ui.cljc"
                      :file/added 8
                      :file/removed 5}
                     {:file/name "src/matnyttig/sider/preferanser/ui.cljc"
                      :file/added 27
                      :file/removed 15}
                     {:file/name "src/matnyttig/sider/smilefjessteder/ui.cljc"
                      :file/added 2
                      :file/removed 2}
                     {:file/name "src/matnyttig/sider/sok/sokeskjema.cljc"
                      :file/added 1
                      :file/removed 1}
                     {:file/name "src/matnyttig/sider/sok/ui.cljc"
                      :file/added 1
                      :file/removed 1}
                     {:file/name "src/matnyttig/sider/vask_av_opptatt_lokale/konkluder_opptatt_lokale_skjema.cljc"
                      :file/added 5
                      :file/removed 5}
                     {:file/name "src/matnyttig/ui/submission_sentinel.cljc"
                      :file/added 16
                      :file/removed 9}]})
```

## Steg 4 – Tegn resten av ugla 🤷‍♂️

Nå har vi alt vi trenger for å gjøre noe med denne dataen, og vi har kommet til
artiklens faktiske formål.

<div class="enhance">
🌶️ THE SPICE 🌶️
</div>

Jeg har valgt å bruke [D3](d3js.org) som er et javascript
visualiseringsbibliotek. Det eksponerer mange funksjoner for å jobbe med data og
bruker kraften av SVG til å tegne opp diagrammer - akkurat som du vil ha dem ✨

Jeg kommer ikke til å gå i dypden med hvordan D3 fungerer. Det er det ikke plass
til her så det får bli en annen bloggpost.

Jeg etterlater dere med antall commits lagt til i Matnyttig hver måned:

<div class="aspect-video bg-white">
<svg viewBox="0 0 1000 500"><g transform="translate(60, 40)"><rect class="bar"
fill="#445bed" x="5.3216374269005655" width="47.89473684210527"
y="389.2909090909091" height="0.7090909090908895"></rect><rect class="bar"
fill="#445bed" x="58.53801169590642" width="47.89473684210527"
y="371.56363636363636" height="18.436363636363637"></rect><rect class="bar"
fill="#445bed" x="111.75438596491227" width="47.89473684210527"
y="293.56363636363636" height="96.43636363636364"></rect><rect class="bar"
fill="#445bed" x="164.97076023391813" width="47.89473684210527"
y="301.3636363636364" height="88.63636363636363"></rect><rect class="bar"
fill="#445bed" x="218.18713450292398" width="47.89473684210527"
y="363.76363636363635" height="26.23636363636365"></rect><rect class="bar"
fill="#445bed" x="271.40350877192986" width="47.89473684210527"
y="349.58181818181816" height="40.418181818181836"></rect><rect class="bar"
fill="#445bed" x="324.6198830409357" width="47.89473684210527"
y="290.0181818181818" height="99.9818181818182"></rect><rect class="bar"
fill="#445bed" x="377.8362573099415" width="47.89473684210527"
y="250.3090909090909" height="139.6909090909091"></rect><rect class="bar"
fill="#445bed" x="431.0526315789474" width="47.89473684210527"
y="141.8181818181818" height="248.1818181818182"></rect><rect class="bar"
fill="#445bed" x="484.2690058479533" width="47.89473684210527"
y="167.34545454545454" height="222.65454545454546"></rect><rect class="bar"
fill="#445bed" x="537.4853801169592" width="47.89473684210527"
y="248.8909090909091" height="141.1090909090909"></rect><rect class="bar"
fill="#445bed" x="590.7017543859649" width="47.89473684210527"
y="88.63636363636364" height="301.3636363636364"></rect><rect class="bar"
fill="#445bed" x="643.9181286549708" width="47.89473684210527"
y="33.32727272727273" height="356.6727272727273"></rect><rect class="bar"
fill="#445bed" x="697.1345029239767" width="47.89473684210527"
y="44.67272727272726" height="345.32727272727277"></rect><rect class="bar"
fill="#445bed" x="750.3508771929825" width="47.89473684210527"
y="85.09090909090908" height="304.90909090909093"></rect><rect class="bar"
fill="#445bed" x="803.5672514619883" width="47.89473684210527"
y="161.6727272727273" height="228.3272727272727"></rect><rect class="bar"
fill="#445bed" x="856.7836257309942" width="47.89473684210527"
y="368.0181818181818" height="21.9818181818182"></rect><g class="axis"
transform="translate(0, 390)" fill="none" font-size="10"
font-family="sans-serif" text-anchor="middle"><path class="domain"
stroke="currentColor" d="M0,6V0H910V6"></path><g class="tick" opacity="1"
transform="translate(29.2690058479532,0)"><line stroke="currentColor"
y2="6"></line><text fill="currentColor" y="9" dy=".15em" dx="-.8em"
transform="rotate(-45)" style="text-anchor: end;">2024-2</text></g><g
class="tick" opacity="1" transform="translate(82.48538011695905,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-3</text></g><g
class="tick" opacity="1" transform="translate(135.7017543859649,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-4</text></g><g
class="tick" opacity="1" transform="translate(188.91812865497076,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-5</text></g><g
class="tick" opacity="1" transform="translate(242.1345029239766,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-6</text></g><g
class="tick" opacity="1" transform="translate(295.3508771929825,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-7</text></g><g
class="tick" opacity="1" transform="translate(348.56725146198835,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-8</text></g><g
class="tick" opacity="1" transform="translate(401.7836257309942,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2024-9</text></g><g
class="tick" opacity="1" transform="translate(455.00000000000006,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor:
end;">2024-10</text></g><g class="tick" opacity="1"
transform="translate(508.21637426900594,0)"><line stroke="currentColor"
y2="6"></line><text fill="currentColor" y="9" dy=".15em" dx="-.8em"
transform="rotate(-45)" style="text-anchor: end;">2024-11</text></g><g
class="tick" opacity="1" transform="translate(561.4327485380118,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor:
end;">2024-12</text></g><g class="tick" opacity="1"
transform="translate(614.6491228070175,0)"><line stroke="currentColor"
y2="6"></line><text fill="currentColor" y="9" dy=".15em" dx="-.8em"
transform="rotate(-45)" style="text-anchor: end;">2025-1</text></g><g
class="tick" opacity="1" transform="translate(667.8654970760234,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2025-2</text></g><g
class="tick" opacity="1" transform="translate(721.0818713450293,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2025-3</text></g><g
class="tick" opacity="1" transform="translate(774.2982456140351,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2025-4</text></g><g
class="tick" opacity="1" transform="translate(827.514619883041,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor: end;">2025-5</text></g><g
class="tick" opacity="1" transform="translate(880.7309941520468,0)"><line
stroke="currentColor" y2="6"></line><text fill="currentColor" y="9" dy=".15em"
dx="-.8em" transform="rotate(-45)" style="text-anchor:
end;">2025-6</text></g></g><g class="axis" fill="none" font-size="10"
font-family="sans-serif" text-anchor="end"><path class="domain"
stroke="currentColor" d="M-6,390H0V0H-6"></path><g class="tick" opacity="1"
transform="translate(0,390)"><line stroke="currentColor" x2="-6"></line><text
fill="currentColor" x="-9" dy="0.32em">0</text></g><g class="tick" opacity="1"
transform="translate(0,354.54545454545456)"><line stroke="currentColor"
x2="-6"></line><text fill="currentColor" x="-9" dy="0.32em">50</text></g><g
class="tick" opacity="1" transform="translate(0,319.09090909090907)"><line
stroke="currentColor" x2="-6"></line><text fill="currentColor" x="-9"
dy="0.32em">100</text></g><g class="tick" opacity="1"
transform="translate(0,283.6363636363636)"><line stroke="currentColor"
x2="-6"></line><text fill="currentColor" x="-9" dy="0.32em">150</text></g><g
class="tick" opacity="1" transform="translate(0,248.1818181818182)"><line
stroke="currentColor" x2="-6"></line><text fill="currentColor" x="-9"
dy="0.32em">200</text></g><g class="tick" opacity="1"
transform="translate(0,212.72727272727272)"><line stroke="currentColor"
x2="-6"></line><text fill="currentColor" x="-9" dy="0.32em">250</text></g><g
class="tick" opacity="1" transform="translate(0,177.27272727272728)"><line
stroke="currentColor" x2="-6"></line><text fill="currentColor" x="-9"
dy="0.32em">300</text></g><g class="tick" opacity="1"
transform="translate(0,141.8181818181818)"><line stroke="currentColor"
x2="-6"></line><text fill="currentColor" x="-9" dy="0.32em">350</text></g><g
class="tick" opacity="1" transform="translate(0,106.36363636363636)"><line
stroke="currentColor" x2="-6"></line><text fill="currentColor" x="-9"
dy="0.32em">400</text></g><g class="tick" opacity="1"
transform="translate(0,70.90909090909089)"><line stroke="currentColor"
x2="-6"></line><text fill="currentColor" x="-9" dy="0.32em">450</text></g><g
class="tick" opacity="1" transform="translate(0,35.45454545454547)"><line
stroke="currentColor" x2="-6"></line><text fill="currentColor" x="-9"
dy="0.32em">500</text></g><g class="tick" opacity="1"
transform="translate(0,0)"><line stroke="currentColor" x2="-6"></line><text
fill="currentColor" x="-9" dy="0.32em">550</text></g></g><text class="title"
x="355" y="-10">Commits per month</text></g></svg>
</div>

Mer spennende ting kommer, men enn så lenge – [her er koden så
langt](https://github.com/boosja/repolyzer)

Med denne, kanskje litt anti-klimatiske, avslutningen, da var poenget her ikke å
vise kule innsikter i kodebasen vår eller imponerende grafer, men rettere
hvordan man kan få tak i dataen under panseret til git og gjøre den om til
en deilig, smibar masse. Og da ligger mulighetene helt oppe.

## Inspirasjonen og granskningen bak

- [Codeq](https://github.com/Datomic/codeq)
- [Git of Theseus](https://github.com/erikbern/git-of-theseus)
- [Github sitt GraphQL-API](https://graphql.org/)
