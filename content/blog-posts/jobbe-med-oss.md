:page/title Har smilefjesordning, søker entusiast for funksjonell programmering
:blog-post/authors [{:person/id :person/magnar} {:person/id :person/christian}]
:blog-post/published #time/ldt "2024-06-26T14:30:00"
:blog-post/tags [:teamet]
:blog-post/description

Vi søker utvikler til teamet vårt -- vil du jobbe med oss?

:open-graph/description

Vi søker utvikler til teamet vårt - vil du jobbe med oss?

:blog-post/body

Har du trua på at små team kan gjøre store greier? Det har vi!

Hei, Christian og Magnar her -- de to utviklerne på Team Mat. Vi skulle gjerne
hatt med én til på laget, men så får det også holde. Er det deg vi ser etter?

Som en av kun tre utviklere jobber du like gjerne med integrasjoner på backenden
som dataflyt og pikselflikking på frontenden. Det blir bare oss, så vi må alle
ta i et tak der det trengs. Det er greit om du ikke er ekspert på alt -- det er
ikke vi heller -- men du må ha lyst til å lære.

## Vår arbeidsform

Vi jobber sammen om å lage systemene våre. Parprogrammering er en [naturlig del
av dagen vår](https://www.zombieclj.no) -- særlig i problemløsningsfasen. [Vi setter oss ikke i hver vår krok
og fyrer av Pull Requests til hverandre](/pull-requests/). Istedenfor jobber vi frem løsningene i
fellesskap, og så går vi hvert til vårt og drar ting i land når formen er kjent.

[Koden vi skriver går rett ut i prod, hver gang vi pusher](/kontinuerlig-integrasjon/), så fremt testene
passerer. Det samme gjelder features som ikke skal lanseres ennå -- [vi lar dem bare ligge skjult til de er klare](/om-kroner-og-flagg/). Det er ikke alltid denne prosessen er
feilfri, men med hyppige leveranser følger også raske bugfikser, man må bare
være litt på ballen. [Vi følger med i prod!](/85-sekunder/)

## Våre verktøy

Håper du er glad i parenteser. [For 12 år siden oppdaget vi Clojure](/clojure/), og har aldri
sett oss tilbake. Clojure gir oss data-drevet, funksjonell
programmering ([også på frontenden, med ClojureScript](/datadreven-frontend/)) og [et REPL](https://www.kodemaker.no/blogg/2022-10-repl/) som lar oss bygge systemet "fra innsiden". Kombinert
med [databasen Datomic](/smakebiter-av-datomic/) har vi en funksjonell gavepakke som gir og gir.

Vi bygger systemene våre på [arkitekturen Funksjonell kjerne/imperativt skall](https://youtu.be/ag603CBk2TY).
Det betyr kort og godt at vi implementerer domenelogikken -- mesteparten av
systemet -- med "pure functions", og holder I/O og andre bevegelige deler på
utsiden. [For å kommunisere mellom systemer, dele data og legge ting på kø bruker
vi NATS](/nyttig-nats/).

Systemene våre kjører i GCP og er satt opp med Terraform. [Også her nyter vi stor
autonomi](https://www.kodemaker.no/blogg/2019-12-devops/).

## Vårt oppdrag

Ok, så vi er enige om at både prosess og verktøy er bra greier. Men hva skal vi
lage? Vi har sammen med Øyvind (produkteier), Hedvig (designer) og Dag Julius
(jurist) ansvar for å videreutvikle smilefjesordningen -- du har sikkert sett den
plakaten som henger på døra på spisestedene rundt omkring.

Det første vi skal gjøre er å plukke smilefjes-biten ut av et stort, eldre
system. Her er det en del datamodellering og integrasjons-mikado som skal til
før vi har kontroll på dataene, og kan løpe videre. Når det er gjort skal vi
gjøre hverdagen lettere for inspektørene, samt øke effekten av og omdømmet til
ordningen.

Forresten, hvor kult er det ikke at vi har en egen jurist på teamet? Snakk om å
ta autonomi på alvor!

## Vil du bli med?

Vi er litt sære i IT-bransjen, og ser etter noen som har sammenfallende
verdisyn. Det er ikke noen ulempe om du har sett alle videoene til Rich Hickey,
for å si det sånn. Faller innholdet på denne bloggen i smak, så er det store
sjanser for at vi kommer til å jobbe godt sammen.

I så fall, finn frem tålmodigheten og [søk via annonsen på Webcruiter](https://100500.webcruiter.no/Main2/Recruit/Public/4769469061?language=nb&link_source_id=0).
