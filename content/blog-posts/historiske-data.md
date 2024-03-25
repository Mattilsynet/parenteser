:page/title Hva om databasen ikke mistet data?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-03-26T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:blog-post/description

Kildekoden vår bor i git. Det lar oss svare på når enhver linje ble innført i
kodebasen, hvem som sist endret den, og hvorfor. Hadde det ikke vært fint om vi
hadde like god kontroll på dataene i produksjonssystemene våre? Med Datomic har
vi det.

:open-graph/description

Tradisjonelle databaser har datatap hver gang du gjør `update` og `delete`.
Hvordan ville verden sett ut om det ikke var tilfelle? Datomic har svaret.

:blog-post/body

Tenk deg at hver gang du pusha kode til Github så mista du hele git-historikken
din. Det hadde ikke vært noe særlig. Men det er sånn vi behandler dataene i
produksjonssystemene våre: `update` og `delete` sletter data over en lav sko
uten at vi så mye som rynker på nesa. Men det må ikke være sånn.

Når vi skriver data til Datomic, eller "transacter", som vi sier, får vi tilbake
litt informasjon om transaksjonen, inkludert alle de nye datapunktene:

```clj
@(d/transact conn
  [{:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"
    :person/name "Christian Johansen"
    :person/email "christian@cjohansen.no"}])

;;=>
;;{:db-before datomic.db.Db@cd2e2b2e
;; :db-after datomic.db.Db@538ccc73
;; :tx-data [#datom[13194139534313 50 #inst "2024-03-25T14:33:44.931-00:00" 13194139534313 true]
;;           #datom[17592186045418 72 #uuid "9f6610aa-121b-406b-a96a-8656bc154cda" 13194139534313 true]
;;           #datom[17592186045418 72 "Christian Johansen" 13194139534313 true]
;;           #datom[17592186045418 73 "christian@cjohansen.no" 13194139534313 true]]
;; :tempids {-9223301668109598132 17592186045418}}
```

Fra `:tx-data` ser vi at det ble opprettet fire nye [datoms](/smak-av-datomic/).
Ett for id-en, ett for navnet mitt, ett for epostadressen, og ett som har en
dato på seg. Hvis du ser nærmere etter vil du se at alle datapunktene refererer
til det siste:

<pre class="codehilite codelike">[<strong class="ss">13194139534313</strong> 50 #inst "2024-03-25T14:33:44.931-00:00" <strong class="ss">13194139534313</strong> true]
[17592186045418 72 #uuid "9f6610aa-121b-406b-a96a-8656bc154cda" <strong class="ss">13194139534313</strong> true]
[17592186045418 72 "Christian Johansen" <strong class="ss">13194139534313</strong> true]
[17592186045418 73 "christian@cjohansen.no" <strong class="ss">13194139534313</strong> true]]</pre>

<strong class="codehilite"><span class="ss">13194139534313</span></strong> er
id-en til transaksjonen som skapte disse dataene, og transaksjonen er en entitet
på lik linje med personen jeg opprettet. Datomic legger alltid på ett attributt
på transaksjonen, nemlig `:db/txInstant`. Altså tar Datomic vare på tidspunktet
for alle transaksjoner som skrives til databasen.

La oss nå oppdatere databasen ved å endre epostadressen min:

```clj
@(d/transact
  conn
  [{:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"
    :person/email "christian.johansen@mattilsynet.no"}])

;;=>
;;{:db-before datomic.db.Db@c6a66422
;; :db-after datomic.db.Db@11270aa5
;; :tx-data [#datom[13194139534315 50 #inst "2024-03-25T14:54:22.580-00:00" 13194139534315 true]
;;           #datom[17592186045418 74 "christian.johansen@mattilsynet.no" 13194139534315 true]
;;           #datom[17592186045418 74 "christian@cjohansen.no" 13194139534315 false]]
;; :tempids {-9223301668109598123 17592186045418}}
```

Nå ble det opprettet tre nye datoms:

1. En transaksjonsentitet
2. Informasjon om at det legges til ny epostadresse
3. Informasjon om at gammel epostadresse fjernes

I stedet for å bare skrive over dataene mine har Datomic tatt vare på
informasjon om at person-entiteten har en ny verdi for epost-attributtet. Så
hvordan ser personen ut nå?

```clj
(d/entity db [:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"])

;;=>
;;{:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"
;; :person/name "Christian Johansen"
;; :person/email "christian.johansen@mattilsynet.no"}
```

Det ser riktig ut. Men hva med epostadressen jeg skrev over? Datomic vet om den
også. Det ser vi hvis vi ber om å hente ut entiteten på et gitt tidspunkt (fra
før den fikk ny epost-adresse):

```clj
(d/entity
 (d/as-of db #inst "2024-03-25T14:54:00.00-00:00")
 [:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"])

;;=>
;; {:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"
;;  :person/name "Christian Johansen"
;;  :person/email "christian@cjohansen.no"}
```

Der har vi eposten min fra gode gamle dager!

Jeg kan til og med hente ut all historikk om et gitt attributt med en
datalog-spørring:

```clj
(d/q '[:find ?email ?txInstant
       :in $ ?person
       :where
       [?person :person/email ?email ?tx true] ;; 1
       [?tx :db/txInstant ?txInstant]]         ;; 2
     (d/history db)
     [:person/id #uuid "9f6610aa-121b-406b-a96a-8656bc154cda"])

;;=>
;; #{["christian@cjohansen.no" #inst "2024-03-25T14:33:44.931-00:00"]
;;   ["christian.johansen@mattilsynet.no" #inst "2024-03-25T14:54:22.580-00:00"]}
```

Her ber jeg Datomic om alle data som tilfredsstiller to kriterier:

1. Alle datoms som legger til en verdi på `:person/email` på den spesifikke
   personen jeg ba om
2. `:db/txInstant`-attributtet fra transaksjonen som skrev til `:person/email`

Datomic svarer med de to epostadressene jeg har skrevet, og tidspunktet de ble
skrevet. Med andre ord har jeg både `created_at` og `updated_at` for hvert
eneste attributt i hele databasen. Ikke nok med det, jeg har søren meg
`updated_at` for hver gang et attributt blir skrevet -- med gammel og ny verdi!

## Hva skal jeg med dette?

Her er det kun fantasien din som setter grenser, men la meg gi deg et par
eksempler.

Du kommer på jobb en morgen og finner en exception i loggen fra i går kveld. Når
du prøver å trigge feilen på nytt går alt som det skal. Siden datagrunnlaget har
endret seg har du gått glipp av øyeblikket. Med Datomic er det bare å be om
databasen på det aktuelle tidspunktet, og vips så er du i stand til å gjenskape
den nøyaktige situasjonen som feilet.

En bruker i systemet klager over at en viktig epost ikke har kommet frem. Du
sjekker databasen og finner riktig epostadresse. Men eposten ble sendt av
systemet for flere timer siden. Hvilken epostadresse lå der da jobben som sendte
eposten gikk ut? Svaret er bare en spørring unna.

Det er ingen grenser for hvor nyttig det er å ha tilgang til all historikk til
enhver tid. Og det fine er at i det øyeblikket du kommer på en ny feature som
lener seg på historikk så har du allerede masse historikk å fore den med. Alt
ligger der, det er bare opp til deg å finne noe lurt å bruke det til.
