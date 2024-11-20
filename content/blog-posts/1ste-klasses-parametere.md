:page/title Førsteklasses parametere i praksis
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-11-20T09:00:00"
:blog-post/tags [:design :clojure]
:blog-post/description

Dette blir ikke en lang bloggpost, jeg hadde bare lyst til å vise deg noe kult.
Jeg skrev tidligere om velkjente parameternavn. Det ble ganske stilig også i
møte med routing og forms.

:blog-post/body

Hei! Dette blir ikke en lang bloggpost, jeg hadde bare lyst til å vise deg noe
kult. I [«Forsvar mot svartekun... parametere!»](/forsvar-mot-svartekunster/)
skrev jeg om velkjente (*well-known*) parameternavn. Det viste seg å bli ganske
stilig også i møte med routing og forms.

## Routing

Vi har toveis routing i app-en vår. Det betyr at vi kan gå fra en URL til det vi
kaller en `location` (et map som beskriver en konkret instans av en side)

```clj
(router/arrive "/statistikk/2024")

;; => {:page-id :pages/statistikkside
;;     :params {:saksår 2024}}
```

men vi kan også gå fra en location til en URL:

```clj
(router/depart {:page-id :pages/statistikkside
                :params {:saksår 2024}})

;; => "/statistikk/2024"
```

Dette er kult av flere grunner, men spesielt nyttig er at sidens URL bare er
definert ett sted. Da blir det vanskeligere å lage brukne lenker.
Det ser omtrent sånn ut:

```clj
(def page
  {:id :pages/statistikkside
   :route ["statistikk" :saksår]})
```

## Førsteklasses parametere

Du la kanskje merke til at `:saksår` endte opp som et tall `2024`, og ikke en
streng `"2024"` når routinga hadde kappet opp URL-en?

Det gjør den fordi den vet at `:saksår` alltid er et tall i koden vår. Slik ser
det ut i praksis:

```clj
(def param-mappings
  {:saksår {:arrive parse-long
            :depart str}})
```

Parameteret `:saksår` blir gjort til en string når vi lager en URL (på vei
bort, altså `depart`), mens når vi ankommer siten via en URL blir den parset som
en long.

Allerede nyttig, men dette skulle vise seg å bare være begynnelsen. Det tok ikke
lang tid før flere slike definisjoner ramlet inn. Se bare her:

```clj
(router/depart {:page-id :pages/regionside
                :params {:region/id "M25000"}})

;; => "/region/nord"
```

Hvorfor skal våre brukere belemres med interne ID-er for en region, når de kan
få forholde seg til navn som de kan godt?

```clj
(router/arrive "/kravpunkter/renhold")

;; => {:page-id :pages/kravpunktside
;;     :params {:kravpunkt/id :kravpunkt/renhold}}
```

I vår datamodell prefikser vi alle kravpunktene med `:kravpunkt/` slik at de er
lette å holde styr på. Igjen hjelper routinga og parameter-definisjonene oss å
holde implementasjonsdetaljene for oss selv.

## Query-parametere

Når vi så skulle slenge parametere etter `?` i URL-en, så koblet vi på det
samme systemet. Da dukket `:url-alias` opp, slik:

```clj
(def param-mappings
  {:saksår {:arrive parse-long
            :depart str
            :url-alias :saksaar}})
```

Ja, det ble nærmest tvingende nødvendig. Jeg mener, hvem har lyst til å se dette?

```
/statistikk/?saks%C3%A5r=2024
```

Nei, da blir det bedre med

```
/statistikk/?saksaar=2024
```

I koden bruker vi fortsatt `:saksår` overalt. URL-aliaset brukes bare akkurat
idet vi mapper til og fra en URL. Nydelig.

## Forms

Så skulle vi lage skjemaer. Det ble jo den sanne fryd, for vi igjen koblet på
det samme systemet:

```html
<form>
  <input name="saksår" value="2024"/>
  <input name="region/id" value="nord"/>
  <input name="kravpunkt/id" value="renhold"/>
</form>
```

Når denne form-en postes, så får vi:

```clj
{:saksår 2024
 :region/id "M25000"
 :kravpunkt/id :kravpunkt/renhold}
```

Jeg må fortelle mer om hvordan vi håndterer skjemaer snart, men jeg lovet at
denne bloggposten skulle bli kort.
