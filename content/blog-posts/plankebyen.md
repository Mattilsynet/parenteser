:page/title Lange flate trær
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-09-03T09:00:00"
:blog-post/tags [:clojure]
:blog-post/series {:series/id :clojure-core}
:blog-post/description

Hjembyen min Fredrikstad er også kjent som Plankebyen, så du kan si at jeg har
alle forutsetninger for å forstå `tree-seq` -- funksjonen som lager lange lister
av trær.

:blog-post/body

Hjembyen min Fredrikstad er også kjent som Plankebyen, så du kan si at jeg har
alle forutsetninger for å forstå `tree-seq` -- funksjonen som lager lange lister
av trær.

## Forskjellen mellom `postwalk` og `tree-seq`

For et par uker siden så vi på `postwalk` og hvordan det var [et nyttig verktøy
for å gjøre oppdateringer i en trestruktur](/interpolasjon/). Vi brukte den til
å lage en `interpolate`-funksjon:

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

Observer at vi fikk beholde trestrukturen slik den var - med enkelte
modifikasjoner. Det er også nettopp her den store forskjellen ligger. Funksjonen
`tree-seq` bryter trestrukturen fra hverandre og gir oss en lang liste med alle
elementene i treet. Slik:

```clj
(def hiccup
  [:div
   [:h1 "Hallo " :navn "!"]
   [:p "Fint å se deg."]])

(tree-seq coll? identity hiccup)

;; => ([:div [:h1 "Hallo " :navn "!"] [:p "Fint å se deg."]]
;;     :div
;;     [:h1 "Hallo " :navn "!"]
;;     :h1
;;     "Hallo "
;;     :navn
;;     "!"
;;     [:p "Fint å se deg."]
;;     :p
;;     "Fint å se deg.")
```

Som du ser får vi en salig blanding av nivåer i trestrukturen. Ja, vi får
endatil hele det opprinnelige treet tilbake i første posisjon. Det oppleves
kanskje ikke så veldig hendig, men se nå: Vi kan slenge på et filter.

```clj
(->> (tree-seq coll? identity hiccup)
     (filter string?))

;; => ("Hallo "
;;     "!"
;;     "Fint å se deg.")
```

Nå begynner det å ligne på noe. Her er alle rå tekststrenger i dom-strukturen.
Det kan være nyttig hvis vi vil sikre at vi alltid bruker `[:i18n]`-tagger, for
eksempel.

Eller se her:

```clj
(->> (tree-seq coll? identity hiccup)
     (filter keyword?))

;; => (:div :h1 :navn :p)
```

Nå fant vi alle keywords som var i bruk. Vi kunne for eksempel brukt det til å
sjekke at det alltid er en og bare én `:h1` på siden:

```clj
(->> (tree-seq coll? identity hiccup)
     (filter #{:h1})
     count)

;; => 1
```

Så ble det hendig likevel.

Her kan du i grunn slutte å lese for 95% av all bruk av `tree-seq`, men hvis du
er ille nysgjerrig på detaljene så har jeg litt mer rare greier på lager.

## Hva er greia med `coll?` og `identity`?

Første parameter til `tree-seq` (normalt sett `coll?`) er et predikat som avgjør om
en node i treet skal manøvreres ned i - eller om den skal behandles som en
løvnode.

Andre parameter (normalt sett `identity`) er en funksjon hvis jobb er å
returnere en liste med nye noder, dersom predikatet slo til.

Vi kunne for eksempel driste oss til å manøvrere ned i strenger også:

```clj
(->> (tree-seq
      #(or (coll? %)
           (string? %))
      seq
      hiccup)
     (filter char?))

;; => (\H \a \l \l \o \space \!
;;     \F \i \n \t \space \å
;;     \space \s \e \space
;;     \d \e \g \.)
```

Om det ble så veldig nyttig, vet jeg ikke, men det demonstrerer at man selv kan
bestemme hvordan `tree-seq` ser på trestrukturene vi gir den.

Mer nyttig er eksempelet fra Christian sin bloggpost om [Kode som skriver
kode](/detektimen/):

```clj
(defn get-syms [xs]
  (->> xs
       (tree-seq coll? (fn [x]
                         (cond-> x
                           (and (map? x) (:as x))
                           (select-keys [:as]))))
       (filter symbol?)
       (remove #{'&})))
```

Her erstatter vi `identity` med vår egen funksjon, for å snevre ned hva som
teller som noder på vår vei nedover i treet:

```clj
(cond-> x
  (and (map? x) (:as x))
  (select-keys [:as]))
```

Det meste får flyte gjennom som vanlig (`x`), men dersom det er map som
inneholder nøkkelen `:as`, så trår vår `select-keys` til.

Resulatet er at når vi har en destrukturering av denne typen:

```clj
(defn test [{:keys [bar baz] :as foo}])
```

Så blir symbolet `foo` valgt ut, mens `bar` og `baz` blir droppet. Akkurat det
vi ønsket oss for loggingen vår.

## Til slutt

Ingen av oss har noe særlig lyst til å drive med [tømmerfløting nedover
Glomma](https://digitaltmuseum.no/011012567032/tommerfloting-pa-glomma-funnefoss).
På samme måte prøver vi å unngå å jobbe med dypt nøsta data. Aller helst jobber vi med
[flate, møre data](/flate-data/), men når vi har trestrukturer i hendene er
`postwalk` og `tree-seq` fine verktøy å ha i kassa.
