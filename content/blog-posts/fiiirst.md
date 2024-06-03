:page/title Tre løse tanker om «first»
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-06-04T09:00:00"
:blog-post/tags [:clojure]
:blog-post/series {:series/id :clojure-core}
:blog-post/description

Det slo meg her om dagen at funksjonen `first` er litt for lett å gripe etter.
Den er åpenbart nyttig, men *think you a little about!* Den blir fort
mer trøblete enn man skulle tro ved første øyekast.

:blog-post/body

Det slo meg her om dagen at funksjonen `first` er litt for lett å gripe etter.
Den er åpenbart nyttig, men *think you a little about!* Den blir fort
mer trøblete enn man skulle tro ved første øyekast.

## 1. Sortere og så ta én

Her er en klassiker:

```clj
(first (reverse (sort-by :poeng deltakere)))
```

Denne gir oss deltakeren med flest poeng, javisst, men var det egentlig nødvendig å
sortere hele lista for å finne vinneren? Nei, clojure.core har `max-by`:

```clj
(apply max-by :poeng deltakere)
```

I likhet med `max` så tar den varargs, så vi må trå til med `apply`. Utover det
så er det bare oppside: Kjører bare én gang gjennom datastrukturen, og lager
vesentlig mindre søppel i minnet.

#### Bare oppside, sa du?

Nei, akkurat den påstanden måtte jeg moderere fort, du!

I motsetning til `sort-by` som bruker `compare`, så fungerer `max-by` kun med
tall. Hvis du for eksempel vil sammenligne datotid, så får du en exception i
fjeset.

Da kan du dra inn [James Reeves](https://github.com/weavejester) sin
[medley](https://github.com/weavejester/medley)-pakke og bruke
[`greatest-by`](https://weavejester.github.io/medley/medley.core.html#var-greatest-by)
istedenfor. Funker.

## 2. Håper det bare er én

Vi er opptatt av hvor landets restauranter er å finne, slik at vi kan dra
til dem og gjennomføre inspeksjon. Derfor har vi modellert inn kjøpesentere og
slikt i modellen vår.

Et senter kan har flere adresser. De er ofte ganske store, og kan fort ligge i
flere gater og ha flere innganger. Dette har vi modellert med attributtet
`:senter/adresser`.

På den annen side, så tror vi ikke det kan finnes flere kjøpesentere på samme
adresse. Dette er en antagelse som denne koden håper at holder:

```clj
(first (:senter/_adresser adresse))
```

Den [følger referansen andre veien](/alle-gatene-i-kommunen/), slik at vi går
fra en *adresse* til en *liste med sentere* på den adressen. Og så tar den det
første.

Håper det bare er ett senter! 🤞🤞

Hvis antagelsen vår ryker, så feiler denne koden i det stille. Den vil da bare
ta et tilfeldig senter og si seg godt fornøyd.

I en slik situasjon, hvor man plukker den første fordi det bare skal være én, så
kan det være lurt å sjekke sine antagelser:

```clj
(let [[senter & flere] (:senter/_adresser adresse)]
  (when (seq flere)
    (throw (ex-info "Jøss, her var det jaggu flere sentere, ass!" adresse)))
  ...)
```

## 3. Oppslag med first og filter

Her er en klassiker til:

```clj
(first (filter #(= id (:id %)) deltakere))
```

Denne finner deltakeren med en gitt `id`.

Her er det ikke egentlig koden det er noe gæ'ærnt med, men kanskje
datastrukturen? Ikke alltid, men noe å tenke på: Lister er litt dritt å jobbe
med.

Har du noen gang lagt merke til hvor fint det er å bruke `update-in` og
`assoc-in`? De tar begge en *path* til verdien som skal oppdateres:

```clj
(def player {:health {:max 8, :current 7}})

(update-in player [:health :current] - 2)

;; => {:health {:max 8, :current 5}}
```

Med en gang du introduserer lister, så slutter disse fine verktøyene å fungere
spesielt godt. Derfor bruker vi bare maps i [Parens of the
Dead](https://www.parens-of-the-dead.com), for eksempel. Med `id` som nøkkel.

Hvis du hadde gjort det sånn, så kunne klassikeren endres, fra:

```clj
(first (filter #(= id (:id %)) deltakere))
```

til:

```clj
(get deltakere id)
```

og en deltaker sine poeng kan oppdateres med:

```
(update-in deltakere [id :poeng] + 7)
```

Herlig! Du kan jo bare tenke litt på hvordan den siste der hadde sett ut med en
liste. Det hadde ikke vært noe moro.

## Til slutt

Det finnes selvfølgelig gode grunner til å bruke `first`, men neste gang du
skriver `first`, husk denne bloggposten og think you a little about.
Det er alt jeg sier.
