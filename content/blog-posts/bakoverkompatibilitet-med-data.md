:page/title Bakoverkompatibilitet med data
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-08-05T11:07:30.305284"
:blog-post/tags [:bakoverkompatibilitet :data]
:blog-post/description

En historie om hvordan dataorientering muliggjør bakoverkompatibilitet, fortalt med SI-enheter.

:open-graph/description

En historie om hvordan dataorientering muliggjør bakoverkompatibilitet, fortalt med SI-enheter.

:blog-post/body

Er det ikke deilig når en bit av koden bare er *ferdig*?

Christian har tidligere skrevet om bakoverkompatibilitet i
[Slutt å ødelegge tingene mine](/bakoverkompatibilitet/)
og i [Hvordan jeg lærte å slutte å bekymre meg og elske API-et](/bakoverkompatibilitet-i-praksis/).
I dag skal jeg fortelle en liten historie om Munit, et lite system for å regne på tall med enhet.
Systemet startet *litt* dataorientert, som gjorde det enklere å endre, og endte *mer* dataorientert, som ga flere fine effekter.

Da jeg innså at jeg kunne gjøre endringen uten å endre public API-et mitt fikk jeg en god følelse av ro!
☺️😌

## Dagens eksempel: ekvivalente personbiler for søylelast

Hvor mye er egentlig 17 Mega-Newton (MN)?

I byggingen av [Clarion Hotel The Hub] i Oslo Sentrum i 2017, kom jeg over en stor søylelast.

![Clarion Hotel The Hub](/images/the-hub.jpg)

Hotellet skulle få (har) en stor, fin konferansesal, cirka 24 meter i bredden.
Hvis jeg hadde fylt konferansesalen med en søylepark, ville salen sett ut som en parkeringskjeller.
Den hadde blitt vanskelig å bruke.
Tenk, du danser vilt rundt og brøler til Avril Lavigne:

> 🎸 🎶<br>
> He was a skater boy.<br>
> She said, "See ya later, boy."<br>
> He wasn't good enough for her.<br>
> 🎸 🎶

... før du tryner baklengs inn i en nokså hard stålsøyle.

For å unngå det, bar vi kreftene fra hotellrommene over ut til en usynlig søylerad i veggene på siden av salen.
Noe sånt:

![Skisse av søylen med de 17 MN](/images/the-hub-skisse-soyle-17mn.png)

<style type=text/css>
/* Begrens bilder til tekstbredden */
img {
    max-width: 100%;
}
</style>

To søyler endte opp med å måtte tåle 17 MN trykk hver.
Men hvor mye er det egentlig, i størrelser vi har et forhold til?

La oss prøve å regne om til personbiler: hvor høyt må vi stable personbiler for å få samme vekt?

[Clarion Hotel The Hub]: https://www.strawberry.no/hotell/norge/oslo/clarion-hotel-the-hub/

```clojure
(require '[munit.prefix :refer [k M]]
         '[munit.si :refer [kg m s]]
         ;; OBS: * og / kommer fra munit.units, ikke clojure.core.
         '[munit.units :refer [* /]])

;; ett tonn er 1000 kilo
(def t (* 1000 kg))
t
;; => [1000 kg]

;; en personbil veier cirka et tonn.
(def personbil-masse [1 t])

;; SI-enheten Newton er kilogram-meter per sekund i annen
(def N [kg m {s -2}])
;; les denne som "kilogram meter per sekund i annen". (-2 er eksponenten til sekund)
;; (målet er at tall med enhet skal være naturlige å skrive!)

;; 1 kilo-Newton er 1000 Newton, og 1 Mega-Newton er 1 000 000 Newton.
(def kN [k N])
(def MN [M N])

;; ... der k og M er vanlige Clojure-tall fra munit.prefix.
k
;; => 1000
M
;; => 1000000

(def g "omtrentlig tyngdeakselerasjon i Norge"
  ;; SI-enheten for aksellerasjon er (meter per sekund) per sekund
  ;; ... altså endring i fart per tid.
  [9.8 m {s -2}])

(def personbil-tyngekraft (* personbil-masse g))
(def stor-søylelast [17 MN])
(def ekvivalente-personbiler (/ stor-søylelast personbil-tyngekraft))

ekvivalente-personbiler
;; => 1734.6938775510205
```

