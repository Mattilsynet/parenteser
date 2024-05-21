:page/title Transaksjoner i Datomic: Et herlig bekvemmelig API
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-05-21T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:open-graph/description

Man har ikke mye glede av en database uten å kunne dytte data inn i
den. Etter 7 bloggposter i serien er det tid for en titt på Datomic
transaksjoner - og hva enn Dead Kennedys har med saken å gjøre.

:blog-post/description

Man har ikke mye glede av en database hvis man ikke kan dytte data inn i
den. Etter 7 bloggposter i serien er det på tide å ta en titt på Datomic
transaksjoner - og hva enn Dead Kennedys har med saken å gjøre.

:blog-post/body

Data i Datomic lagres ikke i tabeller eller dokumenter, men som selvstendige
RDF-lignende tupler:

```clj
[entitet, attributt, verdi]
```

Her er eksempelet fra [den første bloggposten i serien](/smak-av-datomic/) som
handlet om nettopp dette:

```clj
[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :clojure]
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar"]
[5678 :person/family-name "Sveen"]
```

La oss si at jeg har lyst til å legge til en tag på den bloggposten, noe slikt,
kanskje:

```clj
[1234 :blog-post/tags :eksplosjoner]
```

Da ville transaksjonen sett slik ut:

```clj
[[:db/add 1234 :blog-post/tags :eksplosjoner]]
```

Som jeg kunne sendt til databasen på denne måten:

```clj
(d/transact conn [[:db/add 1234 :blog-post/tags :eksplosjoner]])
```

Her bruker vi databasefunksjonen `:db/add`, som legger til et fakta om en
entitet. Ettersom `:blog-post/tags` kan ha flere verdier per attributt, så blir
det lagt til ved siden av de andre:

```clj
[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :clojure]
[1234 :blog-post/tags :eksplosjoner] ;; <==
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar"]
[5678 :person/family-name "Sveen"]
```

Hvis jeg bruker `:db/add` på et attributt som ikke tar flere verdier, så blir
den nye verdien lagt til og den gamle verdien trukket tilbake:

```clj
(d/transact conn [[:db/add 5678 :person/given-name "Magnar Trygve"]])

;; =>

[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :clojure]
[1234 :blog-post/tags :eksplosjoner]
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar Trygve"] ;; <==
[5678 :person/family-name "Sveen"]
```

Ingen fare, [vi kaster ikke data med Datomic](/historiske-data/), men verdier
som er trukket tilbake blir filtrert ut med mindre du ber om dem spesielt.

Hvis jeg vil fjerne en tag, så gjør jeg det slik:

```clj
(d/transact conn [[:db/retract 1234 :blog-post/tags :clojure]])

;; =>

[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :eksplosjoner]
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar Trygve"]
[5678 :person/family-name "Sveen"]
```

Helt fine byggeklosser, dette, men var det så bekvemmelig? Ikke veldig. For å
sitere Dead Kennedys: *Give me convenience or give me death*.

## Fra rør til porselen

La oss ta en ekstra titt på en av transaksjonene våre:

```clj
[[:db/add 5678 :person/given-name "Magnar Trygve"]]
```

Det første problemet her er `5678`. Hva er dette tallet? Jo, det er Datomic sin
interne entitets-ID. Den har ikke jeg noe forhold til, særlig siden det egentlig
ser noe sånt ut:

```clj
[[:db/add 17592202810723 :person/given-name "Magnar Trygve"]]
```

Så her kommer Datomic-transaksjonene sin første lille bekvemmelighet: Jeg kan
referere til entiteter på en mye hyggeligere måte:

```clj
[[:db/add [:person/id "magnars"] :person/given-name "Magnar Trygve"]]
```

Dette `[:person/id "magnars"]` er en *entity ref*. Istedenfor å måtte finne ut
av en intern database-ID kan du referere til entiteten med et attributt - så
lenge attributtet unikt identifiserer entiteten.

Du kjenner det kanskje igjen fra [en tidligere bloggpost](/alle-gatene-i-kommunen/):

```clj
(d/entity db [:poststed/postnummer "1630"])

;; => {:db/id 17592186315131}
```

