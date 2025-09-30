:page/title Defaktorering?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-09-30T09:00:00"
:blog-post/tags [:design :metodikk]
:blog-post/description

Kan det samme grepet først gjøre koden bedre for så å gjøre den verre? Javisst,
dersom forutsetningene endrer seg.

:blog-post/body

Den siste uka har vi skrevet kode som prøver å forstå data fra et eldre system.
I sånne situasjoner må man av og til ty til noen kreative grep.

På et tidspunkt skrev vi denne rakkeren:

```clj
(defn finn-lukking-basert-på-lukkedato [tilsynsavtaler vedtak]
  (let [prefiks (ld->timestamp (:LUKKET vedtak))
        treff (->> tilsynsavtaler
                   (map :IDSTRING)
                   (filter #(str/starts-with? % prefiks)))]
    (when-not (next treff)
      (first treff))))
```

La meg forsøke å forklare hva denne gjør, og hvorfor.

Jeg kunne skrevet mangt og meget om meningsbærende id-er, men i denne
sammenhengen så er det, dessverre, nyttig informasjon i id-en som ikke er
tilgjengelig noe annet sted. `:IDSTRING`-er ser sånn ut:

```
"Z2509031641352050240HQPUN_TilsynAvtale"
```

Den observante leser vil kanskje se sporet av et tidsstempel, og visst: id-en
sier noe om når denne tingen ble opprettet, med en presisjon som ikke er
tilgjengelig ellers i datasettet.

Funksjonen over prøver altså å finne den tilhørende tilsynsavtalen ved å
sammenligne datoen i `(:LUKKET vedtak)` med deler av en id. Ikke den mest
elegante koden vi har skrevet, men i nøden spiser fanden fluer.

Litt dypere ned i kaninhullet kom vi opp med enda en funksjon som gjorde
unevnelige ting med et eksternt system sine id-er:

```clj
(defn finn-lukking-av-gebyr [tilsynsavtaler vedtak]
  (when-let [ld (and (:GEBYRBELOEP vedtak)
                     (:LUKKET vedtak))]
    (let [prefiks (ld->timestamp ld)]
      (->> tilsynsavtaler
           (map :IDSTRING)
           (filter #(str/starts-with? % prefiks))
           (apply greatest)))))
```

Denne gjør mye av det samme som den forrige, og det er fortsatt like rart.
Hvordan skulle vi sørge for at denne koden ikke vipper folk som kommer etter oss
fullstendig av pinnen?

Etter å ha vurdert kommentarer og docstrings landa vi på en nokså elegant
refaktorering:

```clj
(defn finn-tilsynsavtaler-med-tidspunkt [tilsynsavtaler ld]
  (let [prefiks (ld->timestamp ld)]
    (->> tilsynsavtaler
         (map :IDSTRING)
         (filter #(str/starts-with? % prefiks)))))

(defn finn-lukking-basert-på-lukkedato [tilsynsavtaler vedtak]
  (let [treff (finn-tilsynsavtaler-med-tidspunkt tilsynsavtaler (:LUKKET vedtak))]
    (when-not (next treff)
      (first treff))))

(defn finn-lukking-av-gebyr [tilsynsavtaler vedtak]
  (when-let [ld (and (:GEBYRBELOEP vedtak)
                     (:LUKKET vedtak))]
    (->> (finn-tilsynsavtaler-med-tidspunkt tilsynsavtaler ld)
         (apply greatest))))
```

Ok, her er det fortsatt mye rart, hva er det som er så elegant? Vel, vi fikk
isolert den mest hårete logikken - den som forventer å finne tidspunkter i
id-er - bak et forklarende navn. Om nødvendig kan vi forklare denne logikken
ytterligere med en docstring, og nå bare på ett sted.

Ved å trekke ut det som var felles i de to funksjonene har vi også tydeliggjort
forskjellen på disse.

Litt indireksjon har altså hjulpet oss å forstå forskjellen på to funksjoner som
har mye til felles.

## Defaktoreringen

Enda litt dypere ned i kaninhullet viser det seg at også
`finn-lukking-basert-på-lukkedato` burde gjøre `greatest` og ikke `first`:

```clj
(defn finn-lukking-basert-på-lukkedato [tilsynsavtaler vedtak]
  (->> (finn-tilsynsavtaler-med-tidspunkt tilsynsavtaler (:LUKKET vedtak))
       (apply greatest)))

(defn finn-lukking-av-gebyr [tilsynsavtaler vedtak]
  (when-let [ld (and (:GEBYRBELOEP vedtak)
                     (:LUKKET vedtak))]
    (->> (finn-tilsynsavtaler-med-tidspunkt tilsynsavtaler ld)
         (apply greatest))))
```

Dermed er disse like nok til at vi ikke trenger begge, og vi tok bort
`finn-lukking-av-gebyr`. Da satt vi igjen med dette:

```clj
(defn finn-tilsynsavtaler-med-tidspunkt [tilsynsavtaler ld]
  (let [prefiks (ld->timestamp ld)]
    (->> tilsynsavtaler
         (map :IDSTRING)
         (filter #(str/starts-with? % prefiks)))))

(defn finn-lukking-basert-på-lukkedato [tilsynsavtaler vedtak]
  (->> (finn-tilsynsavtaler-med-tidspunkt tilsynsavtaler (:LUKKET vedtak))
       (apply greatest)))
```

Plutselig fremsto ikke refaktoreringen like hjelpsom lenger. Når den ekstra
funksjonen bare kalles fra ett sted, lager den indireksjon uten noen særlig
verdi.

Så da runda vi like gjerne av med en ny refaktorering, som i effekt reverserte
den første. En _defaktorering_ om vi er i det lystige hjørnet:

```clj
(defn finn-lukking-basert-på-lukkedato [tilsynsavtaler vedtak]
  (let [prefiks (ld->timestamp (:LUKKET vedtak))]
    (->> tilsynsavtaler
         (map :IDSTRING)
         (filter #(str/starts-with? % prefiks))
         (apply greatest))))
```

There and back again.

Refaktorering er en essensiell del av å skrive god kode. Men nådeløs oppdeling
av kode er ikke et mål i seg selv. Indireksjon kan være et slagkraftig verktøy
som hjelper kode å kommunisere bedre, men har også en kostnad. Direkte og rett
frem kode er lettere å lese og forstå, og når indireksjonen ikke lenger er
nødvendig kan vi forbedre koden ved å flette ting sammen igjen.
