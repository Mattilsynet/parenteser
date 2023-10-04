:page/title Vektet søk
:blog-post/published #time/ldt "2023-10-07T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/description

I neste installasjon i "hvordan fungerer egentlig søk?" skal vi se litt på
hvordan vi kan implementere vekting for å bedre tune resultatene våre.

:open-graph/description

En introduksjon til vekting av søkeresultater i en søkemotor.

:blog-post/introduction

Andre del i serien "Hvordan fungerer egentlig søk?" (
[del 1](/blog-posts/byggeklosser-for-sok/))

:blog-post/body

Det er mange måter å vekte søk på. Vi kan vekte ved indeksering, både basert på
hvilket felt et symbol kommer fra, og basert på hva slags type "tokenization"
som er gjort. Kanskje skal ngrams få lavere vekt enn hele ord. I tillegg kan vi
vekte når vi søker i indeksen. Da har vi muligheten til å levere litt
forskjellige typer søk på den samme indeksen.

I den første artikkelen bygget vi kun én stor indeks. Da er det begrenset med
muligheter for å vekte idet du søker, fordi du allerede har mistet en del
informasjon om symbolene som ligger i indeksen. En strategi som gir stor
fleksibilitet er å lage mange mindre indekser, og så tune søkene ved å kombinere
disse på forskjellig vis.

Her er et gjensyn med dataene vi skal indeksere:

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

Sist indekserte vi titlene, beskrivelsen, og ngrams av lengde 2 fra titlene. La
oss nå gjøre det igjen, men plassere dem i tre forskjellige indekser:

```js
{
  "title": {
    "klovneforskningens": ["a1", "a3"],
    "lysende": ["a1"],
    "fremtid": ["a1"],
    // ...
  },
  "description": {
    "om": ["a1"],
    "klovneforsknings": ["a1"],
    "spennende": ["a1"],
    "innsikt": ["a1"],
    // ...
  },
  "titleNgram": {
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
    // ...
  }
}
```

La oss så tilpasse søkefunksjonen vår fra sist. Sist tok den imot en index og en
søkestreng, gjorde en hardkodet "tokenize" før den slo opp i indeksen, og hadde
til slutt en hardkodet terskelverdi for hvor mange søkesymboler som måtte treffe
for at vi skulle inkludere en id. Denne gangen parameteriserer vi alle disse:

```js
function search({index, requiredMatches, tokenizer}, q) {
  var tokens = tokenizer(q);
  var hits = _.flatMap(tokens, t => lookupToken(index, t));
  var results = _.groupBy(hits, r => r.id);
  var n = Math.floor(tokens.length * requiredMatches || 1);

  return Object.keys(results)
    .filter(r => isRelevantResult(results[r], n))
    .map(id => getScoredResult(id, results[id]))
    .toSorted((a, b) => b.score - a.score);
}
```

For å gjenskape søket fra sist kan vi kalle den som følger:

```js
search({
  index: index,
  tokenizer: tokenize,
  requiredMatches: 0.8
}, "humor føtter");
```

## Søk i flere indekser

Siden vi nå har flere indekser kan vi gjøre flere søk, og kombinere resultatet.
Vi lager en liten funksjon til å hjelpe oss:

```js
function searchAll({queries}, q) {
  var results = _.groupBy(
    _.flatMap(queries, config => search(config, q)),
    r => r.id
  );

  return Object.keys(results)
    .map(id => getScoredResult(id, results[id], 1));
}
```

Denne ligner litt på søkefunksjonen, og fungerer ganske så likt, bare at denne i
praksis gjør en OR mellom alle queryene som sendes inn. Nå kan vi spesifisere at
et søk skal lete i flere indekser og kombinere resultatene:

```js
searchAll({
  queries: [
    {
      index: index.titleNgram,
      requiredMatches: 0.8,
      tokenizer: s => tokenizeNgrams(s, 2)
    },
    {
      index: index.title,
      tokenizer: tokenize
    }
  ]
}, "forske humor");
```

### Vekting

Når vi nå kan søke ved å lete i de forskjellige indeksene med forskjellige
parametere kan vi endelig innføre vekting. Ved å legge til en `boost` på
query-et vil alle treffene derfra ganges med det tallet:

```js
searchAll({
  queries: [
    {
      index: index.titleNgram,
      requiredMatches: 0.8,
      tokenizer: s => tokenizeNgrams(s, 2)
    },
    {
      index: index.title,
      tokenizer: tokenize,
      boost: 20
    },
    {
      index: index.description,
      tokenizer: tokenize,
      boost: 5
    }
  ]
}, "forske humor");
```

Nå søker vi i alle indeksene, men vi vektlegger treff på hele ord, og særlig de
som opptrer i tittelen.

## Auto-complete

Sist nevnte jeg muligheten for å bruke såkalte edge ngrams til
auto-complete-søk. La oss bygge en indeks til:

```js
var index = {
  title: indexDocuments(data, a => tokenize(a.title)),
  description: indexDocuments(data, a => tokenize(a.description)),
  titleNgram: indexDocuments(data, a => tokenizeNgrams(a.title, 2)),
  titleEdgeGrams: indexDocuments(data, a => tokenizeEdgeNgrams(a.title, 2, 15))
};
```

Når vi nå ønsker å søke opp treff til det brukeren har skrevet kan vi bruke den
nye indeksen:

```js
searchAll({
  queries: [
    {
      index: index.titleEdgeGrams,
      tokenizer: s => tokenizeEdgeNgrams(s, 2, 15)
    }
  ]
}, "de ko");

//=> "De Komiske Føttenes Rolle i Humor"
```

Og med det har vi et nokså kraftig liten søkemotor på rett over 100 linjer helt
plain JavaScript - og da har jeg til og med inlinet de 3 lodash-funksjonene vi
brukte, så koden er helt fri for avhengigheter. Ikke dårlig!
