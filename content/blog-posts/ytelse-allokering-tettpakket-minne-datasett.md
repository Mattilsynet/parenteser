:page/title Ytelse for funksjonelle programmerere: allokering og tettpakket data
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-04-24T12:02:06.511912"
:blog-post/tags [:ytelse]
:blog-post/description

Ytelse er ikke et problem inntil ytelse er et problem.
Men må vi egentlig ned i et lavnivå-språk når ting ikke går raskt nok?

:blog-post/body

<style type=text/css>
/* Savner borders på tabeller! */
table {
    margin-left: auto;
    margin-right: auto;
}
table, th, td {
    border: 1px solid black;
}
th, td {
    padding: 0.2em;
}
h3 {
    /* Jeg synes det er vanskelig å skille h3-er fra h2-er */
    /* font-size: 32px !important; */
    /* Den diskusjonen kan vi heller ta felles og få inn i global CSS enn at jeg fikler her. */
}
</style>

Ytelse er unødvendig å bry oss om, ikke sant?
Vi parafraserer ofte Donald Knuth når vi sier "premature optimization is the root of all evil".
Det fulle sitatet er mer nyansert:

> Programmers waste enormous amounts of time thinking about, or worrying about,
> the speed of noncritical parts of their programs, and these attempts at
> efficiency actually have a strong negative impact when debugging and
> maintenance are considered. We should forget about small efficiencies, say
> about 97% of the time: premature optimization is the root of all evil. Yet we
> should not pass up our opportunities in that critical 3%.

Effekten av å ikke tenke ytelse i de resterende 3 % kan være treg feedback under
lokal jobbing, og unødvendig kompiliserte systemer fordi man velger å mellomlagre trege beregninger.

## Mål hvor lang tid det tar!

Når du skal skrive rask kode, må du måle!
Jeg kommer til å vise dagens eksempler med en hjemmesydd Clojure-makro.

