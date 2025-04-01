:page/title Rene funksjoner og uforanderlige data — to bestevenner
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2025-04-01T09:00:00"
:blog-post/tags [:funksjonell-programmering :fk-is]
:blog-post/description

Hva er pure functions og immutable data, og hvorfor er de så inmari bra greier?

:blog-post/body

Du skulle bare visst hvor mange ganger jeg begynte å skrive om [den spennende
arkitekturen vår](/fk-is/) det siste året, men forsøket strandet hver gang. Det
ble rett og slett for vanskelig å forklare uten å kunne referere til det
nydelige tospannet av

- rene funksjoner (pure functions), og
- uforanderlige data (immutable data).

For det er dette som er kruttet!

Så nå skal jeg gjøre et ærlig forsøk på å beskrive hva disse er, og hvorfor de
er så inmari bra greier.

## Immutable data

Du har nok vært borte i uforanderlige data før -- kanskje uten å tenke over det. Her er et eksempel fra Java:

```java
String navn = "Magnar";
navn.toLowerCase() // => "magnar"
navn // => "Magnar"
```

Stringen `navn` endret seg ikke, selv om jeg kjørte `.toLowerCase` på den.
Sjokkerende? Nei, jeg tror ingen Java-utviklere syns det er noe rart, til tross
for at fleste andre objektene i språket oppfører seg annerledes.

Istedenfor å mutere originalen, så får vi en ny string tilbake. Det samme
gjelder `java.time.LocalDate`:

```
LocalDate today = LocalDate.parse("2025-04-01");
LocalDate tomorrow = today.plusDays(1);
today.toString() // => "2025-04-01"
```

Dette er immutable API-er.

Akkurat slik fungerer uforanderlige data i Clojure også -- bare at det gjelder
for alt, inkludert samlinger med data: lister, maps og sett.

```clj
(def favoritter [:lapskaus :taco])
(conj favoritter :pizza) ;; => [:lapskaus :taco :pizza]
favoritter ;; => [:lapskaus :taco]
```

Hvis du legger noe til i en liste i de fleste andre språk, så muterer du lista.
For eksempel i JavaScript:

```js
let favoritter = ["lapskaus", "taco"];
favoritter.push("pizza");
favoritter // => ["lapskaus", "taco", "pizza"]
```

Se hva som skjer når jeg prøver å printe ut lista til konsollet:

<img src="/images/artig-med-objekter.png" title="Et utsnitt fra Chrome konsollet
hvor pizza av en mystisk grunn allerede er i lista, før jeg legger den til på
linja under." class="img"></img>

Ser du hva som har gått galt her? Tilsynelatende er det pizza i lista allerede
før jeg har lagt den til. Er det tidsreisende som besøker oss? I så fall,
hvorfor dro de ikke på festen til Stephen Hawking?

Neida, saken er jo den at konsollet har tatt vare på en referanse til
`favoritter` - en liste som kan endre seg. Hvis jeg skal vite hva som var i
lista når koden kjørte, så må jeg ta et øyeblikksbilde på noe vis.
Kanskje noe sånt?

```js
console.log(JSON.stringify(favoritter));
```

To lister med samme elementer er altså ikke samme verdi i JavaScript:

```js
let favoritter = ["lapskaus", "taco"];
favoritter == ["lapskaus", "taco"] // => false
```

Vi har endt opp med to separate lister, som bare tilfeldigvis har de samme
elementene akkurat her og nå.

Med uforanderlige data så er listene like. Eller for å dra det lenger: For alle
praktiske formål så er de den samme lista.

```clj
(def favoritter [:lapskaus :taco])
(= favoritter [:lapskaus :taco]) ;; => true
```

Det faktum at de er like er verdifullt:

Jeg kan printe dem. Jeg kan se på dem. Jeg kan kopiere dem ut fra konsollet og
lime dem inn i min egen kode. Jeg kan sende dem til en kollega på Slack. De er
like hele veien. Og de fortsetter å være like -- inn i evigheten.

Kort fortalt så er de *verdier*.

## Pure functions

Rene funksjoner er det nærmeste vi kommer matematiske funksjoner i daglig kode.
De har to strenge begrensninger:

- De jobber bare med verdiene de får tilsendt som inn-parametere.
- De gjør ingenting annet enn å gi en retur-verdi.

