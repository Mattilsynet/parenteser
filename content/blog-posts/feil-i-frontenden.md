:page/title Hva kan gå galt i nettleseren til folk?
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2025-01-14T09:00:00"
:blog-post/tags [:framsideutvikling :null-hull]
:blog-post/description

Her er et av våre mål: Ingen kjente feil i prod. Det er ambisiøst, men vi
vil ikke leve med knuste vinduer. Så hvordan håndterer vi det i frontenden,
hvor så mye rart kan gå galt?

:blog-post/body

Her i Team Mat sine grønne enger har vi noen ambisiøse mål. Et av dem er *null
hull*. Kort fortalt: Ingen kjente feil i prod. Vi vil ikke leve med [knuste
vinduer](https://blog.codinghorror.com/the-broken-window-theory/).

Det betyr at alle exceptions fra serveren flakser rett inn i Slack-kanalen vår,
og får første prioritet å fikse. Vi har ikke satt opp noen metrics over antall
exceptions. Vi har som mål å *ikke* gjøre det.

Enten er tallet 0, eller så jobber vi med å få det ned i 0.

## Ikke hele bildet

Så var det denne nettleseren, da. Exceptions på serveren, ja vel, men vi shipper
jo temmelig mye kode til folk sin nettleser. Hva med den?

Det skal jeg si deg!

Å kjøre kode uten exceptions i noen andre sin runtime, på noen andre sin maskin,
det er ikke bare-bare. Med ett befinner man seg i en verden hvor feilmeldinger
fra nettleser-plugins plutselig dukker opp i Slack.

Altså, å oppdage feilene er greit:

```clj
(set! (.-onerror js/window)
  (fn [message url line-number col-number error]
    ,,,))
```

Å sende den, likeså:

```clj
(js/sendBeacon js/window "/frontend-exception" exDetails)
```

Så, selv om vi vil ha null hull, handler denne bloggposten om alle de feilene vi
IKKE vil ha rett i fleisen.

## Noen andres maskiner

Det er ikke til å komme utenom at nettleseren er ulendt terreng. La oss se på
noen av humpene i veien. Disse er samlet opp gjennom en lang karriere, så ikke
alt er like relevant her hos Mattilsynet, men jeg tar det med likevel. Slik som
det første:

#### Noen andres script

I klienten kan det fort kjøre kode fra noen helt andre, rett ved siden av din
egen. Det er spesielt aktuelt hvis du drar inn masse ræl for å spore brukerne
dine. Kanskje du endatil lar markedsføringsfolka dra inn all mulig drit med
Google Tag Manager? Grøss og gru. Slutt med det.

Uansett så trenger du da en sånn:

```clj
(defn someone-elses-problem? [message url line-number col-number]
  (and (= col-number 0)
       (= line-number 0)
       (= url "")
       (= message "Script error.")))
```

Slik ser det altså ut når man får en feilmelding som stammer fra script fra et
annet domene enn det ditt kom fra.

Det lar vi gli forbi i stillhet.

#### Noen andres nettleser-plugin

Feilmeldinger fra plugins i nettleseren blir også rapportert til `onerror`. Og
folk har det med å installere de artigste plugins. Krasjer gjør de óg!

Hos oss har vi gjort dette:

```clj
(defn browser-extension-exception? [error]
  (re-find #"moz-extension" (or (some-> error .-stack) "")))
```

Den ser etter `moz-extension` i feilens stacktrace. Like greit å ignorere.

#### Oversettelser til besvær

Chrome har innebygget oversetter, og den gjør jammen mye rart kan du tro. Vi har
valgt å se bort fra feil som skjer dersom translate er i bruk.

```clj
(defn google-translate-in-action? []
  (some->
   (.getElementsByTagName js/document "HTML")
   first
   .-className
   (str/includes? "translate")))
```

Det er grenser for hvor mye smerte man vil utsette seg selv for.

#### Sutring fra ResizeObserver

Nyere nettlesere har noen snedige triks, slik som ResizeObserver. Den lar
oss fikse på UI-et dersom dokumentet endrer størrelse. Det kan være nyttig for å
endre utsnitt i et kart, for eksempel.

Mindre nyttig er det å få feilmeldinger i Slack om at den ikke klarte å levere
absolutt alle eventene i en resize innenfor én requestAnimationFrame.

```clj
(def ignored-messages
  #{"ResizeObserver loop limit exceeded"
    "ResizeObserver loop completed with undelivered notifications."})
```

Disse er helt ufarlig. Ikke dumt å gjøre litt ytelsestesting på gamle klienter,
altså, men her har vi fått resizet som vi skulle, bare ikke smooooth.

## Lalala, kan ikke høre deg 🙉

Så her er feilene i frontenden vi med vilje stikker fingrene i øra for:

```clj
(defn ignore? [message url line-number col-number error]
  (or (ignored-messages message)
      (someone-elses-problem? message url line-number col-number)
      (browser-extension-exception? error)
      (google-translate-in-action?)))
```

Det gir oss rom for å sette inn innsatsen der det gjelder. Det gir oss mulighet
til å ta i mot feilene rett inn i Slack, og prioritere de med en gang. Det lar
oss sikte mot *null hull*, også i klienten. Og det er
et fint mål, syns jeg.
