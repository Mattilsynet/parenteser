:page/title Flaskehalsen peker på
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-10-09T09:00:00"
:blog-post/tags [:clojure]
:blog-post/description

Kode skal først og fremst leses av mennesker, men når importen tar 50 minutter(!)
er det kanskje på tide å gjøre litt ytelsesforbedringer. Vi trodde vi visste hva
som var treigt, det stemte ikke.

:open-graph/description

Et rask introduksjon til hvordan du kan gå fram for å gjøre
ytelsesoptimalisering i Clojure, med noen tanker om faktiske problemer vs
mistenkte problemer.

:blog-post/body

Kode skal først og fremst leses av mennesker, men når importen tar 50 minutter(!)
er det kanskje på tide å gjøre litt ytelsesforbedringer. Vi trodde vi visste hva
som var treigt, det stemte ikke.

## Start med målinger

Det er lett å bare slenge en `(time ,,,)` rundt koden, og se hva vi får. Denne
tilnærmingen gir oss et tall og kan være nyttig for en før og
etter-sammenligning. Men det er også alt.

Hvis det er én ting jeg har lært av [Goldratt](/best-practice/) så er det at
lokale optimaliseringer er meningsløse om man ikke har noen formening om hvor
flaskehalsene i systemet befinner seg.

[clj-async-profiler](https://github.com/clojure-goes-fast/clj-async-profiler) er
et bibliotek som er laget nettopp for dette. Slik profilerer du et stykke kode
med den:

```clj
(require '[clj-async-profiler.core :as prof])

(prof/profile
  (dotimes [_ 1000]
    (println "Hei sveis")))
```

`dotimes` hjelper JVM-en å bli varm i trøya så den gjør jobben sin omtrent slik
den vil gjøre den i produksjon.

## Hva skjer med importen, da?

Siden vi ønsket å få opp ytelsen i importen vår kjørte vi én import 10000 ganger
med profileren:

```clj
(ns matnyttig.perf
  (:require [clj-async-profiler.core :as prof]
            [matnyttig.imperative-shell.core :as imperative-shell]
            [matnyttig.import.tilsynslop :as tilsynsløp-import]))

(comment
  ;; Hent systemet vårt fra integrant
  (def ctx (:system/ctx integrant.repl.state/system))

  ;; Denne lar oss lese fra systemet uten side-effekter
  (def state (imperative-shell/get-state ctx))

  ;; Testdata
  (def tilsynsløp {,,,})

  (prof/profile
   (time ;; Få total tid i REPL-bufferet
    (dotimes [_ 10000]
      (tilsynsløp-import/importer state (:command/data tilsynsløp)))))

 )
```

Etter å ha kjørt denne kodesnutten kan vi få opp såkalte flame graphs i
nettleseren ved å evaluere `(prof/serve-ui 9998)`. Vår første så sånn ut:

<a href="/images/flame-graph1.png">
<img src="/images/flame-graph1.png" style="max-width: 100%" border="0" alt="Flammegraf over funksjonskallene i én import">
</a>

Hvordan skal vi så lese denne? Kort fortalt: Fra bunnen og opp. Den aller
nederste linja har teksten "all" og viser all tid brukt for operasjonen som er
profilert.

Allerede på neste linje deler grafen seg i to, og illustrerer at ca halvparten
av tiden går med i en ny tråd som ser ut til å drive med garbage collection:

<a href="/images/flame-graph2.png">
<img src="/images/flame-graph2.png" style="max-width: 100%" border="0" alt="Flammegraf som viser garbage collection">
</a>

Vår kode finnes blant de blå linjene i flammegrafen som indikerer Clojure-kode.
Når strekene over hverandre er omtrent like brede så betyr det at mer eller
mindre all tiden brukt på den nederste streken er dekket av streken som er
høyere opp.

<a href="/images/flame-graph3.png">
<img src="/images/flame-graph3.png" style="max-width: 100%" border="0" alt="Flammegraf som viser et veiskille">
</a>

Denne delen av grafen viser at tiden brukt av vår funksjon `forbered-vurdering`
domineres av kallet til `matnyttig.datomic/q`. På forhånd hadde vi mistenkt at
det var lite optimaliserte queries som var syndebukken, så dette er enn så lenge
som forventet. Men så skjer det noe interessant: halvparten av tiden i
`matnyttig.datomic/q` går med i `open-telemetry.tracing/make-span`. Snakk om
Schrödingers ytelsesproblem.

## Many ... minutes later

Som du kanskje har gjettet så er `matnyttig.datomic/q` en bitteliten wrapper
rundt `datomic.api/q` som legger på tracing:

```clj
(defn q [query & args]
  (tracing/with-span ["datomic.api/q" {:query query
                                       :args args}]
    (apply d/q query args)))
```

Her ser vi forøvrig også `apply` som står for den andre halvparten der grafen
deler seg.

Etter å kikket litt rundt viser det seg at det mest kostbare som foregår i denne
importen rett og slett er en del reflection i OpenTelemetry-koden vår. Ops!

Steg 1 i løsningen var å legge til `(set! *warn-on-reflection* true)` i
dev-profilen til prosjektet. Deretter krydra vi OpenTelemetry-koden vår med 50+
typehint for å unngå reflection ved kall på ymse Java-metoder. Resultatet var en
litt hyggeligere flammegraf:

<a href="/images/flame-graph4.png">
<img src="/images/flame-graph4.png" style="max-width: 100%" border="0" alt="Flammegraf som viser mindre overhead i tracingen">
</a>

`time` for 10k imports forteller oss også at vi er på rett vei:

- 7477 millisekunder med reflection
- 2692 millisekunder med typehint

Nesten 3x forbedring fra noe som ikke var på radaren vår i det hele tatt. Hvem
skulle tro at noen konkrete tall skulle vise seg å være så nyttige?

## Videre funn

Etter å fikset reflection-problemene var det bare å bore seg videre ned (opp?) i
flammegrafen.

Vi fant som forventet en del tid brukt på Datomic-queries, men løsningen var
ikke den vi først så for oss. Det var ikke snakk om uoptimaliserte spørringer,
men heller at vi kjørte en spørring på et uindeksert felt for hver eneste import
når vi egentlig bare trengte å kjøre den én eneste gang.

Sånn går no dagan. Etter en times tid med `clj-async-profiler` og flammegrafer
hadde vi fått 10k importer ned fra 7477 millisekunder til 171, altså nesten to
størrelsesordener.

Og husker du importen som tok 50 minutter? Nå tar'n 13. Ikke gæernt!
