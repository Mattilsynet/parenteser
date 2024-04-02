:page/title Skriv kode istedenfor SQL
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-04-02T15:45:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:blog-post/description

Datomic flytter oppslag og spørringer til klienten. Det betyr at du ikke trenger
sende avgårde en tekststreng til en annen server, men kan grave i dataene der du
er. La oss se på et praktisk eksempel.

:blog-post/body

Datomic flytter oppslag og spørringer til klienten. Det betyr at du ikke trenger
sende avgårde en tekststreng til en annen server, men kan grave i dataene der du
er. La oss se på et praktisk eksempel.

På jobb har vi kost oss med adresser, gater, poststeder og kommuner i det siste.
Veldig artig! La oss starte med å slå opp et poststed:

```clj
(d/entity db [:poststed/postnummer "1630"])

;; => {:db/id 17592186315131}
```

Her ber vi om å få en entitet fra databasen med postnummer 1630. Det vi får
tilbake kan se ut som et map med bare en `:db/id` i, men utseendet bedrar. Vi har
fått en Datomic Entity. Se nå:

```clj
(:poststed/navn
 (d/entity db [:poststed/postnummer "1630"]))

;; => "Gamle Fredrikstad"
```

Jøss, her var det jo et navn. Det var ikke der i sted. Hvor kom det fra?

Du gjettet det sikkert. Det kom fra databasen. I motsetning til et vanlig map,
så er en entitet lat. Den henter dataene ved behov. La oss prøve noe:

```clj
(def poststed (d/entity db [:poststed/postnummer "1630"]))

poststed

;; => {:db/id 17592186315131}

(:poststed/navn poststed)

;; => "Gamle Fredrikstad"

poststed

;; => {:db/id 17592186315131,
;;     :poststed/navn "Gamle Fredrikstad"}
```

Se, nå er det der!

## Hva skjer her egentlig?

Du husker kanskje fra [En dekonstruert database](/en-dekonstruert-database/)
at vi har direkte tilgang på indeksene i Datomic. Det er ikke et veldig praktisk
API å bruke, men la oss prøve for moro skyld:

```clj
(:db/id poststed)

;; => 17592186315131
```

Dette er entity-id'en til poststedet. Vi kan slå det opp i `:eavt`-indeksen:

```clj
(first
 (d/datoms db :eavt 17592186315131 :poststed/navn))

;; => #datom[17592186315131 92 "Gamle Fredrikstad" 13194139802457 true]
```

Vi fant nå datom-et, og kan slå opp verdien med `.v`. Totalt sett blir det sånn:

```clj
(.v
 (first
  (d/datoms db :eavt (:db/id poststed) :poststed/navn)))

;; => "Gamle Fredrikstad"
```

Sånn er det det funker under panseret. Jeg antar at vi er enige om at det er
koseligere å skrive dette:

```clj
(:poststed/navn poststed)

;; => "Gamle Fredrikstad"
```

Så la oss smelle igjen panseret og se på litt mer praktisk bruk.

## Alle gatene i kommunen

Nå skal vi gjøre noe sprøtt. Ser her:

```clj
(count (:gate/_poststeder poststed))

;; => 47
```

Hva i all verden?

En gate har poststeder. Det representeres med attributtet `:gate/poststeder`.
Hvis du har en gate, så kan du finne alle poststedene den befinner seg i.

Vi har ikke en gate. Vi har et poststed. Men vi kan følge referansen *den andre
veien*.

Syntaksen for det er å slenge på en `_`.

Det er altså 47 gater på poststed 1630 Gamle Fredrikstad. Hva heter de, mon tro?

```clj
(->> (:gate/_poststeder poststed)
     (map :gate/navn))

;; => ("Vaterland"
;;     "Heibergs gate"
;;     "General Ohmes vei"
;;     "Turveien"
;;     "Katrineborg"
;;     "Klokkerstuveien"
;;     "Lammers gate"
;;     ...)
```

Stilig! Husk, dette er data rett fra databasen. Ingen SQL har blitt skrevet. Vi
bare titter litt på dataene med kode.

La oss avslutte med å finne alle gatene i kommunen:

```clj
(->> (:gate/_poststeder poststed)
     (map :gate/kommune)
     (set))

;; => #{{:db/id 17592186047598}}
```

Vi starter altså på et poststed, finner alle gatene på det poststedet, slår
videre derfra opp kommunen for hver gate, og putter det i et sett.

Det er heldigvis bare én kommune i dette tilfellet (Fredrikstad!), men enkelte
steder i landet har de rotet det til slik at poststeder går på tvers av
kommunegrenser. Derav denne ekstra krøllen.

La oss gå fra dette settet med kommuner, tilbake til alle gatene i hele
kommunen. Det blir veldig stilig!

```clj
(->> (:gate/_poststeder poststed)
     (map :gate/kommune)
     (set)
     (mapcat :gate/_kommune)
     (count))

;; => 1456
```

Jadda!

Når vi har en kommune og slår opp `:gate/_kommune`, så følger vi igjen
referansen andre veien og finner alle gater i kommunen.

Ettersom vi har flere kommuner, så slår vi sammen alle listene med `mapcat`
(også kjent som flatmap i enkelte andre språk).

Med det kan vi si at det er 1456 gater i Fredrikstad kommune, og alt det ut ifra
et postnummer, en database, og litt kode.
