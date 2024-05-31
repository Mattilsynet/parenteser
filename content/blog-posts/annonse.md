:page/title Har smilefjesordning, søker entusiast for funksjonell programmering
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-03-20T09:00:00"
:blog-post/tags [:samarbeid]
:blog-post/description

Vårt lille team ser etter sin tredje utvikler. Kan det være deg?

:blog-post/body

Små, selvstyrte produktteam er veien til god software - også i staten. Derfor
har Mattilsynets utviklingsavdeling vært gjennom en stor omstilling de siste
årene for å komme nettopp dit.

Teamet vårt består i dag av to utviklere, en designer, en jurist og en
produkteier. Har du lyst til å være den tredje utvikleren?

Ikke svar enda! Først, litt om hvordan vi jobber.

## Vår arbeidsform

Vi jobber sammen om å lage systemene våre. Parprogrammering er en naturlig del
av dagen vår - særlig i problemløsningsfasen. Vi setter oss ikke i hver vår krok
og fyrer av Pull Requests på hverandre. Istedenfor jobber vi frem løsningene i
fellesskap, og så går vi hvert til vårt og drar ting i land når formen er kjent.

Koden vi skriver går rett ut i prod, hver gang vi pusher, så fremt testene
passerer. Det samme gjelder features som ikke skal lanseres enda, vi lar de bare
ligge skjult til de er klare. Det er ikke alltid denne prosessen er feilfri, men
med hyppige leveranser følger også raske bugfikser, man må bare være litt på
ballen. Vi følger med i prod!

## Våre verktøy

Håper du er glad i parenteser. For 12 år siden oppdaget vi Clojure, og har aldri
sett oss tilbake. Clojure (og ClojureScript!) gir oss data-drevet, funksjonell
programmering og et REPL som lar oss bygge systemet "fra innsiden". Kombinert
med databasen Datomic har vi en funksjonell gavepakke som gir og gir.

Vi bygger systemene våre på arkitekturen Funksjonell kjerne/imperativt skall.
Det betyr kort og godt at vi implementerer domenelogikken - mesteparten av
systemet - med "pure functions", og holder I/O og andre bevegelige deler på
utsiden.

Systeme våre kjører i GCP og er satt opp med Terraform. Også her nyter vi stor
autonomi. Vi har NATS på radaren for å dele data mellom systemer - kanskje den
er på plass før du setter deg ved pulten.

## Vårt oppdrag

Ok, så vi er enige om at både prosess og verktøy er bra greier. Men hva skal vi
lage? Vi har sammen med Øyvind (produkteier), Hedvig (designer) og Dag Julius
(jurist) ansvar for å videreutvikle smilefjesordningen - du har sikkert sett den
plakaten som henger på døra på spisestedene rundt omkring.

Det første vi skal gjøre er å plukke smilefjes-biten ut av et stort, eldre
system. Her er det en del datamodellering og integrasjons-mikado som skal til
før vi har kontroll på dataene, og kan løpe videre. Når det er gjort skal vi
gjøre hverdagen lettere for inspektørene, samt øke effekten av og omdømmet til
ordningen.

Forresten, hvor kult er det ikke at vi har en egen jurist på teamet? Snakk om å
ta autonomi på alvor!

## Hvem er du?

Som en av kun tre utviklere mestrer du like godt integrasjoner på backenden som
dataflyt og pikselflikking på frontenden. Teamet skal ikke bli noe særlig
større, så vi må ta i et tak der det trengs.

Du liker å jobbe sammen med andre, og har ikke noe i mot å parprogrammere.
