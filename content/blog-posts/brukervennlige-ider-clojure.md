:page/title Brukervennlige ID-er i Clojure
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-30T09:00:01"
:blog-post/tags [:clojure :ux]
:blog-post/description

En konkret implementasjon av de fine id-ene jeg skisserte i [Datamodellering er
også UX-design](/datamodellering-ux/).

:open-graph/description

En algoritme i Clojure for å generere fine brukervennlige id-er.

:blog-post/body

I [Datamodellering er også UX-design](/datamodellering-ux/) skisserte jeg
ingrediensene i en algoritme for å generere brukervennlige id-er, men hvordan
gjør vi det faktisk i praksis? Her er én mulig løsning i Clojure.

Algoritmen krever at vi holder styr på en teller feks i en database. Jeg hopper
over det her, fordi detaljene avhenger av hvilken database du bruker. Men hver
gang du skal generere en id må du starte med å øke løpenummeret med én i en
transaksjon og få den nye verdien, la oss kalle den `n`:

```clj
(def n 1024)
```

Vi trenger så et alfabet, og antall tegn i det:

```clj
(def alphabet "23456789acefghjkqrsuwxz")
(def num-chars (count alphabet))
```

For å oversette tallet vårt (`1`) til en id i alfabetet vårt må vi konvertere
fra 10-tallssystemet til vårt håndsydde 23-tallssystem. Klar for en aldri så
liten bakoversveis?

```clj
(->> n
     (iterate #(/ % num-chars))
     (map long)
     (take-while pos?)
     (map #(mod % num-chars)))
```

Dette er en slags faktorisering. `iterate` returnerer en uendelig sekvens som
ser sånn ut:

```clj
(def n 1024)

(n
 (/ n 23)
 (/ (/ n 23) 23)
 (/ (/ (/ n 23) 23) 23)
 (/ (/ (/ (/ n 23) 23) 23) 23)
 ,,,)
```

`(map long)` runder disse av til heltall:

```clj
(1024
 44
 1
 0
 0
 ,,,)
```

`(take-while pos?)` plukker fra denne sekvensen så lenge tallet er større enn 0:

```clj
(1024
 44
 1)
```

Til slutt bruker vi modulus (`mod`) mot lengden på alfabetet til å finne hvor
mange det er av hver potens, `(map #(mod % num-chars))`, som gir oss:

```clj
(12
 21
 1)
```

Dette er som jeg nevnte en "slags faktorisering" (faktorene kommer i motsatt
rekkefølge), fordi det kan leses som "12 enere, 21 tiere og 1 hundrer", eller
mer presist med vårt alfabet:

```clj
12 * 23^0 ;; "Ener"
21 * 23^1 ;; "Tier"
 1 * 23^2 ;; "Hundrer"
```

Rekkefølgen på faktorene spiller liten rolle, så lenge alle id-er lages på samme
vis.

Vi kan nå slå opp faktorene i det nye alfabetet og sette det sammen til en
streng:

```clj
(def alphabet "23456789acefghjkqrsuwxz")
(def num-chars (count alphabet))

(defn encode-id [n]
  (->> n
       (iterate #(/ % num-chars))
       (map long)
       (take-while pos?)
       (map #(mod % num-chars))
       (map #(nth alphabet %)) ;; Slå opp
       (apply str)))           ;; Bygg streng
```

Dette gir oss id-en `"gx3"`. Hvis vi vil at alle id-er skal ha et visst antall
tegn, slik jeg foreslo, har vi et par valg. Vi gikk for den enkleste løsningen,
nemlig å starte løpenummeret vårt på det laveste tallet som gir x antall tegn,
altså `(Math/pow 23 (dec x))`, som med `x` lik 6 gir id-en `"222223"`. Dette
synliggjør også baklengsheten ved at "eneren" (altså 3) står bakerst, prefikset
av en haug "nuller" (altså 2).

## Litt morsommere id-er

ID-ene vi får nå ser veldig ut som løpenummere -- noe de jo er. For å redusere
den effekten valgte vi litt cheeky å stokke om alfabetet vårt, noe som også kan
gi opphav til noen søte formuleringer. Vi kjørte en `shuffle` og justerte litt
på resultatet for hånd og endte opp med dette alfabetet:

```clj
(def alphabet "awsg6x9h34j572uqr8feckz")
```

Vi så oss også fornøyd med id-er på 3 eller flere tegn, og endte da opp med
id-er som ser sånn ut:

```clj
(encode-id 529) ;;=> "aaw"
(encode-id 530) ;;=> "waw"
(encode-id 531) ;;=> "saw"
```

Blir ikke morsommere enn man gjør det til sjæl, si.
