:page/title Interpolering i en verden hvor alt er data
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-08-20T09:00:00"
:blog-post/tags [:clojure :data :webutvikling]
:blog-post/description

Det er overraskende hvor mange muligheter som åpner seg når alt er data,
istedenfor objekter eller funksjoner. Her er noen som overrasket meg.

:blog-post/body

Når jeg sier at jeg "jobber med data" så mener jeg det i en mer bokstavelig
betydning enn det som ofte blir forstått. En hel verden av muligheter åpner seg
når alt er data istedenfor objekter eller funksjoner.

## Strenginterpolering

Det var i forbindelse med web templates at jeg først hørte det noget spesielle
ordet *interpolere*. Hvis du har vært med noen år så kjenner du sikkert igjen
de gode gamle bartene fra Mustache:

```clj
(interpolate "Hei, {{navn}}!"
             {:navn "Magnar"})

;; => "Hei, Magnar!"
```

Vi fletter altså verdier inn i en tekst. Dette trikset, med eller
uten barter, har vist seg å være ganske nyttige greier. Ikke er det spesielt
vanskelig å [skrive
selv](https://gist.github.com/magnars/d3e36c87fd756a2e64ba81fe29faf023) heller,
til tross for at man må diske opp med en hårete regex.

Enda mer nyttig viser det seg å være når man kan operere på vilkårlige data. La
meg introdusere dagens helt: `clojure.walk/postwalk`.

## En vandring i data

Med `postwalk` kan vi tusle ordnet gjennom datastrukturer og ved hvert punkt
bestemme om vi vil gjøre endringer. La oss ta en titt:

```clj
(require '[clojure.walk :refer [postwalk]])

(postwalk
 (fn [form]
   (if (= :navn form)
     "Magnar"
     form))
 {:greeting ["Hei, " :navn "!"]})

;; => {:greeting ["Hei, " "Magnar" "!"]}
```

Første argument til `postwalk` er en funksjon som i tur og orden mottar alle
verdiene i datastrukturen. I dette eksemplet returnerer den nesten alltid den
samme verdien, med mindre vi snubler over keywordet `:navn`.

Se så! Allerede har vi laget vår egen data-interpolering:

```clj
(defn interpolate [data replacements]
  (postwalk
   (fn [form]
     (or (replacements form)
         form))
   data))

(interpolate {:greeting ["Hei, " :navn "!"]}
             {:navn "Magnar"})

;; => {:greeting ["Hei, " "Magnar" "!"]}
```

Dette er postwalk i sin enkleste form -- bare tenk på alt mulig av sprell
funksjonen vår kunne ha gjort -- men så greit kan det altså gjøres.

## Adjø, templates

Vi har for lengst lagt Mustache og web templates bak oss. Det viser seg at
koding gjøres best med kode. Sprøtt, jeg vet.

Vi bruker data i et format som kalles [hiccup](https://github.com/weavejester/hiccup):

```clj
[:div
  [:h1 "Hallo!"]
  [:p "Fint å se deg."]]
```

Som du ser så er det en HTML-representasjon med Clojure-datastrukturer. Nyttige
greier. Hvordan [James Reeves](https://github.com/weavejester) fant på "hiccup" som navn er ikke lett å gjette
seg til, men det er en annen sak.

Uansett, her har vi et perfekt case for vår nye interpoleringsfunksjon:

```clj
(interpolate
 [:div
  [:h1 "Hallo " :navn "!"]
  [:p "Fint å se deg."]]
 {:navn "Christian"})

;; => [:div
;;     [:h1 "Hallo " "Christian" "!"]
;;     [:p "Fint å se deg."]]
```

## Atten bokstaver i midten

Vi brukte dette trikset til å flytte vår
[i18n](https://en.wikipedia.org/wiki/Internationalization_and_localization)-kode
heeelt ytterst i stacken. Hva betyr det? Jo, altså, før måtte vi skrive koden vår sånn:

```clj
(defn greet [dict user]
  [:div
   [:h1 (i18n/tr dict :hello (:name user))]
   [:p (i18n/tr dict :greeting)]])
```

Som du ser har vi en funksjon `i18n/tr` (translate) som tar i mot hvilken
setning som skal brukes (`:hello`) og en ordbok `dict` for det språket som er
gjeldende.

Vi må altså sende hele pakken med språksnutter fra funksjon til funksjon nedover
i lagene, for å klare å rendre den minste komponent.

Hadde vi brukt React, Reagent eller andre view-rendere som representerer DOM-en
med objekter, så hadde det stoppet her. Eller vi måtte ha lagt i18n-greiene i en
global singleton eller noe sånt fælt. Med datasentriske view-rendere som
[Dumdom](https://github.com/cjohansen/dumdom) og
[Replicant](https://github.com/cjohansen/replicant), så kan vi fortsette å jobbe
med bare data. Da kan vi bruke `postwalk` til å utsette internasjonaliseringen
til et mer beleilig tidspunkt.

Den nye hallo-funksjonen vår blir slik:

```clj
(defn greet [user]
  [:div
   [:h1 [:i18n :hello (:name user)]]
   [:p [:i18n :greeting]]])
```

Vi slipper altså å sende ordboka vår rundt, og kan heller gjøre det én gang for
hele siden helt ytterst:

```clj
(defn i18n-ify [dict hiccup]
  (postwalk
   (fn [form]
     (if (and (vector? form)
              (= :i18n (first form)))
       (apply i18n/tr dict (next form))
       form))
   hiccup))
```

Med ett er kunnskapen om hvilket språk som skal rendres samlet på én plass, der
den hører hjemme.

PS! Christian og jeg har skrevet [m1p](https://github.com/cjohansen/m1p) som tar
denne idéen og løper videre med den.

## Jeg klikker

Hva da med `onClick`?

Funksjoner som skal håndtere events ser jo vanligvis noe sånt ut:

```clj
[:button
  {:onClick (fn []
              (js/alert "Takk for rapporten!")}]
```

Denne funksjonsliteralen lager en anonym funksjon som blir registrert som
click-handler på knappen.

Helt opak, altså. Ugjennomtrengelig. Ikke inspiserbar. En funksjon er ikke data,
og er dermed endestasjon for postwalk. Vi får ikke gjort noe med teksten inni den.

Derfor har Replicant støtte for data også i denne posisjonen:

```clj
[:button
  {:on {:click [[:alert "Takk for rapporten!"]]}}]
```

Denne datastrukturen blir sendt til en event bus for dispatch, som beskrevet i
["En enkel frontendarkitektur som funker"](https://www.kodemaker.no/blogg/2020-01-enkel-arkitektur/).

Dermed får den også delta i internasjonaliseringen vår:

```clj
[:button
  {:on {:click [[:alert [:i18n :thanks]]]}}]
```

Igjen er det bare data, alt sammen. Lett å se på, lett å jobbe med, lett å
interpolere og internasjonalisere.

Hvis dette virker spennende, anbefaler jeg på det varmeste å se [Christian sitt
foredrag om datadrevne UI-er fra
JavaZone](https://parenteser.mattilsynet.io/datadreven-frontend/) som en nydelig
hovedrett etter denne appetittvekkeren.
