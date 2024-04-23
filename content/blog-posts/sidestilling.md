:page/title Sidestilling med juxt
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-04-23T09:00:00"
:blog-post/tags [:clojure]
:blog-post/series {:series/id :clojure-core}
:blog-post/description

Kjernebiblioteket i Clojure har rikelig med småfunksjoner man ikke ser hver dag.
I dag skal vi se på en hendig liten funksjon med et rart navn.
Det er tid for `juxt`.

:blog-post/body

Kjernebiblioteket i Clojure har rikelig med småfunksjoner man ikke ser hver dag.
I dag skal vi se på en hendig liten funksjon med et rart navn.
Det er tid for `juxt`.

> Juxtaposition is an act or instance of placing two elements close together or
> side by side. This is often done in order to compare/contrast the two, to show
> similarities or differences, etc.
>
> -- https://en.wikipedia.org/wiki/Juxtaposition

Det er åpenbart at Rich Hickey hadde ordboka lett tilgjengelig når han skrev
clojure.core. Funksjonen `juxt` tar navnet sitt fra *juxtaposition* - eller
*sidestilling* på godt norsk - og vel, det passer perfekt.

La oss se på et eksempel først, så skal jeg forklare litt mer etterpå.

## Et eksempel

Her har jeg litt kode som drar ut titler for alle bloggpostene våre:

```clj
(->> (blog-posts/get-blog-posts db)
     (map :page/title))

;; =>

("Sidestilling med juxt"
 "NATS JetStream: Persistente køer og logger"
 "PubSub med NATS"
 "Skriv kode istedenfor SQL"
 ...)
```

Jeg kan også hente ut URL-en til disse.

```clj
(->> (blog-posts/get-blog-posts db)
     (map :page/uri))

;; =>

("/sidestilling/"
 "/nats-jet-stream/"
 "/intro-til-nats/"
 "/alle-gatene-i-kommunen/"
 ...)
```

Men hva om jeg ville se dem ... side om side? Sidestilt, om du vil?

```clj
(->> (blog-posts/get-blog-posts db)
     (map (juxt :page/title :page/uri)))

;; =>

(["Sidestilling med juxt" "/sidestilling/"]
 ["NATS JetStream: Persistente køer og logger" "/nats-jet-stream/"]
 ["PubSub med NATS" "/intro-til-nats/"]
 ["Skriv kode istedenfor SQL" "/alle-gatene-i-kommunen/"]
 ...)
```

Aha! Er det ikke fint? `juxt` fikser biffen.

## En høyere ordens funksjon

`juxt` er en såkalt høyere ordens funksjon. Disse kommer i to former:

- funksjoner som tar imot andre funksjoner som parametere (slik som `map`)
- funksjoner som returnerer en ny funksjon (slik som `constantly`)

`juxt` har gleden av å oppfylle begge disse kriteriene. 💪

Den tar i mot et vilkårlig antall funksjoner, og returnerer en ny funksjon som
kaller hver av funksjonene i tur og orden og putter resultatene i en vektor.

I eksempelet vårt sender vi inn `:page/title` og `:page/uri`. Disse er keywords,
som i Clojure kan brukes som funksjoner for å slå seg selv opp i maps (også 💪). Tilbake
får vi en funksjon med en implementasjon som i praksis ser sånn ut:

```clj
(fn [m]
  [(:page/title m)
   (:page/uri m)])
```

## Et eksempel til

For å gjøre det noe tydeligere, la oss si at vi hadde denne [litt
unødvendige](https://www.kodemaker.no/blogg/2019-07-gammelt-triks-ny-kontekst/)
funksjonen:

```clj
(defn get-blog-post-author-name [blog-post]
  (:person/given-name
   (:blog-post/author blog-post)))
```

Kanskje vi er interessert i å se titler og navn i sammenheng:

```clj
(->> (blog-posts/get-blog-posts db)
     (map (juxt get-blog-post-author-name
                :page/title)))

;; =>

(["Magnar" "Sidestilling med juxt"]
 ["Christian" "NATS JetStream: Persistente køer og logger"]
 ["Christian" "PubSub med NATS"]
 ["Magnar" "Skriv kode istedenfor SQL"]
 ...)
```

For å dra det litt inn i det absurde -- mest for eksempelet sin del -- så kan vi se
hvem som skriver de lengste titlene:

```clj
(->> (blog-posts/get-blog-posts db)
     (map (juxt get-blog-post-author-name
                (comp count :page/title))))

;; =>

(["Magnar" 21]
 ["Christian" 42]
 ["Christian" 15]
 ["Magnar" 25]
 ...)
```

Her er det atpåtil en liten bonus-funksjon. `comp` komponerer to funksjoner
sammen.

## Daglig bruk

Hvis du møter på `juxt` ute i det fri er det mest sannsynlig én av to praktiske
bruksområder: Å lage oppslagstabeller eller sortere lister.

#### Lage oppslagstabeller

Til dette formål kombinerer vi `juxt` med `identity`. Det er en til av de rare
småfunksjonene i clojure.core. Denne returnerer argumentet sitt uendret:

```clj
(= (identity foo) foo) ;; => true
```

Da ser det noe slik ut:

```clj
(->> (blog-posts/get-blog-posts db)
     (map (juxt :page/uri identity))
     (into {}))

;; =>

{"/sidestilling/" {...}
 "/nats-jet-stream/" {...}
 "/intro-til-nats/" {...}
 "/alle-gatene-i-kommunen/" {...}
 ...}
```

- Start med en liste bloggposter.
- Bruk `map` og `juxt` til å lage en liste med tupler på formen `[side-url bloggpost]`
- Tøm dem inn i et map med `into`, hvor første verdi i tuplet blir nøkkel, andre blir verdi

Dermed har jeg laget meg et map hvor jeg kan slå opp en bloggpost med URL-en.

Med andre ord kan jeg med `map`, `juxt` og `identity` lage vilkårlige
oppslagstabeller fra en liste. Det er hendige greier.

#### Sortere lister

Her kommer vi inn på en av fordelene med uforanderlige data. I de
fleste programmeringsspråk så er lister muterbare, og dermed ikke
sammenlignbare. Det er ikke noe poeng i å sammenligne objektpekere.
Uforanderlige lister derimot er faste verdier -- som kan vi sortere.

La oss se hvem som skriver de lengste bloggpostene, med lengste tittel som tie
breaker:

```clj
(->> (blog-posts/get-blog-posts db)
     (sort-by (juxt (comp - count :blog-post/body)
                    (comp - count :page/title))))
```

Her sorterer vi først på antall tegn i `:blog-post/body` -- altså selve teksten
-- og deretter `:page/title`. Når vi slenger på `-` (minus) så får vi sortert
flest først.

Vi kan avslutte med å bruke `juxt` en siste gang for å sidestille disse dataene
så de blir lette å se på:

```clj
(->> (blog-posts/get-blog-posts db)
     (sort-by (juxt (comp - count :blog-post/body)
                    (comp - count :page/title)))
     (map (juxt get-blog-post-author-name
                :page/title
                (comp count :blog-post/body))))

;; =>

(["Christian" "Bokstavkjeks – den som leter skal finne!" 9577]
 ["Magnar" "Jakten på de forsvunnede 85 sekunder" 8419]
 ["Christian" "Virtuell DOM fra bunnen av" 8319]
 ["Christian" "All PR er god PR?" 7939]
 ["Christian" "Flate, møre data" 7451]
 ...)
```

Ikke overraskende dominerer Christian denne lista. 😅