Søylen måtte altså tåle å bære 1700 personbiler stablet oppå hverandre.
Det er en høy stabel!

## Dataorienterte API-er

Munit er et lite bibliotek i prototype-fase som lar deg jobbe med tall som har SI-enhet, eller en annen enhet.
Biblioteket er dataorientert, i den forstand at du sender inn vanlige Clojure-datastrukturer, og får ut Clojure-datastrukturer.
Det følger også med noen vanlige konstanter.

```clojure
(require '[munit.si :refer [m]]
         '[munit.units :refer [* /]])
         
;; Clojure-tall tolkes som enhetsløse størrelser
(def pi 3.1415)

;; Maps lar deg definere nye enheter av eksponenter
(def m3 {m 3})

;; Vektorer impliserer multiplikasjon
(def mm (/ m 1000))
(def r [30 mm])
(def h [3 m])
(def sylinder-volum (* pi r r h))
```

Det er alt!
Nå kan du biblioteket: Lag tall med SI-enheter av vanlige Clojure-tall, vektorer, maps eller base-enheter, bruk så operasjonene som følger med.

## Før endringen: munit implementert med defrecord.

Etter to uker i "[hengekøya]", skurret følgende for meg i implementasjonen:

[hengekøya]: https://www.youtube.com/watch?v=f84n5oFoZBc

```clojure
(ns munit.impl
  "Details for working with units. Do not use directly."
  (:require clojure.pprint
            munit.runtime))

;; [3]
(set! *warn-on-reflection* true)

;; [1]
(defrecord BaseUnit [system sym])
(defrecord Quantity [magnitude exponents])

;; 👇 [2]                         👇 [3]              👇 [3]
(defmethod print-method BaseUnit [^BaseUnit base-unit ^java.io.Writer w]
  (.write w (pr-str (.sym base-unit))))

(defmethod clojure.pprint/simple-dispatch BaseUnit [^BaseUnit base-unit]
  (clojure.pprint/write-out (.sym base-unit)))

;; [4] Reload SI units after redefining records.
(when (and munit.runtime/dev?
           (contains? (loaded-libs) 'munit.si))
  (require 'munit.si :reload))
```

😬

1. Trenger vi egentlig typer for BaseUnit og Quantity?
2. Hvorfor må vi dille med print-method for å forkle BaseUnit som symboler?
3. Typehint for å unngå reflection er litt kjedelig.
4. ... og hvorfor må vi drive og passe på at vi laster SI-navnerommet med `def`-er av BaseUnit-er på nytt når vi redefinerer BaseUnit-typen?

Dette må da kunne løses på en mindre vond måte.

## Etter endringen: data hele veien ned.

Records viste seg å være en dårlig idé!

Jeg innførte typene da jeg ikke klarte å implementere +, -, * og / lett uten å ha en "kanonisk" representasjon for tall med enhet.
Overalt måtte jeg finne størrelsen ("magnitude") og enheten ("unit") til tall.

Det problemet kunne jeg i stedet løst ved å lage to funksjoner, `magnitude` og `unit`!

