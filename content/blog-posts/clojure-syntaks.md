:page/title Clojure-syntaks 101
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-06-04T09:00:00"
:blog-post/tags [:clojure :data]
:blog-post/description

I dag skal jeg vise deg all syntaksen i Clojure. I motsetning til hva du tror så
er det en ganske interessant øvelse. Det er nemlig mer til dette språket enn
mange parenteser.

:open-graph/description

En rask introduksjon til syntaksen i Clojure, og litt om hvor den kommer fra.
Det er mer interessant enn du tror!

:blog-post/body

I dag skal jeg vise deg all syntaksen i Clojure. I motsetning til hva du tror så
er det en ganske interessant øvelse. Det er nemlig mer til dette språket enn
mange parenteser.

I Clojure kan vi opprette alle de vanligst brukte datatypene med dedikert
syntaks, med såkalte dataliteraler. Du kjenner nok igjen en del av dem.

##### Strenger

```clj
"Hei på deg!"
```

##### Tall

```clj
3.14
42
```

##### Booleans

```clj
true
false
```

##### Symboler og nøkkelord

Clojure har også to datatyper som oftest brukes til å referere til ting.

Symboler ser ut som det mange andre språk kaller "identifiers":

```clj
name
lower-case
parenteser.page/render-page
```

Nøkkelord (keywords) brukes til navngivning og som nøkler i maps, enum-verdier
osv:

```clj
 :name
 :page/kind
 :page.kind/frontpage
```

Både symboler og nøkkelord kan ha navnerom, som står foran skråstreken i
eksemplene over. Jeg har tidligere skrevet litt om [bruk av nøkler](/nokler/).

Jeg kom til Clojure fra Ruby. Ruby kaller artig nok `:name` for et symbol 😅
Clojure og Ruby bruker forskjellige navn om denne datatypen, men bruker den på
samme måte.

## Collections

Clojure har dataliteraler for collections også.

##### Vektorer

Vektorer ligner mye på arrays i andre språk, og har effektiv indeksoppslag og
appends. Du kan bruke komma til å separere elementene, men du må ikke. Clojure
behandler komma som whitespace.

```clj
["Magnar" "Christian" "Ole-Marius"]
```

##### Lister

```clj
(1 2 3)
```

##### Sett

Sett er en samling av unike elementer.

```clj
#{1 2 3 4}
```

##### Maps

Maps er den datastrukturen vi bruker aller mest til å modellere data i Clojure.
Den kan ha vilkårlige typer som både nøkler og verdier, men veldig ofte er det
keywords som brukes som nøkler.

```clj
{:name "Christian"
 :employed? true
 :children 2}
```

## Datastrukturer? Hvor er syntaksen?

Greit, Clojure har noen dataliteraler. Men hva med syntaksen? Du kan faktisk
allerede hente diplomet ditt, for du kjenner nå 99% av syntaksen til Clojure.
Hva? Jo, nå skal du høre: Clojure-kode uttrykkes med Clojure sine
datastrukturer.

Her er bittelitt mer syntaks: `;` starter en kommentar. Konvensjonen tilsier at
vi bruker `;;`.

La oss se på en funksjon:

```clj
(defn greet [name]
  (str "Hello, " name))
```

Hva ser vi her? Jo en liste med blandet innhold:

```clj
(           ;; Liste
 defn       ;; Symbol
 greet      ;; Symbol
 [name]     ;; Vektor med et symbol
 (          ;; Ny liste
  str       ;; Symbol
  "Hello, " ;; Streng
  name      ;; Symbol
  )
)
```

Da blir det forhåpentligvis litt tydeligere hvorfor parentesene hele tiden står
på "feil" side: funksjonskall, funksjonsdefinsjoner og alt annet som er omringet
av parenteser _er lister_.

Kode **er** data. Sagt på en annen måte: I Clojure skriver du i praksis
[AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree)-en direkte. Hva gir
det oss? MASSE!

Når kode er data så kan vi redigere koden med [strukturelle
operasjoner](https://paredit.org/). Dette er både mer presist og mer kraftfult
enn å manuelt redigere syntaks. Se bare [hva Magnar får til når han er
gira](https://emacsrocks.com/e14.html).

Når kode er data kan vi få kompilatoren til å skriver om koden vår med såkalt
metaprogrammering med [makroer](https://clojure.org/reference/macros). Et
utrolig skarpt verktøy som vi ikke bruker så ofte. Men makroer har eksempelvis
gitt oss Go sin `go`-konstrukt som [et
bibliotek](https://github.com/clojure/core.async). Dette er en endring som i de
aller fleste andre språk hadde krevd ny syntaks og støtte i den offisielle
kompilatoren. Med makroer kan miljøet rundt utvide språket med biblioteker.
Ganske kult!

Når kode er data så kan vi lett bygge DSL-er som "ser ut som" kode, med ren
data. Det betyr at feks konfigurasjon kan ligge tett opptil koden vår uten at
den trenger å være Touring-komplett.

Når kode er data kan vi bruke standardbiblioteket som vi bruker til
dataprosessering til å manipulere koden vår.

Data er fete greier, og i Clojure er sørenmeg til og med koden data. Er det rart
vi er glad i Clojure?
