:page/title Hvor i nærheten kan jeg spise?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-03-04T08:30:00"
:blog-post/tags [:geografi :datamodellering :sok]
:blog-post/description

"Finn alle serveringssteder ca 1km fra meg" er en vanlig problemstilling i vårt
domene. En problemstilling som lar seg overraskende greit løse blant annet med
geohashes.

:blog-post/body

"Finn alle serveringssteder ca 1km fra meg" er en vanlig problemstilling i vårt
domene. En problemstilling som lar seg overraskende greit løse med geohashes,
litt matematikk og litt kreativitet.

## Hva sa De? Geohash?

Et serveringssted har en adresse. Som offisielt register har
[matrikkelen](https://www.kartverket.no/eiendom/eiendomsgrenser/matrikkelen-norgeseiendomsregister)
et geopunkt for hver eneste adresse i Norge. Et geopunkt er et koordinat på
jordoverflaten, feks angitt med [lengdegrad](https://snl.no/lengdegrad) og
[breddegrad](https://snl.no/breddegrad).

Hvis vi rister av oss litt presisjon og regner om punktet til en geohash får vi
en alternativ representasjon med noen interessante egenskaper. Geohasher deler
jordens overflate opp i et rutenett med firkanter og gir hver av dem en tekstlig
id, feks `u4xsu`, som dekker deler av Oslo.

<img class="img" src="/images/geohash-u4xsu.png" alt="Geohash som dekker deler av Oslo">

Lengden på en geohash sier noe om størrelsen på firkanten, kortere hash gir et
større område. Id-ene er også hierarkiske, som betyr at geohashen `u4xsu` finnes
innenfor det større området angitt av `u4x`. Under ser du vårt eksempelpunkt
sammen med de nærmeste naboene.

<img class="img" src="/images/geohash-grid.png" alt="Geohash-firkanter over østlandet">

## Størrelse på firkantene

Som du ser av bildet over er ikke firkantene kvadratiske. Det har seg
slik at planeten vi bor på er mer eller mindre kuleformet(!) og en kule lar seg
ikke villig dele opp i kvadrater. Lengden på geohashen gir oss høyden på
firkanten, mens bredden blir smalere jo lenger nord vi kommer.

Eksempelet over, `u4xsu` har 5 tegn, og gir en firkant som er 4.89km i høyden,
og en god del mindre i bredden. Ved å øke til 6 tegn får vi firkanter som er 610m
høye, og "opptil 1220km brede". Det betyr at firkanter i denne oppløsningen er
bredere enn de er høye nærmere ekvator, ganske kvadratiske her på østlandet, og
noe smalere lenger nord:

<img class="img" src="/images/geohash-grid2.png" alt="Mindre Geohash-firkanter over østlandet">

Færre tegn gir større områder: 4 tegn gir firkanter med 19.5km høyde, og 3 tegn
gir 156km høyde.

## Praktisk bruk av geohashes

Hvordan hjelper disse firkantene oss å finne frem til serveringsstedene våre?
Steg 1 er å lagre en geohash for alle lokasjonene vi er interessert i. Siden
geohashes er hierarkiske så trenger vi bare lagre én lang streng. 12 tegn er
makslengde for en geohash. Ikke bra nok for å navigere i en maurtue, men godt
nok for å finne et sted å spise.

For å finne steder i nærheten må vi ha et utgangspunkt. La oss si at jeg står på
punktet angitt av:

```
[59.91386880, 10.75224540]
```

Vi må nå beregne geo-hashen til dette punktet med ønsket presisjon. Siden vi vil
ha 1km avstand så må jeg enten bruke 5 tegn som gir meg opptil 4.89km², eller 6
tegn som gir meg ca 610m².

6 tegn gir meg geohashen `u4xsud`. For å finne alle serveringsstedene som
befinner seg i samme firkant som meg kan jeg rett og slett søke etter alle
stedene i databasen som har en geohash som starter på "min" hash.

I Java betyr dette rett og slett å loope over alle stedene og ta vare på de der
`geoHash.startsWith("u4xsud")`. Med Datomic kan det se sånn ut:

```clj
(defn finn-adresser-med-geohash [db geohash]
  (d/q
   '[:find ?a
     :in $ ?hash
     :where
     ;; Finn adresser som er i bruk på et lokale
     [?l :lokale/adresse ?a]
     ;; Finn geo-hashen til adressen
     [?a :posisjon/geohash ?gh]
     ;; Inkluder adressen om geohashen
     ;; starter med søke-hashen vår
     [(clojure.string/starts-with? ?gh ?hash)]]
   db geohash))
```

Dette tar oss noe på vei. Men hva om jeg befinner meg helt i hjørnet på en
firkant?

<img class="img" src="/images/geohash-hjorne.png" alt="Punkt i hjørnet på en geohash">

Om jeg bare finner steder innenfor denne firkanten går jeg glipp av steder som
er et steinkast unna, men som ligger i en annen firkant. Løsningen er å lete
ikke bare i "min" firkant, men også de nærmeste naboene. Det får vi til med litt
matematikk, som vi kan få levert av et bibliotek.

Vi bruker [factual/geo](https://github.com/factual/geo) i Clojure, som igjen
bruker Java-biblioteket [geohash-java](https://github.com/kungfoo/geohash-java)
for geohash-beregningene, og henter ut alle disse firkantene:

<img class="img" src="/images/geohash-naboer.png" alt="Alle naboene til en geohash">

Nå kan vi søke opp alle stedene som ligger innenfor en av disse firkantene, som
fortsatt er en rask og enkel operasjon. Datomic-spørringen over trenger bare
minimale justeringer for å gi oss svaret:

```clj
(defn finn-adresser-med-geohash [db geohash-er]
  (d/q
   '[:find ?a
     :in $ [?hashes ...]
     :where
     ;; Finn adresser som er i bruk på et lokale
     [?l :lokale/adresse ?a]
     ;; Finn geo-hashen til adressen
     [?a :posisjon/geohash ?gh]
     ;; Inkluder adressen om geohashen
     ;; starter med søke-hashen vår
     [(clojure.string/starts-with? ?gh ?hashes)]]
   db geohash-er))
```

### Avstand, schmavstand

Vi ønsket oss steder som lå ca 1km unna. Er det det vi nå finner? Vel, ikke
helt. Dersom vi befinner oss midt i geohashen vi søker med så treffer vi ganske
bra: ca 900-2100m i hver retning. Som vi så, kan vi like gjerne befinne oss i
hjørnet på firkanten. Da finner vi steder som er mellom 600m og 1700m unna.

For å øke søkerommet kan vi enten legge til enda en sirkel med naboer (altså
søke i 4x4 firkanter), eller bruke en kortere geohash, feks 5 tegn. Vi søker da
i et større område enn vi egentlig ønsker, men kan bruke mer presise verktøy for
å snevre resultatet inn igjen.

## Endelig algoritme

Algoritmen vi landet på for å finne serveringsstedene ser sånn ut:

1. Bruk ønsket radius til å finne en passende lengde på geohashen
2. Finn geohashen for punktet vi søker fra
3. Finn nabo-geohashene
4. Finn alle serveringsstedene med disse 9 hashene
5. Snevre inn søket til ønsket avstand ved å beregne nøyaktig avstand

For å beregne en mer nøyaktig avstand bruker vi
[Haversine-formelen](https://en.wikipedia.org/wiki/Haversine_formula). Denne er
for tung å bruke på alle adressene vi har, men etter å ha snevret ned søkerommet
med geohashes sitter vi igjen med få nok steder til at vi kan ta oss tid til å
finregne på det.

## PS!

Det er verdt å nevne at det er noen grensetilfeller med
[geohashes](https://en.wikipedia.org/wiki/Geohash) hvor man må holde tunga rett
i munnen, men så lenge man befinner seg i Norge er det stort sett smale
firkanter som er det viktigste å merke seg.

Geohashes er blant verktøyene som brukes for nærhet i mange søkemotorer.

Rutenettene kommer fra [www.geohash.es/](https://www.geohash.es/).