Noen ganger kan det være enklere å forstå konsepter etter
[eliminasjonsmetoden](https://www.youtube.com/watch?v=EosJOmazz3Q&list=OLAK5uy_klU4fZJI7jtHabnry7BxYADsbVpVzYmyY&index=12).
La oss prøve.

En ren funksjon vil aldri:

- spørre i databasen
- gjøre et http-kall
- sjekke systemklokka
- slå opp i et register

Hvis den skal ha informasjon, så må det sendes inn. Et viktig poeng er dette:
Hvis jeg kaller funksjonen med de samme parameterne så får jeg *alltid tilbake
samme svar*.

Observer at en muterbar liste (slik som i JavaScript-eksempelet over) *kan endre
seg* - og dermed ikke garanterer at jeg alltid får samme svar. Det *er ikke* en
verdi. Med andre ord: uforanderlige data er en forutsetning for rene funksjoner.

Okay, vi fortsetter. En ren funksjon vil aldri:

- skrive til databasen
- sende en e-post
- publisere en melding på en strøm
- oppdatere en key-value store

Alt den gjør er å returnere en verdi.

## Jeg syns du sa du skulle si hvorfor det var *bra*, jeg.

Jeg gjør det! Eller prøver, i hvert fall.

Det er nemlig disse begrensningene som gjør det bra.

Det er begrensningene som gjør at koden blir så lett å forstå. Å følge. En ren
funksjon finner ikke på noe sprell. Alt som skal til for å forstå den rene
funksjonen er *rett der*.

Koden er rett der. Dataene er rett der. Effekten av å kjøre koden er rett der.

#### Et lite eksempel kan ikke skade på dette tidspunktet

La oss si at det har oppstått en feil i prod.

- Ettersom jeg bruker *uforanderlige data*, så kan jeg kopiere verdiene fra prod
  over til min lokale maskin. Og vite at de er *like* de som kjørte i prod.

Nøyaktig samme data som i prod, tilgjenglig på min maskin, bare ved helt enkel
klipp og lim.

Ettersom mine rene funksjoner bare opererer på sine inn-parametere så har jeg
allerede alle dataene. Jeg trenger ikke å:

- prøve å gjenskape noen situasjon i databasen.
- bekymre meg for om feilen er på grunn av en race condition.
- lure på om en http-tjeneste var nede.
- mistenke at det har å gjøre med klokkeslettet det skjedde.

Ettersom mine rene funksjoner bare returnerer en verdi, så

- skriver ikke funksjonen til et annet sted i minnet eller disk eller en
  tjeneste, som jeg må løpe etter og sjekke.
- skjer det ikke noe annet muffens jeg må passe på -- ingen e-poster blir sendt.

Nei, jeg kan bare titte på returverdien, og dermed se alt funksjonen gjorde.

Totalt sett, med uforanderlige data og rene funksjoner så kan jeg være trygg på
at jeg klarer å gjenskape feilen fra prod uten noe trøbbel.

Okay, det var eksempelet, men det samme gjelder såklart også når jeg bare skal
forsøke å skjønne koden - selv om det ikke har gått galt i prod. Ettersom alt
foregår rett i nærheten, så trenger jeg ikke gå så langt for å forstå. Ettersom
dataene er verdier, så kan jeg *se på dem*.

Hva mener jeg med "se på dem"?

Jo, altså: Jeg kan se på dem med øynene mine. Som tekst i editoren min, eller et
konsoll, eller en logg, eller et trace. Det er ikke noe objekt som enkapsulerer
dataene sine. Det er ingen levende, forandelig samling som jeg må se på på
nøyaktig riktig tidspunkt. Nei, det er bare maps, sett, lister, verdier.

Tenk på hvor mye hyggeligere det er å se på en JSON-payload, istedenfor å
forsøke å forstå hva som foregår i en kjørende JavaScript-prosess.

Det er forskjellen.

## Til slutt

Tenk at jeg skrev alt dette her uten å nevne hvor lett det er å skrive tester
for slik kode. Det er åpenbart en annen kjempefordel, men jeg gjetter på at det
ikke er det du er mest opptatt av akkurat nå.

Jeg vil heller tro at du tenker: "men jeg MÅ jo skrive til databasen ... KAN jeg
ikke det da?" Joda, vi kommer til det. Dette er jo bare del 1 av serien om den
kule arkitekturen vår. 😄

<br><br><br>

PS!

Dette er ingen lett ting å skulle forklare, og dermed heller ikke å skulle
forstå. Det tok meg lang tid å internalisere de greiene her, så hvis du syns det
er utfordrende så skjønner jeg det godt. Bli med videre i serien, så gir det
forhåpentligvis mer mening etterhvert. 🫶
