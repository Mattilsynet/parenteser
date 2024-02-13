:page/title 3 småbiter om hvor latterlig lett det var å legge inn kommuner
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-02-09T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:blog-post/description

Da vi nylig lagde egne sider for hver kommune på den nye smilefjes-siden, tok
jeg meg selv i å humre fornøyd gjentatte ganger. Det var en fin dag i selskap
med Datomic.

:blog-post/body

Vi slengte nylig sammen nye sider for [smilefjesplakater på
nett](https://smilefjes.mattilsynet.no/). Det var litt hastverk fordi en on-prem
server skulle skrus av, så da var det ekstra gøy å levere hele greia på en uke.
I likhet med [Matvaretabellen](https://www.matvaretabellen.no) og denne bloggen
så lagde vi igjen en [statisk side](/lange-flate-filer/) med Christian sin
[Stasis Powerpack](https://github.com/cjohansen/powerpack). Det hjalp
utvilsomt bra på farta.

En ting som manglet på den gamle siten var en oversikt over alle spisesteder per
kommune. Ettersom Posten Bring har vært hjelpsomme nok til å legge ut en
[oversikt over alle postnummer med tilhørende
kommuner](https://www.bring.no/tjenester/adressetjenester/postnummer/postnummertabeller-veiledning),
så bestemte vi oss for å stunte ut
[kommunesider](https://smilefjes.mattilsynet.no/kommune/fredrikstad/) i siste
lita. Det skulle vise seg å være latterlig enkelt. Her er tre små smakebiter fra
Datomic i den forbindelse.

## Småbit 1 -- importen

Vi hadde allerede dratt inn alle smilefjestilsyn og tilhørende spisesteder fra
[datasettet på Data
Norge](https://data.norge.no/datasets/288aa74c-e3d3-492e-9ede-e71503b3bfd9). Det
så (delvis) slik ut:

```clj
(let [m (zipmap csv-header csv-line)]
  {,,,
   :spisested/navn (:navn m)
   :spisested/orgnummer (:orgnummer m)
   :spisested/poststed {:poststed/postnummer (:postnr m)}
   ,,,})
```

Poststed er en egen entitet, hvor `:poststed/postnummer` er satt opp som unikt
identifiserende:

```clj
;; Her fra Datomic skjemaet:

{:db/ident :poststed/postnummer
 :db/valueType :db.type/string
 :db/unique :db.unique/identity ;; <--
 :db/cardinality :db.cardinality/one}
```

Datomic håndterer slike attributter spesielt, slik at dette ...

```clj
 {:poststed/postnummer (:postnr m)}
```

... blir til en upsert. Altså, den bruker ekisterende poststed-entitet hvis den
allerede finnes -- og hvis ikke så lages det en ny.

Og fordi poststedet deklareres sånn her ...

```clj
 :spisested/poststed {:poststed/postnummer (:postnr m)}
```

... så kobles den samtidig til spisestedet.

Men her er det jo ingen kommuner. De kommer fra Bring sin CSV, som vi også
importerer, men i et eget steg. Ser omtrent sånn ut:

```clj
{:poststed/postnummer (:postnummer m)
 :poststed/navn (:poststed m)
 :poststed/kommune {:kommune/kode (:kommunekode m)
                    :kommune/navn (:kommunenavn m)}}
```

Igjen har vi upserts - to stykk. Hvis det allerede finnes et poststed med dette
postnummeret, så brukes det - men berikes da med navn og kommune. Hvis det
allerede finnes en kommune med den koden, så brukes den.

Og dermed får vi flettet sammen spisesteder, via poststed, til kommune - bare
via upserts, uten at jeg "trengte gjøre noe" for å koble dem sammen.

Herlig.

PS! Jeg skjønner at jeg må skrive en liten smakebit om Datomic sitt fin-fine
system for å beskrive transaksjoner av data i dette formatet. Det kommer!

## Småbit 2 -- hva er URL-en?

Jeg har [tidligere skrevet om](/smak-av-datomic/) at Datomic modellerer sine
data som entiteter og attributter, ikke i tabeller. Når du er vant til tabeller,
så kan det føles litt som at alle entitetene bare flyter ustrukturert rundt. Men det
er bra, det! Verden er ikke firkanta.

Hvis du leste Christian sin [bloggpost om nøkler og deres bruk](/nokler/), så så
du et eksempel på dette i praksis:

```clj
{:db/id 17592186046486
 :kommune/kode "3107"
 :kommune/navn "Fredrikstad"
 :page/uri "/kommune/fredrikstad/"
 :page/kind :page.kind/kommune-page}
```

Her har vi en entitet som i visse kontekster er en kommune, i andre kontekster
en side. Datomic lar deg modellere dette uten noe stress.

Så når jeg satt og skulle lage denne lenken ...

```clj
[:a.hover:underline {:href "..."}
  (:kommune/navn kommune)]
```

... så tok jeg meg selv i å lure "Okay, men hva er egentlig lenken til en
kommuneside?"

Jeg begynte å lete etter en funksjon som kunne fortelle meg det. Kanskje lage
en `(get-kommune-url kommune)` funksjon en plass.

Så slo det meg:

```clj
[:a.hover:underline {:href (:page/uri kommune)}
  (:kommune/navn kommune)]
```

Haha! Så enkelt kan det være.

## Småbit 3 -- søket

Litt senere ville vi legge inn kommunenavn også i søket på forsiden. Navnet
passet bra sammen med annen adresseinformasjon i indeksen. Sånn ser den
relevante koden ut:

```clj
(defn get-searchable-address [spisested]
  (->> [(-> spisested :spisested/adresse :poststed)
        (-> spisested :spisested/adresse :linje1)
        (-> spisested :spisested/adresse :linje2)]
       (filter not-empty)
       (str/join " ")))
```

Ja ha, denne funksjonen tar jo bare imot spisestedet. Ingen database å slå opp
kommunen i.

Tenk litt på hvordan du ville gått frem for å også sende kommunen ned til denne
funksjonen.

Kanskje hadde du måtte legge til en JOIN i en SQL-spørring et annet sted? Da
måtte du i så fall gjort en dobbel join, fra spisested til poststed til kommune.

Kanskje du måtte lagt til kommunenavnet i en slags spisested DTO eller et
Spisested-objekt?

Kanskje hadde du sendt inn både kommunen og spisestedet til denne funksjonen?

Okay, nok tankespinn. Her er det vi endte opp med å gjøre:

```clj
(defn get-searchable-address [spisested]
  (->> [(-> spisested :spisested/adresse :poststed)
        (-> spisested :spisested/adresse :linje1)
        (-> spisested :spisested/adresse :linje2)
        (-> spisested :spisested/poststed :poststed/kommune :kommune/navn)]
       (filter not-empty)
       (str/join " ")))
```

Haha!

Med Datomic sitt entitets-API, som er bygget på den [direkte tilgangen til
indekser](/en-dekonstruert-database/) vi har snakket om før, så er hele
databasen navigerbar på denne måten.

Ingen SQL. Ingen ugudelige INNER JOINs. Bare data.
