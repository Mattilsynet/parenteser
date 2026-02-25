:page/title Intensjon, instruks og effektuering: byggesteiner i effektsystemer
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2026-02-25T18:33:26.589162"
:blog-post/tags [:fk-is :arkitektur]
:blog-post/description

Hvordan håndterer du effekter i det imperative skallet ditt?
Intensjon, instruks og effektuering er et sted å starte.

:blog-post/body

Tirsdag denne uka LINK hadde vi en svært trivelig meetup. Det var ikke bare de faste folka fra Clojure. Det var Oslo Socially Functional: Reboot.

Oslo Socially Functional var *ett* samlingspunkt for hele FP-miljøet i Oslo. Ikke avgrenset med murer mellom programmeringsspråk og kjøretidsmiljø, én arena for å dele ideer, erfaringer og andre ting man har lært.

Sentralt i miljøet var [Bodil Stokke](https://bodil.lol).
Suveren programmerer, legendarisk meme-trollkvinne.

![](/images/bodil-we-could-be-gods.png)

Temaet vårt var funksjonell arkitektur. Ikke hvordan en enklelt ren funksjon er hyggelig å jobbe med å teste, men hvordan hele systemer som tar med seg funksjonelle systemer er lettere å forstå, få innsyn i og å endre. Etter to spennende talere åpnet paneldebatten, og det ble snakk om effektsystemer.

Et godt effektsystem bør:

1. Fange intensjonen for hva som skal gjøres
2. Oversette den intensjonen til en detaljert instruks
3. Effektuere instruksen på mest direkte vis.

Kodebasen vår har konsepter for de tre punktene:

1. Kommandoer skal fange brukerens intensjon
2. Kommandoer kan *planlegges* for å få en detaljert instruks. Instruksen er en liste av effekter.
3. Effekter *gjør én ting*, uten mikkmakk. Direkte, imperativ kode.

La oss se på et eksempel: turplanlegging.
(Magnar plukket samme eksempel [på Javazone][fkis-jz], hvis du får blod på tann).

[fkis-jz]: https://parenteser.mattilsynet.io/fkis-jz/

```clojure
(commands/plan (📦/lag-state [savalkroa 📦/christian])
  {:command/kind :commands/planlegg-tur
   :command/data {:mats/id (:mats-id savalkroa)
                  :tur/filter-parametere {:filter/kveldsinspeksjon :uten}}
   :command/mastermind (:entity-ref 📦/christian)})

;; Vennligst ignorer 📦-greiene!
;; Det er testdata vi med vilje har pakket til side for å kunne se hva
;; som egentlig skjer i testene.
```

Intensjonen? Planlegg en tur for å besøke et serveringssted, og gi plakat med smil.

Når vi har *planlagt kommandoen* får vi instruksen; effektene:

```clojure
{:plan/result {:tur/id [:effect/ref [:stabil-tx "tur" :tur/id]]}
 :plan/effects
 [{:effect/kind :effects.app-db/transact
   :effect/data
   [{:db/id "tur"
     :tur/deltakere #{{:db/id "deltaker"
                       :deltaker/rolle :deltaker.rolle/inspektør
                       :deltaker/person (:entity-ref 📦/christian)}}
     :tur/status :tur.status/planlegges
     :tur/filter-parametere {:filter/kveldsinspeksjon :uten
                             :filter/omkrets 50.0}
     :tur/stoppene #{{:db/id "stoppet"}}}
    {:db/id "stoppet" :stopp/serveringssted (:entity-ref savalkroa)}
    {:db/id "tur" :tur/id [:effect/ref [:stabil-tx "tur" :tur/id]]}
    {:db/id "stoppet" :stopp/id [:effect/ref [:stabil-tx "stoppet" :stopp/id]]}
    {:db/id "deltaker" :deltaker/id [:effect/ref [:stabil-tx "deltaker" :deltaker/id]]}]}]}
```

Den eneste effekten her er "utfør databasetransaksjon".
Jeg vil si at den er direkte.
Den kaller `datomic.api/transact`, pluss at den lager bittelitt trace-data.

Men er dette her bra, da?
I paneldebatten sa [Bjørn Einar][bjartwolf] noe sånt:

[bjartwolf]: https://github.com/bjartwolf

> Effekter bør ikke ha if-setninger.

For meg treffer det spikeren på hodet, fordi:
**Effekter *gjør én ting*, uten mikkmakk. Direkte, imperativ kode.**

Hvis det er vanskelig å implementere effekten uten if-setninger, gjør den ikke én ting!
Og hvis effekten tar beslutninger, bør koden som tar beslutninger flyttes ut av effekten.
Det tar oss videre til prinsippet Magnar poengterte på Javazone og på nytt nå på tirsdag:

> Du vil ha så mye av koden som mulig i den funksjonelle kjernen, og så lite av koden som mulig i det imperative skallet.

Der effekter med if-er lager trøbbel, er vi godt rigget for å håndtere valg i den funksjonelle kjernen!
Lag en funksjon som tar beslutningen, og *send heller inn* beslutningen til effekten.

Måtte du lage en stor, fin kjerne og et godt effektsystem!
