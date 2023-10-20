:page/title Slutt å ødelegge tingene mine
:blog-post/published #time/ldt "2024-10-20T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:bakoverkompatibilitet :produktivitet]
:blog-post/description

IT-bransjen elsker tilsynelatende å gjøre gamle ting om igjen. I vårt evige jag
på det perfekte API-et kaster vi ut ting som fungerer fint bare for å servere
nye ting som overflatisk ser annerledes ut, men i bunn og grunn gjør det samme.
Det koster mer enn det smaker.

:open-graph/description

I vårt evige jag på det perfekte API-et kaster vi ut ting som fungerer fint bare
for å servere nye ting som ser annerledes ut, men i bunn og grunn gjør det
samme. Det koster mer enn det smaker.

:blog-post/body

Har du stoppet verdiskapende arbeid for å gjøre endringer på kode du egentlig
var ferdig med, for å kunne oppdatere et bibliotek? Eller har du kanskje slettet
`node_modules`, bare for å oppleve at det ikke lenger er mulig å starte appen
din? Velkommen i klubben. Tenk om vi slapp å søle bort tida vår på sånt.

## API-forbedringer, eller?

Denne uka satt vi oss til å oppdatere
[clean-css](https://github.com/clean-css/clean-css) fra versjon 3.x til siste
versjon, 5.3. Differansen på de tallene er nok til å fylle den mest erfarne
utvikler med frykt. Og visst var det [inkompatible
endringer](https://github.com/clean-css/clean-css#important-40-breaking-changes).
La oss se på et par av dem:

>- splits `inliner: { request: ..., timeout: ... }` option into `inlineRequest`
>  and `inlineTimeout` options
>- renames `keepSpecialComments` to `specialComments`

Dette er en vanlig form for endring. Det er utvilsomt gjort for å forbedre
API-et. Konsekvensen for meg som eksisterende bruker er at jeg må gå gjennom all
koden som bruker biblioteket for å speile endringene. Til slutt fungerer koden
min som før -- hvis jeg er heldig.

Noen vil kanskje argumentere for at API-endringen var helt nødvendig. Kanskje
var det gamle API-et forvirrende for nye brukere, eller inkonsekvent. Det er
godt mulig at `specialComments` er en kjempeforbedring over
`keepSpecialComments`, men det ga ikke meg noen som helst verdi - snarere tvert
imot, spiste det bare av tiden min.

Dette var bare et lite bibliotek -- de som bruker mer omfattende rammeverk, så
som Rails, Spring og React, kjenner panikkangsten melde seg når en ny
majorversjon annonseres. Jeg har selv brukt et månedsverk på å bytte ut Rails 2
med Rails 3, og jeg har sett folk gi opp majorversjon-oppdatering av Spring Boot
etter flere dagers arbeid -- på en relativt fersk kodebase.

## Endringenes kostnad

Hva er egentlig problemet med inkompatible endringer?

I beste fall bruker du litt tid på å gå over kode -- som strengt tatt var ferdig
-- for å oppdatere bruk av biblioteket. Når du er i mål har du en ny versjon,
men appen din er den samme som før. Så i beste fall "bare" kaster du bort litt
tid.

Noen ganger er det ikke tid til å sitte å knote med kode som allerede er ferdig.
Da må du hoppe over oppdateringen. Det kan igjen bety at du går glipp av
kritiske sikkerhetsfikser, eller ikke får brukt nye features som kunne ha
hjulpet deg med det videre arbeidet.

Det aller verste utfallet er når du endrer på koden din for å få inn
oppdateringen, men du innfører nye feil mens du holder på. Da har du fått
oppdatert biblioteket, men i praksis rasert din egen løsning i prossessen. Been
there, done that, for å si det sånn.

## Deprekering og semantisk versjonering

Mange av dere sitter kanskje nå og rister på hodet. Vi har [semantisk
versjonering](https://semver.org/) til å sørge for at folk er informert om
inkompatible endringer, og dessuten var det API-et jeg sutret over deprekert i
lang tid.

Verken advarsler om at noe kommer til å brekke, eller et system for å annonsere
at noe har brukket er til mye hjelp for meg. Inkompatible endringer er fortsatt
inkompatible og koster tid og energi fra alle som må gjennom dem. Som [Rich
Hickey sa](https://www.youtube.com/watch?v=oyLBGkS5ICk): "breaking changes are
broken".

## Hvordan kan jeg da forbedre API-er?

Ok, så vi skal ikke fjerne eller endre eksisterende funksjonalitet, men vi kan
fortsatt legge til ting. Ønsker du å endre på en signatur, eller bytte navn på
en funksjon? Lag en ny, da vel! Og la den gamle være i fred. Eller la den gamle
kalle den nye. Det er også fullt mulig å ta ting ut av offisiell dokumentasjon
for å unngå at nye brukere roter seg inn i API-er vi skammer oss over.

Den clean-css-oppdateringen jeg snakket om innledningsvis ble gjort i
[Optimus](https://github.com/magnars/optimus). Der ble også API-et forbedret,
men på en måte som gjør at eksisterende brukere kan oppgradere uten å endre en
eneste linje i egen kode. Dersom Optimus kan dekke over inkompatible endringer i
dette API-et så kunne søren meg clean-css gjort det samme. Her fra README-en til
Optimus:

> In earlier versions of Optimus, this was a curated set of options. These old
> options will still work (we're trying not to break your stuff), but it is
> probably a good idea to take a look at all the available settings in
> clean-css.

Endringen som ble gjort i Optimus besto av:

- Et nytt API - til glede for nye brukere
- Bittelitt kode som mappet gamle options til nye
- Dokumentasjon av gamle options ble flyttet ut av allfarvei og lenket opp som
  "legacy options"

## Applikasjoner vs biblioteker

Det er en viktig forskjell på biblioteker til allmenn bruk og applikasjonskode.
For å unngå at applikasjonskode "råtner på rot" må man tørre å rydde og endre i
takt med ny innsikt. Den viktige forskjellen er at dette er kode som kun brukes
internt i teamet, så endringer treffer ikke et ukjent antall utviklere der ute.

Når du jobber i et bibliotek som kanskje tusenvis av andre utviklere har basert
sitt arbeid på kan du rett og slett ikke endre og herje som du vil for at ting
skal bli marginalt mer estetisk eller "riktig". Og husk, når du jobber på et
HTTP-API som applikasjonen din eksponerer til verden så bør du operere i
"biblioteksmodus". Med andre ord, ikke lov å brekke API-et.

## Når kan jeg brekke API-et, da?

Ideelt sett: Aldri. I praksis vil jeg si at det er OK å fjerne ting som er
aktivt skadelig. Eksempelvis har Github Actions nå deprekert [en feature som
gjør det litt for lett å lekke sensitiv
informasjon fra bygg](https://github.blog/changelog/2022-10-11-github-actions-deprecating-save-state-and-set-output-commands/).
Da tenker jeg det er greit å endre - ja, det koster og svir, men å fortsette å
støtte denne mekanismen kan gi Githubs brukere enda større problemer.

## Hva kan vi gjøre?

Neste gang du må endre eksisterende kode for å oppdatere et bibliotek, spør deg
selv: Hvor mye mer arbeid er det å faktisk løse dette problemet selv? Kanskje du
bare kan skrive det du trenger selv, og bli en avhengighet lettere. Hvis ikke
det er aktuelt, kan du kanskje bruke energien din på å bytte til et annet
bibliotek som respekterer tiden din bedre.

Selv har jeg lenge sverget til å kun gjøre bakoverkompatible endringer i egne
open source-prosjekter. For å markere dette har jeg sluttet med semantisk
versjonering, og versjonerer heller bibliotekene med en dato - de er alle
kompatible, og den nyeste er den beste hittil.

Etter at jeg bestemte meg for å slutte med inkompatible endringer har jeg også
brukt mer tid på det opprinnelige designet av nye biblioteker - og prøver dem
grundigere ut før jeg legger på dokumentasjon og slipper dem ut i verden. I
denne prosessen har stort sett alt behov for å gjøre ødeleggende endringer
forsvunnet.

Alle API-ene vårt team skal levere kommer til å følge samme prinsipp, slik at de
som skal bruke dataene våre kan bruke tid på verdiskapning heller enn å pirke på
ting som var ferdig i fjor. Det syns jeg du også skal gjøre.
