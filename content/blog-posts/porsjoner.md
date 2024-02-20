:page/title Data i passe porsjoner
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-02-20T09:00:00"
:blog-post/tags [:clojure]
:blog-post/series {:series/id :clojure-core}
:blog-post/description

Noen datamengder kan ikke spises i én jafs, men må heller porsjoneres ut i
passende mengder. Heldigvis er det lekende lett i Clojure.

:open-graph/description

Batching av operasjoner: en kort liten hyllest til Clojures standardbibliotek.

:blog-post/body

Nylig ba jeg en database om noe data for en mengde id-er, omtrent sånn:

```clj
(defn get-last-served [conn meal-ids]
  (db/q conn
   '{:select [:meal-id (max :served-at)]
     :from :meals
     :where (in :meal-id ?meal-ids)}
   {:params {:meal-ids meal-ids}}))
```

Altså: gitt alle disse måltids-id-ene, gi meg tilbake en liste med id-en og
siste tidspunkt det ble servert.

Problemet kom da jeg ba om for mange måltider på en gang. Denne spørringen
skulle nemlig til en eldre databaseserver som ikke syns det var noe særlig å få
mer enn 1000 id-er på én gang i en `in`.

Løsningen ble å batche spørringen min. Så hvordan gjør vi det? Batching er
egentlig to operasjoner: del opp input i passe porsjoner, og samle resultatene i
én datastruktur.

Så hvordan deler man opp en datastruktur i Clojure? Med `partition` eller
`partition-all`:

```clj
(partition 2 [0 1 2 3 4])
;;=> ((0 1) (2 3))

(partition-all 2 [0 1 2 3 4])
;;=> ((0 1) (2 3) (4))
```

Som du ser så kan `partition` finne på å utelate data. Det er fordi den kun
returnerer tupler av angitt størrelse (2, i dette tilfellet). Har du en "rest"
så blir den ikke med. Dette har sitt bruk, men ikke til å løse batching.

`partition-all` inkluderer all input, selvom det betyr at den kan returnere
tupler med ulikt antall elementer. Det passer bra for oss, som nå har en liste
med en passe mengde inputs å sende til database-serveren.

Gitt at vi har en database-tilkobling i `conn` og en liste med id-er i `ids` kan
vi nå loope over denne lista og hente resultatene for hver enkelt batch:

```clj
(map
 (fn [batch]
   (get-last-served conn batch))
 (partition-all 1000 ids))
```

Dette gir oss en liste med lister av resultater. Disse må samles i én liste. Den
aller enkleste måten å gjøre det på er å bytte ut `map` med `mapcat` -- `mapcat`
forventer nemlig at funksjonen du gir den returnerer en liste, og så
konkatenerer den sammen alle resultatene til én liste:

```clj
(mapcat
 (fn [batch]
   (get-last-served conn batch))
 (partition-all 1000 ids))
```

Vips, så har vi løst batching! La oss lage en funksjon av det:

```clj
(defn batch [f batch-size xs]
  (mapcat f (partition-all batch-size xs)))
```

Vi kan bruke den sånn:

```clj
(defn get-last-served [conn meal-ids]
  (batch
   (fn [batch]
     (db/q conn
      '{:select [:meal-id (max :served-at)]
        :from :meals
        :where (in :meal-id ?meal-ids)}
      {:params {:meal-ids batch}}))
   1000
   meal-ids))
```

Vakkert!