Her sender vi inn en *entity ref* til `d/entity`, som jo egentlig tar en ID:

```clj
(d/entity db 17592186315131)

;; => {:db/id 17592186315131}
```

Samme greia i transaksjoner, altså. Men bekvemmelighetene slutter ikke der.

## Fra refs til maps

La oss si at vi vil legge til en ny person i databasen. Vi har jo ingen
database-ID for den nye entiteten enda. Vi kan da bruke en placeholder streng,
for å fortelle Datomic at det her er snakk om en ny entitet:

```clj
(d/transact conn
 [[:db/add "ny person" :person/id "boosja"]
  [:db/add "ny person" :person/given-name "Mathias"]
  [:db/add "ny person" :person/family-name "Iversen"]])
```

Datomic vil da opprette en ny entitet og bruke den nye ID-en for alle `"ny
person"`. Supert! Men det finnes en mer bekvemmelig måte:

```clj
(d/transact conn
 [{:person/id "boosja"
   :person/given-name "Mathias"
   :person/family-name "Iversen"}])
```

Istedenfor å oppgi hvert attributt for seg, kan jeg oppgi alle sammen samtidig.
Ikke bare føles det mer naturlig, men det blir også selvinnlysende for både
leser og transactor at det her er snakk om samme entitet.

(PS! Sjekk ut [Mathias sin nye blogg](https://mathivethoughts.no). Anbefales!)

Jeg kan også gjøre endringer på denne måten. Fra dette:

```clj
(d/transact conn
 [[:db/add [:person/id "magnars"] :person/given-name "Magnar Trygve"]])
```

til dette:

```clj
(d/transact conn
 [{:person/id "magnars"
   :person/given-name "Magnar Trygve"}])
```

Men det ser jo helt likt ut som når vi legger til en ny person? Det stemmer. Det
er *upsert*. Hvis en entitet med `:person/id "magnars"` allerede finnes, så
endres den. Hvis ikke, så lages det en ny.

## Finalen

Hva da med det store eksempelet i begynnelsen?

```clj
[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :clojure]
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar"]
[5678 :person/family-name "Sveen"]
```

Dette kan transactes til databasen slik:

```clj
(d/transact conn
 [["bloggen" :blog-post/id 14]
  ["bloggen" :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
  ["bloggen" :blog-post/author "skribenten"]
  ["bloggen" :blog-post/tags :datomic]
  ["bloggen" :blog-post/tags :clojure]
  ["skribenten" :person/id "magnars"]
  ["skribenten" :person/given-name "Magnar"]
  ["skribenten" :person/family-name "Sveen"]])
```

Det ser ganskelig likt ut, bare med placeholder strenger for å fortelle Datomic
hvilke attributter som hører til samme entitet.

Men igjen kan vi bruke entity maps for å gjøre det hele mer bekvemmelig. Da ser
det slik ut:

```clj
(d/transact conn
 [{:blog-post/id 14
   :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"
   :blog-post/tags [:datomic :clojure]
   :blog-post/author "skribenten"}
  {:db/id "skribenten"
   :person/id "magnars"
   :person/given-name "Magnar"
   :person/family-name "Sveen"}])
```

Her brukes også `"skribenten"` som en midlertidig database-ID, denne gangen for
å fortelle Datomic at `:blog-post/author` peker på personen i samme transaksjon.

Men se nå! Vi kan også skrive den slik:

```clj
(d/transact conn
 [{:blog-post/id 14
   :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"
   :blog-post/tags [:datomic :clojure]
   :blog-post/author {:person/id "magnars"
                      :person/given-name "Magnar"
                      :person/family-name "Sveen"}}])
```

Nå tar det helt av her. Ettersom `:blog-post/author` er et attributt som peker
på en annen entitet (en *ref*), så skjønner Datomic transactoren til og med
dette.

Alt dette er porselen på toppen av røret `[:db/add e a v]`. Du kunne fått til
det samme med de enkleste byggeklossene. Men herlighet så bekvemmelig, da gett!
Dead Kennedys ville vært fornøy... nei, hvem er det jeg prøver å lure. Jello
Biafra gir full faen i alt dette. Men fett er det, likevel.