```clojure
;; Først implementasjonsdetaljene.
;; Scroll forbi hvis du vil, denne kodesnutten er kun med i tilfelle folk lurer.

;; Hvis du ikke scroller forbi, anbefaler jeg å lese `magnitude`, `unit` og
;; `simplify`. De er viktigst!

(ns munit.impl
  "Unit arithmetic implementation details, do not use directly."
  (:refer-clojure :exclude [+ - * /]))

(defn magnitude [x]
  (cond (number? x)
        x

        (vector? x)
        (reduce clojure.core/* (map magnitude x))

        ;; Otherwise, it's a unit, magnitude is 1.
        :else
        1))

(defn remove-vals [m pred?]
  (reduce (fn [m' [k v]]
            (cond-> m'
              (pred? v)
              (dissoc k)))
          m
          m))

(defn mul-units [u1 u2]
  (merge-with clojure.core/+ u1 u2))

(defn unit [x]
  (-> (cond (number? x)
            {}

            (vector? x)
            (reduce mul-units (map unit x))

            (map? x)
            x

            ;; Otherwise, assume a base unit.
            :else
            {x 1})
      (remove-vals zero?)))

(defn simplify [x]
  (cond
    ;; Enhetsløse tall forenkler til kun tallet
    (every? zero? (vals (unit x)))
    (magnitude x)

    ;; Tall med størrelse 1 forenkler til kun enheten
    (and (= 1 (magnitude x))
         (every? #(not= 1 %) (vals (unit x))))
    (unit x)

    ;; for øvrige tall gir vi en vektor av faktorer.
    :else
    (->> [[(magnitude x)]
          (->> (unit x)
               (filter (comp #{1} second))
               (map first))
          (some->> (unit x)
                   (remove (comp #{0 1} second))
                   (into {})
                   not-empty
                   vector)]
         (into [] cat))))

(defn map-vals [m f]
  (reduce (fn [m' [k v]]
            (assoc m' k (f v)))
          {}
          m))

(def negate-vals #(map-vals % clojure.core/-))

(defn invert [x]
  (simplify [(clojure.core// (magnitude x))
             (negate-vals (unit x))]))

(defn div [x y]
  (simplify [(clojure.core// (magnitude x)
                             (magnitude y))
             (merge-with clojure.core/+
                         (unit x)
                         (negate-vals (unit y)))]))

(defn same-unit? [x y]
  (= (unit x) (unit y)))

(defn add [x y]
  (when-not (same-unit? x y)
    (throw (ex-info "Cannot add quantities of different units"
                    {:x x :y y})))
  (simplify
   [(clojure.core/+ (magnitude x) (magnitude y))
    (unit x)]))

(defn negate [x]
  (simplify [(clojure.core/- (magnitude x))
             (unit x)]))

(defn sub [x y]
  (when-not (same-unit? x y)
    (throw (ex-info "Cannot subtract quantities of different units"
                    {:x x :y y})))
  (simplify
   [(clojure.core/- (magnitude x) (magnitude y))
    (unit x)]))
```

... og her er API-et for folk!

```clojure
(ns munit.units
  (:refer-clojure :exclude [* / + -])
  (:require [munit.impl :refer [simplify invert div add negate sub]]))

(defn *
  ([] 1)
  ([x] x)
  ([x y] (simplify [x y]))
  ([x y & args]
   (simplify [x y (vec args)])))

(defn /
  ([x] (invert x))
  ([x y] (div x y))
  ([x y & args]
   (reduce div (div x y) args)))

(defn +
  ([] 0)
  ([x] x)
  ([x y] (add x y))
  ([x y & args]
   (reduce add (add x y) args)))

(defn -
  ([x] (negate x))
  ([x y] (sub x y))
  ([x y & args]
   (reduce sub (sub x y) args)))

(defn measure-in [x target-unit]
  (let [converted (/ x target-unit)]
    (when-not (number? converted)
      (throw (ex-info "Cannot convert to target unit"
                      {:quantity x
                       :target-unit target-unit
                       :leftover converted})))
    converted))
```

## Bakoverkompatibilitet med data

Overgangen fra records til data har gitt meg flere forbedringer:

- Mindre kode
- `def`-er for størrelser (Quantity) og base-enheter (BaseUnit) kan ikke lenger komme ut av synk med typedefinisjoner
- Du velger hvordan du vil ha base-enhet selv, for eksempel med nøkkelord eller symboler:
    ```clojure
    ;; velg hvordan du vil skrive base-enheter selv!
    :si/m :m 'm 'si/m
    ;; ... så lenge de kan sammenliknes med =.
    ```
- Printing og serialisering av data er trivielt (vanlig Clojure-data kan printes) og umagisk (ingen records som later som de er symboler).

