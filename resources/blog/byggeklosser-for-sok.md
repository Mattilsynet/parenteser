:page/title Byggeklosser for søk
:blog-post/description
:blog-post/author :person/christian

Hvordan fungerer egentlig fulltekstsøk? I dette innlegget ser vi på noen vanlige
byggeklosser og implementerer en veldig enkel søkemotor for å lære litt om
akkurat det.

:open-graph/description

Lær om fulltekstsøk ved å implementere noen byggeklosser.

:blog-post/body

Fulltekstsøk kan virke magisk. Hva er det egentlig som foregår når jeg får treff
på "klovneforskning" etter å ha søkt på "forske"? I dette innlegget skal jeg
lage en liten søkemotor i JavaScript som forsøker å svare på det. Det er mange
måter å lage søk på, men tilnærmingen jeg bruker i dette innlegget er løselig
basert på modellen som benyttes i ElasticSearch (og det underliggende Lucene).

## Indeksen

Et raskt søk krever en indeks. Indeksen lar oss gjøre oppslag i konstant tid,
heller enn å iterere gjennom store datamengder for å finne treff. En indeks
bytter plass/minne mot hastighet. På sitt aller enkleste kan en søkeindeks være
en mapping mellom søkefraser og en liste med dokumenter som matcher:

```js
{
  "klovn": [1, 2, 3],
  "forskning": [1, 3],
  "føtter": [2],
  "humor": [1, 2]
}
```

For å søke i denne indeksen slår vi rett og slett opp søkestrengen og ser hvilke
dokumenter vi finner. Denne indeksen kan dermed bare finne treff på de fire
konkrete innslagene. Skal vi svare på mange forskjellige søk må indeksen bli
dramatisk mye større - men den kan ikke inneholde alle tenkelige søkestrenger.
Vi må dermed komme opp med noen lure triks.

## Indeksering

Indeksering er øvelsen vi gjør for å bygge opp indeksen fra kildematrialet vårt.
Vi tar utgangspunkt i følgende datamatriale:

```js
var data = [
  {
    id: "a1",
    title: "Klovneforskningens Lysende Fremtid",
    description: "Om klovneforsknings spennende innsikt i fremtidens humor."
  },
  {
    id: "a2",
    title: "De Komiske Føttenes Rolle i Humor",
    description: "Hvordan klovneføtter påvirker vår opplevelse av humor."
  },
  {
    id: "a3",
    title: "Klovneforskningens Overraskende Oppdagelser",
    description: "Oppdag de uventede funnene som klovneforskere har gjort."
  }
];
```

En god start vil være å indeksere alle titlene. Siden vi ikke ønsker at brukerne
må søke etter eksakt tittel, må vi massere dataene litt før de går i indeksen. I
ElasticSearch bruker man "analyzers" for å brekke et tekstfelt ned i symboler
som lagres i indeksen. En analyzer kan bestå av en rekke verktøy, men den
viktigste er det som kalles en "tokenizer" - en algoritme som sier noe om
hvordan en tekststreng blir til individuelle symboler.

En god start er å kvitte seg med store bokstaver og splitte strengen på
mellomrom og annen tegnsetting. Den resulterende indeksen vil se ut som følger:

```js
var index = {
  "klovneforskningens": ["a1", "a3"],
  "lysende": ["a1"],
  "fremtid": ["a1"],
  "de": ["a2"],
  "komiske": ["a2"],
  "føttenes": ["a2"],
  "rolle": ["a2"],
  "i": ["a2"],
  "humor": ["a2"],
  "overraskende": ["a3"],
  "oppdagelser": ["a3"]
};
```

## Søk

For å søke i indeksen så slår vi opp søkestrengen og ser hva vi finner. En
veldig naiv tilnærming ser ut som følger:

```js
function search(index, q) {
  return index[q];
}
```

Velrettede søk får svar:

```js
search(index, "klovneforskningens"); //=> ["a1", "a3"]
```

Men det skal ikke mye fantasi til før denne tilnærmingen bryter sammen:

```js
search(index, "Klovneforskningens"); //=> null
search(index, "rolle humor"); //=> null
```

Enda vi har indeksert tittelen `"De Komiske Føttenes Rolle i Humor"` gir et søk
på `"rolle humor"` ingen treff.

### Søkesymboler

Indeksen ble bygget opp ved å lage symboler fra kildematrialet. For å treffe
godt i indeksen er vi nødt til å analysere søkestrengen med samme verktøy og så
slå opp alle symbolene vi da ender opp med. La oss først lage en støttefunksjon
som finner alle id-ene som matcher ett symbol:

```js
function lookupToken(index, token) {
  const hitsById = _.countBy(index[token]);

  return Object.keys(hitsById)
    .map(id => ({
      id: id,
      token: token,
      score: hitsById[id]
    }));
}
```

