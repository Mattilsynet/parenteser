:page/title Hvordan jeg lærte å slutte å bekymre meg og elske API-et
:blog-post/published #time/ldt "2023-10-27T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:bakoverkompatibilitet :produktivitet]
:blog-post/description

Som en oppfølging til mitt [innlegg om
bakoverkompatibilitet](/bakoverkompatibilitet/) har jeg i dag noen praktiske
tips til hvordan du kan designe HTTP API-er som brukerne dine får en god
opplevelse med over tid.

:open-graph/description

Som en oppfølging til mitt innlegg om bakoverkompatibilitet har jeg i dag noen
praktiske tips til hvordan du kan designe HTTP API-er som brukerne dine får en
god opplevelse med over tid.

:blog-post/body

De aller fleste applikasjonsutviklere forvalter i dag en eller flere
HTTP/REST-API-er, enten det er til kommunikasjon mellom mikrotjenester, fra
backend til frontend, eller til eksterne team og kunder. Jeg har noen konkrete
tips til design av slike API-er for å sørge for at de kan utvikle seg på en
[bakoverkompatibel](/bakoverkompatibilitet/) måte.

## Aldri returner en ren liste

Du har implementert et API for å søke. Det skal returnere søkeresultater, så du
går for en god gammel liste:

```js
[{
   id: 627,
   title: "Cheez Crunchers",
   score: 98.2
 }, {
   id: 32647,
   title: "Bacon crisp",
   score: 47.5
 }, {
   id: 934,
   title: "Sørlandschips",
   score: 32.3
 }]
```

Problemet med lista dukker opp idet du har lyst til å legge til litt
meta-informasjon i endepunktet ditt. Kanskje du ønsker å formidle hvilke
indekser det ble søkt i, og hvilken søkestreng dette er resultater for. Når body
er en liste er det ingen sted å legge sånt, og du tvinges til å endre typen på
responsen, som er alt annet enn bakoverkompatibelt.

En elegant løsning på dette problemet er å **aldri** (der brukte jeg det ordet
igjen) returnere en ren liste. Pakk det alltid inn i et objekt, slik at du har
plass til å legge til flere felter senere:

```js
{
  results: [{
     id: 627,
     title: "Cheez Crunchers",
     score: 98.2
   }, {
     id: 32647,
     title: "Bacon crisp",
     score: 47.5
   }, {
     id: 934,
     title: "Sørlandschips",
     score: 32.3
   }]
}
```

Denne responsen har all verdens rom for å vokse på en måte som ikke er til bry
for de som allerede har tatt i bruk API-et ditt. Nydelig.

## Aldri returner nakne objekter

Nå skal ikke jeg legge meg opp i hva slags innhold API-et ditt serverer, men her
snakker jeg altså om "nakent" som i: Ikke returner rene domenekonsepter som body
i en respons. Dette er i grunnen en variant av poenget over: gi deg selv litt
rom å vokse i.

Se på dette API-et for å opprette en bruker:

```
POST https://verdens-beste-api.no/users
Content-Type: application/json

{
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen"
}
```

API-et svarer 201, og inkluderer den nye brukeren (med sin tildelte id) i body:

```js
{
  id: 123,
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen"
}
```

La oss si at du ønsket å inkludere noe meta-informasjon om handlingen. Hvor skal
vi gjøre av det? Det frister ikke akkurat å slenge det inn i selve brukeren?
Akkurat som vi gjorde med lister kan man også med objekter inkludere et ekstra
nivå med rom for vekst:

```
{
  user: {
    id: 123,
    givenName: "Christian",
    familyName: "Johansen",
    username: "cjohansen"
  }
}
```

Voila! Denne responsen kan fint vokse med flere nøkler enn bare `user`, uten at
det tråkker de som allerede bruker API-et ditt på tærne.

## Ignorer overflødig informasjon

Når API-et ditt får henvendelser bør det rett og slett ignorere overflødig
informasjon. Se for deg igjen at vi skal opprette en bruker:

```
POST https://verdens-beste-api.no/users
Content-Type: application/json

{
  givenName: "Christian",
  familyName: "Johansen",
  username: "cjohansen",
  userAgent: "my-consumer"
}
```