... og denne overgangen kunne jeg gjøre uten å brekke public-API-et mitt!
Kodesnutten med de 17 MN var uendret mellom gammelt API og nytt API.

Dette er en ny måte å tenke API-design for meg.
Det har vært til stor hjelp å se Christian jobbe med Replicant og Nexus.
Stegene blir noe sånt:

- Hvilke datastrukturer er hyggelige å skrive inn fra REPL?
- Finn operasjonene som folk skal bruke (`* / + -`).
- ... og finn så funksjonene som trengs for å lage API-et (`simplify`, `magnitude` og `unit`).

Sam Ritchie spurte en gang Gerald Sussman om hvordan han så på formlene sine, om han brukte TeX.
Sussman svarte at han bare så dataene.
(For Sussman var data alltid lister.
 Sussman har jobbet mest i Scheme, og (faktisk) sammen med Guy Steele skrevet Scheme, et språk som har hatt stor innflytelse på Clojure.)
For meg ligger det noe viktig akkurat her: med en konsis og god datastruktur, blir den datastukturen i seg selv notasjon man "ser".
I dag ser du kanskje `[:em heisann!]` like godt som `<em>heisann!</em>`.
I morgen ser du kanskje `[9.8 m {s -2}]` like godt som `9.8 m/s²`?

## Interoperabilitet med data

Nå som munit ikke lenger krever noen bruk av spesifikke typer, passer munit mye bedre inn i andre systemer.
Det Hiccup har gjort for HTML og det Ring har gjort for HTTP-requests og HTTP-responses kan kanskje Munit gjøre for tall med SI-enhet.
Med en datanotasjon (en datastruktur) på plass, kan forskjellige biblioteker jobbe på samme datastruktur.

## To funksjoner erstattet typer i "midjen" til biblioteket

Veldig mange gode biblioteker er orientert rundt en "smal midje".
I Clojure-økosystemet kjenner du kanskje allerede midjene [Hiccup] for HTML og [Ring] for HTTP.
[Pandoc] er en fin midje for dokumenter.

[Hiccup]: https://github.com/weavejester/hiccup
[Ring]: https://github.com/ring-clojure/ring
[Pandoc]: https://pandoc.org/

Vi trenger *midjer* for å unngå dobbeltarbeid.
Forfatteren av Oil Shell sier det godt i [The Internet Was Designed With a Narrow Waist]:

> It avoids O(M × N) code explosions, letting us write O(M + N) amounts of code instead.
> This is a big deal in practice! Most code is glue, but it doesn't have to be this way.

[The Internet Was Designed With a Narrow Waist]: https://www.oilshell.org/blog/2022/02/diagrams.html

I første versjon av munit, hadde jeg ingen typer.
Men første versjon ble aldri ferdig, fordi jeg fikk en MxN-eksplosjon i implementasjonen av *, /, + og -!
Alle måtte plukke ut enhet og størrelse uten hjelp.

Kvantitet-typen tok så over som midjen i munit-med-typer:

```clojure
(defrecord Quantity [magnitude exponents])
```

... før jeg fant ut at jeg like gjerne kunne eksponere størrelse (`magnitude`) og enhet (som først het `exponents`, før den ble omdøpt til `unit`) som vanlige funksjoner.

*Takk til Mathias, Sigmund og Lars Kristian, som leste tidlige versjoner av denne teksten.
Teksten er langt bedre nå, mye takket være innspillene deres!
Takk til Colin Smith og Sam Ritchie for arbeidet på [Emmy].
Takk til Gerald Sussman og Chris Hanson for [Software Design for Flexibility], som skisserer ut arkitekturen som Munit følger.
Takk til Anteo AS og Tormod Mathiesen for [Broch], et annet bibliotek for tall med enhet.*

[Software Design for Flexibility]: https://mitpress.mit.edu/9780262045490/software-design-for-flexibility/
[Emmy]: https://github.com/mentat-collective/emmy
[Broch]: https://github.com/anteoas/broch