```clojure
(def nanos->millis (partial * 1e-6))

(defn measure-ms*
  "Measure the execution time of body-fn. Returns a tuple of [result, time-ms]."
  [body-fn]
  (let [before-ns (System/nanoTime)
        result (body-fn)
        after-ns (System/nanoTime)]
    (vector result (nanos->millis (- after-ns before-ns)))))

(defmacro measure-ms [& body] `(measure-ms* #(do ~@body)))
```

`measure-ms` returnerer en tuppel med verdien du regnet ut og antallet millisekunder det tok.
La oss se om `Thread/sleep 300` faktisk venter i 300 ms.

```clojure
(measure-ms (Thread/sleep 300) :done)
;; => [:done 305.109125]
```

Jepp! 300 ms pluss litt mer.

... Men er det godt nok å måle sånn?
`measure-ms` sier ingenting om variasjonen i timing over tid.
Og jeg fikk variasjon i timing da jeg gjorde målinger!
Denne artikkelen illustrerer poenger om ytelse knyttet til allokering og tettpakket minne, den gir ikke fullstendige benchmarks.
Hvis du vil ha bedre svar på "hvor lang tid tar det?" som ikke bare gir en pekepinn på størrelsesorden, se  https://clojure-goes-fast.com/.

## Allokering én gang eller mange ganger

Vi skal først undersøke effekten av å allokere mange ganger eller allokere én gang.

Hver allokering tar tid, og vi må vente på operativsystemet — allokering har en konstantkostnad.
Når vi allokerer én gang, tar vi den kostnaden én gang.
Når vi allokerer mange ganger, tar vi den kostnaden mange ganger.

Til eksemplene under kommer jeg til å bruke en million heltall i en
Clojure-vektor, allokert før vi starter timeren.

```clojure
(def million 1000000)
(def n million)
(def xs (vec (range n)))
```

### `java.util.ArrayList`, én verdi av gangen

```clojure
(measure-ms
  (let [l (java.util.ArrayList.)]
    (doseq [x xs]
      (.add l x)))
  :done)
;; => [:done 32.91675]
```

32 ms, med vårt grove målingsverktøy.

### `java.util.ArrayList`, alle verdiene på én gang

Med samme ArrayList, men i én sjau går det fortere:

```clojure
(measure-ms (java.util.ArrayList. xs) :done)
;; => [:done 24.010040999999998]
```

### Java-array med tall i

Men vi trenger ikke dynamisk allokering av flere verdier når vi allerede har alle verdiene!
Da kan vi bruke en dum java-array.

```clojure
(measure-ms (int-array xs) :done)
;; => [:done 15.028208]
```

Enda raskere!

## Tall i minnet: tettpakket eller her og der

CPU-en din er som en togstasjon.
Du har instruksjonene dine om hva du skal gjøre (koden).
Det kommer et tog med data inn, og du skal sende ut ny data på et annet tog.

Tall pakket tett i minnet er et fullt tog.
CPU-en får lastet mange tall samtidig, og du kan bruke beregningskapasiteten til CPU-en effektivt.

Når toget i stedet kun inneholder adresser til tall som ligger andre steder, blir flaskehalsen i stedet venting på andre tog.
Nettopp dette skjer når vi har en ArrayList av objekter, eller en Clojure-vektor av maps.

CPU-en må lete etter tallene andre steder, og total tid brukt er dominert ventingen.

### Indreprodukt med map og reduce

Indreproduktet ("dot product") av to vektorer er summen av det elementvise produktet av par av elementer i hver vektor.
Phew!
Kodeeksempel.

```
[1 2 3] · [4 5 6]
  = 1*4 + 2*5 + 3*6
  = 4 + 10 + 18
  = 32
```

Dette er mat for map og reduce!

```
(defn dotprod-1 [xs ys]
  (reduce + (map * xs ys)))
```

Ble det riktig, da?

```clojure
(dotprod-1 [1 2 3] [4 5 6])
;; => 32
```

Det stemmer med eksempelet!

Til å måle ytelse, bruker vi x-ene våre fra over (0 til 999 999), samt y-er som er x-ene med negativt fortegn (0 til -999 999).

```clojure
(take 10 xs)
;; => (0 1 2 3 4 5 6 7 8 9)

(def ys (vec (map - xs)))
(take 10 ys)
;; => (0 -1 -2 -3 -4 -5 -6 -7 -8 -9)
```

På tide å måle:

```clojure
(measure-ms (dotprod-1 xs ys))
;; => [-333332833333500000 76.89958299999999]
```

| Metode     | input-type       | Tid   |
|------------|------------------|-------|
| map/reduce | PersistentVector | 77 ms |

### Indreprodukt med loop/recur og spredt minne

Så du noe muffens med map/reduce-indreproduktet over?

Ja, det var noe muffens.
Den allokerte!
Returverdien fra map er en lazy-seq.
Kan vi unngå den allokeringen?

```clojure
(defn dotprod-2 [xs ys]
  (let [len (min (count xs) (count ys))]
    (loop [i 0
           accum 0]
      (if (< i len)
        (recur (inc i)
               (+ accum (* (get xs i)
                           (get ys i))))
        accum))))
```

Ingen lazy-seq som mellomverdi.
Hjelper det?

```clojure
(measure-ms (dotprod-2 xs ys))
;; => [-333332833333500000 33.972291999999996]
```

Det hjalp!

| Metode     | input-type       | Tid   |
|------------|------------------|-------|
| map/reduce | PersistentVector | 77 ms |
| loop/recur | PersistentVector | 34 ms |

### Indreprodukt med loop/recur og tettpakket minne

`clojure.lang.PersistentVector` er et [Hash Array Mapped Trie] under panseret.
Det gjør endring av verdier til en O(1)-operasjon med strukturell deling.
Kostnaden for strukturell deling er at vi ikke får tettpakket minne.

[Hash Array Mapped Trie]: https://en.wikipedia.org/wiki/Hash_array_mapped_trie

Tettpakket minne får vi derimot med gode, gamle Java-arrays!

```
(set! *warn-on-reflection* true)

(defn dotprod-3 [^longs xs ^longs ys]
  (let [len (min (count xs) (count ys))]
    (loop [i 0
           accum 0]
      (if (< i len)
        (recur (inc i)
               (+ accum (* (aget xs i)
                           (aget ys i))))
        accum))))

(def xs-longs (long-array xs))
(def ys-longs (long-array ys))

(measure-ms (dotprod-3 xs-longs ys-longs))
;; => [-333332833333500000 7.029]
```

Datatypen vi akkumulerer med har også litt å si.
Clojure gir oss Long til heltall.

```clojure
(type 1)
;; => java.lang.Long
```

La oss prøve oss med flyttall.

```clojure
(defn dotprod-4 [^floats xs ^floats ys]
  (let [len (min (count xs) (count ys))]
    (loop [i 0
           accum 0.0]
      (if (< i len)
        (recur (inc i)
               (+ accum (* (aget xs i)
                           (aget ys i))))
        accum))))

(def xs-floats (float-array xs))
(def ys-floats (float-array ys))

(measure-ms (dotprod-4 xs-floats ys-floats))
;; => [-3.3333283333312755E17 3.62012]
```

| Metode     | input-type       | Tid   |
|------------|------------------|-------|
| map/reduce | PersistentVector | 77 ms |
| loop/recur | PersistentVector | 34 ms |
| loop/recur | long/1           | 7 ms  |
| loop/recur | float/1          | 4 ms  |

(Hvis du lurer på hva `^longs` og `^floats` gjør - disse er typehint til Java, som gjør at vi unngår reflection.
Uten typehint bruker denne koden 3-4 sekunder i stedet for 1-10 millisekunder.
Faktor 1000 er en stor forskjell!
Tusen takk til Eugene Pakhomov på Clojurians-Slacken som [hjalp meg å plassere typenhintene].)

[hjalp meg å plassere typenhintene]: https://clojurians.slack.com/archives/C053AK3F9/p1745483613432909

## Array-programmering og datasett

Så da må vi skrive imperativ loop/recur og typehinte med Java-typer for å få farten vi ønsker, da?
Det var jo ikke sånn vi hadde lyst til å skrive Clojure?

Nei!
Det finnes bedre løsninger.

Vi kan skrive høynivå-kode som følger prinsippene våre for allokering og tettpakket minne ved å:

1. Holde tall i minnet som _typede arrays_
2. Gjør tallknusing med batch-operasjoner på typede arrays.

Dette kalles array-programmering!
Ideen er gammel.
[APL] ("A Programming Language", 1966) er array-orientert, og lar oss skrive indreproduktet vårt slik:

[APL]: https://en.wikipedia.org/wiki/APL_(programming_language)

```apl
      1 2 3 +.× 4 5 6
32
```

Prøv selv på https://tryapl.org/. For å lære mer om Array-programmering når du kommer fra et Clojure-tankesett, anbefaler jeg Dave Liepmanns [Array languages for Clojurians].

[Array languages for Clojurians]: http://www.appliedscience.studio/articles/array-programming-for-clojurists.html

Kun arrays gir oss en utfordring med organisering av kode.
Hvis vi bare sender rundt typede arrays, hvordan vet vi egentlig hva ting er?
For å vite hva ting er, kan vi gi arrays _navn_.
Hvis vi i tillegg tenker på arrays som _kolonner_, og samler noen navngitte kolonner, har vi et _datasett_.

| x | y |
|---|---|
| 1 | 4 |
| 2 | 5 |
| 3 | 6 |

Voila!

La oss se hvordan det går med vektorproduktet vårt hvis vi bruker tablecloth-datasett, og tablecloth-API-er.

```clojure
(require '[tablecloth.api :as tc]
         '[tablecloth.column.api :as tcc])

(let [ds (tc/dataset {:x xs :y ys})]
  (measure-ms
    (tcc/sum (tcc/* (:x ds) (:y ds)))))
;; => [-3.333328333335E17 1.399125]
```

| Metode     | input-type       | Tid   |
|------------|------------------|-------|
| map/reduce | PersistentVector | 77 ms |
| loop/recur | PersistentVector | 34 ms |
| loop/recur | long/1           | 7 ms  |
| loop/recur | float/1          | 4 ms  |
| Datasett   | Tablecloth       | 1 ms  |

## Ytelse for funksjonelle programmerere, oppsummert

I Clojure jobber vi ofte med sequences av maps.

```clojure
(let [the-maps [{:x 1 :y 4}
                {:x 2 :y 5}
                {:x 3 :y 6}]]
  (reduce + (map *
                 (map :x the-maps)
                 (map :y the-maps))))
;; => 32
```

Når ytelsen med map, reduce og venner ikke lenger er akseptabel, er ofte datasett en effektiv og behagelig løsning.

```clojure
(let [ds (tc/dataset {:x [1 2 3] :y [4 5 6]})]
  (tcc/sum (tcc/* (:x ds) (:y ds))))
;; => 32.0
```

Mye av forklaringen for _hvorfor_ datasett gir bedre ytelse ligger i færre allokeringer, og tall pakket tett i minnet.

## Referanser og videre lesing

- Noj er en kuratert startpakke for å gjøre data science i Clojure.
  Start med [Noj-boka] og se etter Tablecloth i innholdsfortegnelsen for å prøve programmering med datasett.

- Mye av data science-stacken i Clojure bygger på biblioteker skrevet av [Chris Nuernberger].
  Chris jobbet med ytelseskritisk C++ for Nvidia før han skiftet fokuset til Clojure.
  I [Building Clojure data science ecosystem - Chris Nuernberger - Scicloj Interview 2] intervjuer [Daniel Slutsky] og jeg Chris, og i
  [Why dtype-next?] forklarer Chris grunding hvorfor Tablecloth egentlig går så fort.

- [Noj Light #1] er en online-konferanse for data science med Clojure, avholdt 16. og 17. mai 2025.
  For oss i Norge har vi kanskje andre planer den 17., men jeg planlegger i alle fall å se opptak!

[Noj-boka]: https://scicloj.github.io/noj/
[Chris Nuernberger]: https://github.com/cnuernber
[Daniel Slutsky]: https://github.com/daslu
[Building Clojure data science ecosystem - Chris Nuernberger - Scicloj Interview 2]: https://www.youtube.com/watch?v=zYNlZXTV14E
[Why dtype-next?]: https://cnuernber.github.io/dtype-next/datatype-to-dtype-next.html
[Noj Light #1]: https://scicloj.github.io/scinoj-light-1/
