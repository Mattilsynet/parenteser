:page/title En liten titt på Datalog
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-05-07T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:blog-post/description

Datalog er et logisk spørrespråk som ligger et sted mellom Prolog og SQL, noe
som unektelig høres ganske esoterisk ut, men som viser seg å være ganske greit
når man har tatt en liten titt på det.

:blog-post/body

Datalog er et logisk spørrespråk som ligger et sted mellom Prolog og SQL, noe
som unektelig høres ganske esoterisk ut, men som viser seg å være ganske greit
når man har tatt en liten titt på det.

### Kode vs spørring

I en [tidligere bloggpost](/alle-gatene-i-kommunen/) i denne serien så vi dette
kodeeksempelet:

```clj
(:poststed/navn
 (d/entity db [:poststed/postnummer "1630"]))

;; => "Gamle Fredrikstad"
```

Datomic støtter også å finne data ved hjelp av Datalog, hvor spørringen ville
sett slik ut:

```clj
[:find ?navn .
 :where
 [?poststed :poststed/postnummer "1630"]
 [?poststed :poststed/navn ?navn]]
```

Hva skjer her egentlig? Jo, vi ber om å finne én verdi, som vi kaller `?navn`.

- Spørsmålstegnet i starten av symbolet indikerer til Datalog at dette er en
  variabel.
- Punktumet `.` indikerer at vi bare vil ha én verdi, ikke flere.

Så ramser vi opp noen klausuler som må oppfylles. Disse følger mønsteret vi
kjenner fra [første bloggpost](/smak-av-datomic/) i serien:

```
[entitet, attributt, verdi]
```

Altså,

- Finn en entitet `?poststed` med attributtet `:poststed/postnummer` med
  verdi `"1630"`.

- Finn denne samme entiteten `?poststed` sitt attributt `:poststed/navn`, og bind `?navn` til verdien av attributtet.

Så kan vi be Datomic gjennomføre spørringen, slik:

```clj
(d/q '[:find ?navn .
       :where
       [?poststed :poststed/postnummer "1630"]
       [?poststed :poststed/navn ?navn]]
     db)

;; => "Gamle Fredrikstad"
```

Her kaller vi funksjonen `datomic.api/q` med spørringen vår og databasen `db`,
og får svaret tilbake. Fortsatt Gamle Fredrikstad!

### En forvikling i datamodellen

I tiden siden den forrige bloggposten ble skrevet, så har datamodellen vår
endret seg. Vi oppdaget at "poststed" var en fullverdig entitet som kunne ha
flere postnummere.

Altså:

- Et poststed er identifisert av sitt navn.
- Hvert poststed kan ha mange postnummere.

Med denne oppdaterte modellen, så ser spørringen vår sånn ut:

```clj
(d/q '[:find ?navn .
       :where
       [?postnummer :postnummer/nummer "1630"]
       [?postnummer :postnummer/poststed ?poststed]
       [?poststed :poststed/navn ?navn]]
     db)

;; => "Gamle Fredrikstad"
```

Her ser vi at det er tre klausuler.

- Det finnes en entitet `?postnummer` med `:postnummer/nummer` med verdi `"1630"`.
- Den samme entiteten `?postnummer` har en referanse til en annen entitet
  `?poststed` via `:postnummer/poststed`-attributtet. Det er en slags join.
- Denne andre entiteten `?poststed` har et `?navn`.

Merk at alle variablene er navn som vi har funnet på. De kunne
like gjerne vært `?a`, `?b` og `?c`:

```clj
(d/q '[:find ?a .
       :where
       [?b :postnummer/nummer "1630"]
       [?b :postnummer/poststed ?c]
       [?c :poststed/navn ?a]]
     db)

;; => "Gamle Fredrikstad"
```

Poenget her er at de brukes til å vise hvilke deler av `[e, a, v]`-triplene som
er de samme. De binder klausulene sammen.

## Parameteriserte spørringer

Vi kan også lage oss en spørring som tar imot postnummeret. Det kan jo tenkes at
vi lurer på noen andre steder også. Da ser det slik ut:

```clj
(d/q '[:find ?navn .
       :in $ ?nummer
       :where
       [?postnummer :postnummer/nummer ?nummer]
       [?postnummer :postnummer/poststed ?poststed]
       [?poststed :poststed/navn ?navn]]
     db "1630")

;; => "Gamle Fredrikstad"
```

I tillegg til `:find` og `:where`, så har vi nå `:in`. Denne seksjonen
beskriver inn-parameterene. Legg merke til at `d/q` tar to parametere i tillegg
til selve spørringen: `db` og `"1630"`. Dette speiles av `$` og `?nummer`.
Dollartegnet er altså databasen. Mer om det i en senere bloggpost.

Nå som vi har parameterisert spørringen, kan vi lage oss en funksjon:

```clj
(defn finn-stedsnavn-på-postnummer [db nummer]
  (d/q '[:find ?navn .
         :in $ ?nummer
         :where
         [?postnummer :postnummer/nummer ?nummer]
         [?postnummer :postnummer/poststed ?poststed]
         [?poststed :poststed/navn ?navn]]
       db nummer))

(finn-stedsnavn-på-postnummer db "1605") ;;=> "Fredrikstad"
(finn-stedsnavn-på-postnummer db "1412") ;;=> "Sofiemyr"
```

Men la meg stille deg et spørsmål: Er det egentlig nødvendig å pakke denne
fine datastrukturen inn i en opak funksjon? Er ikke noe av poenget med et
spørrespråk at det er deklarativt og inspiserbart? At det er data?

La oss prøve på det:

```clj
(def finn-stedsnavn-på-postnummer
  '[:find ?navn .
    :in $ ?nummer
    :where
    [?postnummer :postnummer/nummer ?nummer]
    [?postnummer :postnummer/poststed ?poststed]
    [?poststed :poststed/navn ?navn]])

(d/q finn-stedsnavn-på-postnummer db "1630") ;;=> "Fredrikstad"
(d/q finn-stedsnavn-på-postnummer db "1412") ;;=> "Sofiemyr"
```

Rock 'n roll! 🤘

## En sjokkerende tvist på slutten

Hva om vi har lyst til å finne alle postnummere på et poststed?

```clj
(def finn-postnummere-for-stedsnavn
  '[:find [?nummer ...]
    :in $ ?navn
    :where
    [?postnummer :postnummer/nummer ?nummer]
    [?postnummer :postnummer/poststed ?poststed]
    [?poststed :poststed/navn ?navn]])

(d/q finn-postnummere-for-stedsnavn db "Gamle Fredrikstad")

;;=> ["1636" "1634" "1633" "1632" "1630" "1639" "1637"]
```

Hvordan skiller denne spørringen seg fra den forrige?

- Istedenfor å finne `?navn .` (ett navn), så finner vi nå `[?nummer ...]` (flere nummere).

- Vi tar imot `?navn` som parameter istedefor `?nummer`.

Men hva med klausulene?

De er sørenmeg kliss like!

Vi beskriver jo den samme sammenhengen, bare med en annen ukjent.

M. Night Shyamalan, ta deg en bolle!
