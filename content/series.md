--------------------------------------------------------------------------------
:series/id :clojure-core
:series/name clojure.core
:series/sequential? true
:page/uri /clojure-core/
:page/kind :page.kind/series
:series/blurb

[clojure.core] er en serie med bloggposter om alle de nyttige verktøyene i
`clojure.core`, hjørnesteinen i det rikholdige standardbiblioteket til Clojure.

:series/description

`clojure.core` er hjørnesteinen i det rikholdige standardbiblioteket til
Clojure. Der bor det haugevis med nyttige små funksjoner. Stabler du sammen nok
av disse generiske funksjonene kan du løse et bredt spekter av
(domene-spesifikke) problemer med svært få skreddersydde abstraksjoner. God
oversikt over hva `clojure.core` har å by på er på mange måter nøkkelen til å
lykkes med Clojure. I denne serien med bloggposter trekker vi frem noen
godbiter, i håp om at du også sier deg enig i følgende sitat:

> It is better to have 100 functions operate on one data structure than 10
> functions on 10 data structures.

[Alan Perlis](https://en.wikipedia.org/wiki/Epigrams_on_Programming)

--------------------------------------------------------------------------------
:series/id :datomic
:series/name Smakebiter av Datomic
:series/sequential? true
:page/uri /smakebiter-av-datomic/
:page/kind :page.kind/series
:series/blurb

[Smakebiter av Datomic] er en serie med bloggposter om
den spennende og rare databasen Datomic.

:series/description

Datomic er en jæskla interessant database. Den ble designet fra bunnen opp av
Rich Hickey i 2010 -- etter at han laget et nytt programmeringsspråk nettopp for
å kunne realisere idéene sine. Det er noen begrensninger som har forsvunnet på
de 40 årene sidene de tradisjonelle databasene ble utformet. For de av oss som
er melket opp på SQL, så er det spennende å se hvor annerledes det kan tenkes
rundt lagring og henting av data. Vi her på Team Mat har hatt gleden av å jobbe
med databasen omtrent fra den ble lansert i 2012. Bli med og få en smakebit av
Datomic du også.

--------------------------------------------------------------------------------
:series/id :foredrag
:series/name Foredrag
:page/uri /foredrag/
:page/kind :page.kind/series
:series/blurb

Interessert i [flere foredrag] fra oss på Parenteser-bloggen?

:series/description

Det hender at vi også holder foredrag ute i den vide verden. Her har vi samlet
noen av dem til glede for nye lesere.

--------------------------------------------------------------------------------
:series/id :fulltekstsok
:series/name Fulltekstsøk fra bunnen av
:page/uri /hjemmelaget-sok/
:series/sequential? true
:page/kind :page.kind/series
:series/blurb

Jeg har skrevet mer om [fulltekstsøk fra bunnen av] i denne serien med
bloggposter.

:series/description

Fulltekstsøk kan virke magisk. Hva er det egentlig som foregår når jeg får treff
på "klovneforskning" etter å ha søkt på "forske"? Gjennom disse bloggpostene
håper jeg å finne ut av det ved å bygge en enkel søkemotor fra bunnen av i
JavaScript. Resultatet blir godt nok til å ta i bruk i prod på
[beskjedne](https://www.matvaretabellen.no/)
[datamengder](https://smilefjes.mattilsynet.no/) (noen tusen dokumenter).

--------------------------------------------------------------------------------
:series/id :nats
:series/name Nyttig å vite om NATS
:series/sequential? true
:page/uri /nats/
:page/kind :page.kind/series
:series/blurb

[Nyttig å vite om NATS] er en serie med bloggposter om det distribuerte
meldingssystemet NATS.

:series/description

[NATS](https://nats.io) er et distribuert meldingssystem med mange bruksmønstre:
fra pubsub, arbeidskøer og strømming av persistente logger til key/value og
object storage. Alt fra den samme primitiven, emnebasert meldingsutveksling.

NATS er skrevet i Go og distribueres dermed som en rask frittstående binærfil.
Den har en implementasjon av [Raft konsensusalgoritmen](https://raft.github.io/)
for distribuert persistens med replikering.
