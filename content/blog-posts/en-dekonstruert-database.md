:page/title En dekonstruert database
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-01-30T09:00:00"
:blog-post/tags [:datomic]
:blog-post/series {:series/id :datomic}
:blog-post/description

Rich Hickey sa en gang at design fundamentalt handler om ta ting fra hverandre,
slik at man kan sette dem sammen igjen. Dette gjelder i høyeste grad for
Datomic, databasen som er kløyvet like i to.

:blog-post/body

I [forrige smakebit](/smak-av-datomic/) snakket vi om hvordan Datomic skiller
seg fra tradisjonelle databaser i måten den modellerer data - ikke i tabeller
med rader og kolonner, men i form av entiteter og attributter. Dagens smakebit
tar for seg et helt annet aspekt, hvor Datomic også har en radikalt annerledes tilnærming.

Rich Hickey [sa en gang](https://www.youtube.com/watch?v=QCwqnjxqfmY) at design
fundamentalt handler om ta ting fra hverandre, på en slik måte at man kan sette
dem sammen igjen. Dette gjelder i høyeste grad for Datomic sin operasjonelle
arkitektur.

Tradisjonelle databaser slik vi kjenner dem består av en kjørende prosess --
serveren -- som all databaseaktivitet går gjennom. Serveren er arkivaren, en
selvstendig aktør som vokter og røkter dataene. Som klient av databasen sender
du forespørsler til serveren -- ofte i form av SQL
-- og så løper arkivaren av gårde og finner frem nøyaktig de datasnuttene du har
etterspurt, eller oppdaterer de feltene du ba om å få endret.

Sånn er det ikke med Datomic.

Istedenfor en server/klient-arkitektur, har Datomic en
transactor/peer-arkitektur.

- **Transactor**-ens eneste oppgave er å skrive nye transaksjoner med data.

- **Peers** leser data.

Peers er navngitt sånn fordi de ikke er klienter av en server, men er
likeverdige, fullverdige lesere. De har like god tilgang til dataene som alle
andre peers, inkludert transactoren.

Kort fortalt: Skriving og lesing er helt separert, og gjøres av forskjellige prosesser.
Datomic har tatt den tradisjonelle rollen til en databaseserver og kløyvet like
i to.

## Så da finnes det peers man kan spørre om data?

Nei, men godt spørsmål. Her er det som er så kult: *Du* er en peer. Altså,
app-en du skriver. Prosessen din, koden din har direkte tilgang til dataene. Ved
oppstart spør den transactor-en hvor dataene er lagret, og deretter henter den
dataene selv derfra.

La meg forsøke å illustrere hvordan det ser ut i praksis:

Vi vet at databaser trenger indekser for å være raske. Indeksene er noe
serveren bruker for å slå opp data effektivt. Men med Datomic er ikke indeksene noe
serveren bruker, det er noe vi bruker. Vi har direkte tilgang til indeksene, i
vår egen prosess.

La oss se på litt kode. Datomic API-et gir tilgang til indeksen via `d/datoms`.
Den gir oss en liste med datoms som matcher:

```clj
(count (seq (d/datoms db :avet :player/name)))

;; => 8324
```

Her bruker vi `:avet`-indeksen som er sortert på **a**ttributt først, så
**v**erdi, **e**ntitet og **t**ransaksjon. Vi finner alle oppføringer med
`:player/name`-attributtet, og får vite at det er 8324 spillere med navn i
databasen.

La oss se på starten av indeksen:

```clj
(take 5 (d/datoms db :avet :player/name))

;; =>

(#datom[17592205630297 65 "A Dutch Curious" 13194159119207]
 #datom[17592186090739 65 "Aaa" 13194139579706]
 #datom[17592188248201 65 "Aaaa" 13194141737106]
 #datom[17592186187563 65 "Aaaaaa" 13194139676483]
 #datom[17592195932321 65 "Aaaaaaaaaahhhhh" 13194149421349])
```

Det er åpenbart at indeksen er sortert alfabetisk, og at enkelte spillere av
Adventur ikke har den beste fantasien når det kommer til navn.

Så her er det datoms, tupler av `[e a v t]` (mer om dette i [forrige
smakebit](/smak-av-datomic/)). Attributtet er kanskje vanskeligst å kjenne
igjen, fordi det er representert ved sin database-id, `65`, som er det
løpenummeret `:player/name` har fått.

Så, la oss finne min spiller i dette datasettet:

```clj
(first (d/datoms db :avet :player/name "Magnar"))

;; => #datom[17592202810723 65 "Magnar" 13194156299671]
```

Et raskt oppslag i indeksen, og jeg fant ID-en min.

Hvis jeg allerede hadde hatt en ID, og heller lurte på hva navnet var, så kan
jeg bruke indeksen som er sortert på entitet-ID, `:eavt`.

```clj
(first (d/datoms db :eavt 17592202810723 :player/name))

;; => #datom[17592202810723 65 "Magnar" 13194156299671]
```

Som du ser så gir dette samme datom, bare slått opp i en annen indeks. Hvis jeg gjør
samme oppslag som over, men dropper å oppgi attributtet, slik ...

```clj
(d/datoms db :eavt 17592202810723)
```

... så får jeg ut en liste med *alle* attributter for min spiller.

Ganske stilig.

## Til slutt

Sånn til daglig bruker man ikke indeksene direkte så ofte. Datomic har mer
praktiske måter å hente ut data på enn dette. Men eksempelet illustrerer
poenget: Med Datomic får applikasjonsprosessen din full tilgang til de dataene
man normalt tenker er skjult bak et spørrespråk og en ekstern prosess.

Ikke bare betyr det at problemer som
[N+1](https://docs.sentry.io/product/issues/issue-details/performance-issues/n-one-queries/)
forsvinner helt. Ikke bare betyr det at du aldri igjen kommer til å ta ned prod
fordi du skrev en suboptimal SQL -- du kan bruke så mye prosessorkraft og tid
på en query du bare vil, uten å påvirke ytelsen for noen andre. Det betyr også
at du kommer mye nærmere på dataene. Og det gjør en stor forskjell.
