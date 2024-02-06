:page/title Om nøkler og deres bruk
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-02-06T09:00:00"
:blog-post/tags [:clojure :datomic]
:blog-post/description

I Clojure kan du sette navnerom på nøkler -- en tilsynelatende triviell detalj
med store implikasjoner. La oss se litt på hvordan det hjelper oss med
datamodellering.

:open-graph/description

Et gløtt på datamodellering i Clojure og Datomic med nøkler med navnerom.

:blog-post/body

I Clojure modellerer vi gjerne data med datatypene som språket gir oss: maps,
lister, sett, strenger, tall, keywords og booleans. De er alle immutable og kan
trygt deles.

Maps med keywords som nøkler er den vanligste måten å representere data på i
Clojure, eksempelvis denne norske kommunen:

```clj
{:kode "3107"
 :navn "Fredrikstad"}
```

Keywords (`:kode`) er en slags spesialisering av en streng for det jeg liker å
kalle "teknisk bruk". Der strenger brukes til tekstlig innhold, brukes keywords
som nøkler og korte strenger med spesifikk betydning (eksempelvis verdier i en
enum). For å støtte opp under deres funksjon som nøkler kan keywords brukes som
funksjoner, for å slå seg selv opp i et map:

```clj
(def kommune
  {:kode "3107"
   :navn "Fredrikstad"})

(:navn kommune)
;;=> "Fredrikstad"
```

Clojure keywords kan også ha et navnerom. Ved første øyekast ser det kanskje ut
som en litt mer jålete og/eller omstendig måte å navngi nøklene på:

```clj
(def kommune
  {:kommune/kode "3107"
   :kommune/navn "Fredrikstad"})

(:kommune/navn kommune)
;;=> "Fredrikstad"
```

Når et keyword har navnerom er det en etablert konvensjon i Clojure at det er
**globalt**. Det betyr at `:kommune/navn` alltid kan forventes å ha samme
semantikk, uansett hvor i kodebasen det dukker opp. Det samme kan ikke sies om
`:navn`.

## Eierskap

Navnerom kan si noe om konteksten til datapunktet. For eksempel: Hvis jeg vil
lagre kommunen i en database må jeg gi den en unik id. Å lene seg for hardt på
en naturlig id kan fort gå galt. Det kan ikke ha vært noe gøy å bruke
kommunenummer som primærnøkkel gjennom alle sammenslåingene i 2017, 2017 og
2020.

Kommunen får dermed en syntetisk id som handler mer om databasen min enn om
selve kommunen. Navnerom kan tydeliggjøre dette skillet:

```clj
{:db/id 17592186046486
 :kommune/kode "3107"
 :kommune/navn "Fredrikstad"}
```

Her har vi én entitet, men det kommer tydelig frem at id-en ikke er kommunens ID
i domenet vårt, men heller en syntetisk id som benyttes av databasen.

## Struktur

Med navnerom får keywords også innebygget struktur. Det er dermed åpent for å
beskrive et stykke data langs flere akser uten å måtte ty til nøsting for å
formidle strukturen.

På [smilefjes.mattilsynet.no](https://smilefjes.mattilsynet.no/) har vi [egne
sider for kommuner](https://smilefjes.mattilsynet.no/kommune/fredrikstad/). En
måte å modellere dette på er å si at en side har en kommune:

```clj
{:page/url "/kommune/fredrikstad/"
 :page/kind :page.kind/kommune-page
 :page/kommune {:kommune/kode "3107"
                :kommune/navn "Fredrikstad"}}
```

Dette er forsåvidt greit nok, men det er litt rart at en side har en kommune.
Det kunne selvfølgelig vært modellert mer generisk, men med navnerom kan vi
formidle strukturen samtidig som vi flater ned dataene. Vi kan rett og slett si
at en kommune har en side-URL:

```clj
{:db/id 17592186046486
 :kommune/kode "3107"
 :kommune/navn "Fredrikstad"
 :page/url "/kommune/fredrikstad/"
 :page/kind :page.kind/kommune-page}
```

Her er all data samlet på ett nivå, uten at vi har mistet hverken struktur eller
eierskap. Det finnes en side for kommunen Fredrikstad på URL-en
`/kommune/fredrikstad/`, men det kommer tydelig frem av navngivningen at URL-en
ikke er en iboende egenskap med kommunen.

Er det en side? Ja. Er det en kommune? Også ja. Og nei. Det kommer an på
konteksten du stiller spørsmålet i.

Det er ikke sikkert du er enig med meg i at dette er vakkert. Det er i hvert
fall litt uvant. Men bare vent! Jeg kommer sterkt tilbake med litt om
fortreffeligheten ved flate data i et senere innlegg.

## PS!

Men hvordan skal vi lagre dette, da? Jo, her kommer
[Datomic](/smakebiter-av-datomic/) (igjen) til sin rett. Datomic lagrer data
[som attributter](/smak-av-datomic/). Faktisk er eksempelet over hentet rett ut
fra databasen til [smilefjes-sidene](https://smilefjes.mattilsynet.no/).