Først slås symbolet opp i indeksen, og så finner [`countBy` fra
lodash](https://lodash.com/docs/4.17.15#countBy) en oversikt over hvilke id-er
som matcher, og hvor mange ganger. Til slutt har vi en liste med objekter der vi
har én forekomst av hvert par av id-er og søkesymbol, sammen med en "score" som
for nå er antall treff. Resultatet ser sånn ut:

```js
[
 {
   "id": "a1",
   "token": "humor",
   "score":1
 },
 {
   "id": "a2",
   "token": "humor",
   "score":2
 }
]
```

Når vi potensielt har flere søkesymboler må vi ta stilling til om søket skal
være "OR" (alle dokumenter der minst ett av symbolene matcher) eller "AND" (kun
de dokumentene der alle symbolene matchet) - eller noe midt i mellom. La oss
starte med å kreve alle:

```js
function isRelevantResult(results, n) {
  return results.length >= n;
}

function search(index, q) {
  var tokens = tokenize(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = tokens.length;

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n));
}
```

Først bryter vi søkestrengen ned i symboler. Så slår vi opp hvert symbol, som
gir oss en liste med id/symbol-par med en score.
[`flatMap`](https://lodash.com/docs/4.17.15#countBy) sørger for at alle disse
listene kombineres til en stor liste. For å finne de dokumentene som matchet
alle søkesymbolene grupperer vi på dokument-id, og returnerer de id-ene som har
like mange treff som det er søkesymboler.

Dette søket finner dokument-id-er både med store bokstaver og søkeordene i feil
rekkefølge:

```js
search(index, "Klovneforskningens"); //=> ['a1', 'a3']
search(index, "rolle humor"); //=> ['a2']
```

## Indeksering, part deux

La oss utvide indeksen ved å indeksere beskrivelsen også. Her er et utsnitt
resultatet:

```js
var index = {
  "klovneforskningens": [
    "a1",
    "a3"
  ],
  "lysende": [
    "a1"
  ],
  "fremtid": [
    "a1"
  ],
  "humor": [
    "a1",
    "a2",
    "a2"
  ],

  // ...
};
```

### Scoring

Ordet "humor" opptrer flere ganger for artikkel 2. Da burde artikkel 2 vært
ansett som mest relevant og havnet øverst i resultatet. Det kan vi få til ved å
gi hvert dokument en total score ut fra antall treff per symbol, og så sortere:

```js
function getScoredResult(id, results) {
  return {
    id: id,
    score: results.reduce((score, r) => score += r.score, 0)
  };
}

function search(index, q) {
  var tokens = tokenize(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = tokens.length;

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n))
    .map(id => getScoredResult(id, results[id]))  // Nytt
    .toSorted((a, b) => b.score - a.score);       // Nytt
}
```

Litt å tygge over, men `results[id]` gir oss alle id/symbol-parene med scoren vi
fant i `lookupToken` - eksempelvis `{id: "id1", token: "humor", score: 2}`. Vi
får total score for en id ved å summere hver enkelt score.

## Fuzzy søk

Søke-funksjonen klarer nå å søke på flere termer, men du må fortsatt skrive dem
eksakt som de opptrer i kildematrialet. En måte å løse dette på er ved å bryte
symbolene ned i mindre deler enn hele ord. Det er mange måter å gjøre dette på,
og teknikken vi nå skal se på kalles "ngrams".

Den enkleste måten å forstå ngrams er å se dem. Ordet "humor" består av disse
ngrammene når `n = 2`:

```
hu
um
mo
or
```

La oss oppdatere indeksen vår med ngram av tittelen med `n = 2`. Resultatet er
som følger:

```js
{
  "klovneforskningens": ["a1", "a3"],
  "lysende": ["a1"],
  "fremtid": ["a1"],
  "kl": ["a1", "a3"],
  "lo": ["a1", "a3"],
  "ov": ["a1", "a3", "a3"],
  "vn": ["a1", "a3"],
  "ne": ["a1", "a2", "a3"],
  "ef": ["a1", "a3"],
  "fo": ["a1", "a3"],
  "or": ["a1", "a2", "a3"],
  "rs": ["a1", "a3"],
  "sk": ["a1", "a2", "a3", "a3"],
  "kn": ["a1", "a3"],
  "ni": ["a1", "a3"],
  "in": ["a1", "a3"],
  "ng": ["a1", "a3"],
  "ge": ["a1", "a3", "a3"],
  "en": ["a1", "a1", "a2", "a3", "a3"],
  // ...
}
```

Hvis vi brekker søkestrengen opp i tilsvarende symboler kan vi nå lette på
kravet på om hvor mange av søkesymbolene vi krever match på for å inkludere en
id i resultatet.

For å ta eksempelet vi startet med: "klovneforskning" kan brekkes ned i følgende
ngrams:

```
kl
ov
ne
ef
fo
or
rs
sk
kn
in
ng
```

Mens søkestrengen "forske" består av:

```
fo
or
rs
sk
ke
```

Det er bare "ke" som ikke forekommer i "klovneforskning", så la oss si at vi
krever 80% match (0.8 * 5 = 4):

```js
function isRelevantResult(results, n) {
  return results.length  >= Math.floor(n * 0.8);
}
```

Med denne endringen vil vi nå kunne søke på "forske" og finne artikler om
"klovneforskning":

```js
search(index, "forske")
  .map(res => findArticle(res.id).title)

//=> [
//     "Klovneforskningens Overraskende Oppdagelser",
//     "Klovneforskningens Lysende Fremtid"
//   ]
```

### Edge ngrams

Dersom du skal implementere et autocomplete-aktig søk så er "edge ngrams"
nyttige. Disse lages ved å kun lage ngrams fra starten og utover. Eksempelvis
kan "klovneforskning" blit til edge ngrams med lengde 2 til 15:

```
kl
klo
klov
klovn
klovne
klovnef
klovnefo
klovnefor
klovnefors
klovneforsk
klovneforskn
klovneforskni
klovneforsknin
klovneforskning
```

## Hva skal jeg med dette?

Forhåpentligvis har du nå lært litt om hvordan søk, særlig i ElasticSearch,
faktisk fungerer. Men ikke bare det - denne lille søkemotoren kan være en nyttig
måte å lage et enkelt søk i frontenden dersom datasettet ikke er for stort. La
oss si at du har noen tusen titler - ja, så er disse 100 linjene med JavaScript
nok til å gi deg et ålreit søk.

Ved å utvide søkemotoren med noen enkle konsepter for vekting kan dette bli
svært nyttig. Hvordan det kan gjøres kommer jeg tilbake til i et
oppfølgingsinnlegg.