Slike ting kan skje når folk bruker automatisk mapping av egne datastrukturer
for å generere payloads osv. Uansett hva grunnen er: dersom du får det du
trenger for å gjøre jobben er det ingen grunn til å bry seg om overflødig data.

Jeg gjetter på at mange av dere rynker litt på nesa over dette forslaget, men si
meg: Når sist sjekket du etter overflødige URL-parametere i API-ene dine? Ikke
det, nei.

Det er én ulempe med dette forslaget som må adresseres. La oss si at for- og
etternavn er valgfrie i API-et. Hva skjer da når jeg gjør dette?

```
POST https://verdens-beste-api.no/users
Content-Type: application/json

{
  givenName: "Cher",
  famiName: "Johansen",
  username: "cjohansen"
}
```

Her har jeg åpenbart feilstavet `"familyName"`. Dersom API-et ikke feiler for
"overflødig" informasjon blir dette ikke fanget opp, og jeg får opprettet en
bruker som ikke har etternavn. Én måte å håndtere dette på er å lene seg på at
vi har plass til å kommunisere ekstra informasjon i responsen. Da kan vi
ihvertfall flagge det for brukeren:

```js
{
  user: {
    id: 123,
    givenName: "Cher",
    username: "cjohansen"
  },
  unrecognizedFields: ["famiName"]
}
```

## Det er greit å duplisere data

Se for deg et API som gir informasjon om næringsinnholdet i mat. Dette API-et
har et felt som heter `energy`, et tall:

```js
{
  food: "Cheeze Crunchers",
  energy: 567
}
```

I retrospekt innser vi at dette ikke var et godt valg. Det er ingen informasjon
om hvilken størrelsesorden dette tallet opererer i, eller engang _hva_ det er.
Er det kalorier eller joule? Kilovis? Hvem vet? API-et må forbedres.

Djevelen på skulderen din forteller deg at du burde endre API-et så det heller
ser sånn ut:

```js
{
  food: "Cheeze Crunchers",
  energy: {
    measure: 567,
    unit: "kcal"
  }
}
```

Mye bedre. Men hva med de som allerede bruker API-et? De kommer ikke til å sette
pris på denne endringen -- selvom de verdsetter presiseringen. Siden noen
allerede bruker `energy` er det best å la den være som den er. I stedet legger
vi til et nytt felt:

```js
{
  food: "Cheeze Crunchers",
  energy: 567,
  energyMeasure: {
    measure: 567,
    unit: "kcal"
  }
}
```

Dette API-et vil fungere godt for både game og nye brukere. Dokumentasjonen kan
oppdateres til å understreke at `energy` er der av historiske årsaker, og bør
unngås (men kommer **aldri** til å forsvinne).

Dette forslaget går nok for mange på estetikken løs. Dette er da ikke et rent og
pent API? Nei, sånn er det med kode som har vært ute en vinternatt. Den er
vakker på en annen måte. Dette API-et oser av erfaring og respekt for sine
brukere.

## Hva med versjonering?

Mange bruker versjonering av HTTP-API-er for å gjøre endringer som ikke er
bakoverkompatible. Versjonering kan være en ålreit strategi, men ofte ender
brukerne allikevel opp med å tvinges til å løpe etter endringene dine, fordi
eldre versjoner i praksis pensjoneres når det kommer nye.

Målet med bakoverkompatibilitet er at ting som er laget mot dagens løsning også
fungerer om 5 år uten at noen må gjøre noe. Dersom du hyppig kommer med nye
versjoner og pensjonerer de gamle så leverer du ikke en bakoverkompatibel
tjeneste.

## Tenk på de eksisterende brukerne

Når du designer API-er er det fint å ofre en tanke på "hvilke muligheter har jeg
for å legge til informasjon her uten å ødelegge for de som allerede har tatt
dette i bruk?" Jeg mener ikke at du skal gjøre alt anemisk og generelt i
fleksibilitetens navn, men heller at du ser etter små tilpasninger som gir deg
litt rom å vokse i.

Til sist: alt må ikke inn i samme endepunkt. Det er helt OK å ha to endepunkter
for å opprette en bruker. Dersom du skriver koden din slik at implementasjonen
ikke henger tett sammen med HTTP-representasjonen burde det være en smal sak å
tilby den samme logikken med flere forskjellige HTTP-endepunkter.
