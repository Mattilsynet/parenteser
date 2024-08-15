:page/title Kode som skriver kode
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-08-15T09:00:00"
:blog-post/tags [:clojure :observability]
:blog-post/description

I verktøykassa til Clojure finner vi blant annet makroer -- kode som skriver
kode. Ikke det første man bør ty til, men de kan by på elegante løsninger. Som
de gjorde for oss denne uka.

:blog-post/body

Vi bygger for tiden [et nytt system](/utviklingsstrategi/). Rett før sommeren la
vi grunnlaget for [innsikt i produksjonsmiljøet
vårt](https://www.honeycomb.io/blog/what-is-observability-key-components-best-practices).
Planen er å bruke [OpenTelemetry
tracing](https://opentelemetry.io/docs/concepts/signals/traces/) heller enn
tradisjonell logging, av flere grunner:

- De består av strukturerte data
- Gir mer kontekst enn frittstående logglinjer
- Vi får varighet på alle operasjoner
- Tracene blir nøsta, slik at vi kan se call stacken

I kode ser det sånn ut:

```clj
(defn handle-import-message [ctx message]
  (tracing/with-span ["Handle import message" message]
    (doseq [decree (create-import-decrees message)]
      (commandant/issue ctx decree))))
```

Her behandler vi en [import-melding fra en kø](/nats-import-eksport/).
`tracing/with-span` nøster rundt koden. Videre bruk av `tracing/with-span`
dypere inn i koden vil da legge seg innunder den gjeldende span-en (`"Handle
import message"`) og gi oss en fossefallsoversikt over kallene, slik [Magnar
beskrev tidligere i vår](/85-sekunder/).

Enn så lenge har vi bare plassert traces strategisk rundt inngangspunktene i
kodebasen: når vi tar imot en web request eller plukker en melding fra en kø så
starter vi med å bygge et nytt trace. Dette gir oss traces for absolutt alt
systemet vårt gjør. Sammen med noen velplasserte trace-kall ellers i kodebasen
har vi allerede ganske god oversikt over hvor tiden går, om enn noe grovkornet.

## Repetetivt arbeid

Tidligere i uka lurte vi på hvorfor enkelte importer tok så lang tid. Mer
finkornede traces ville vært til hjelp, så vi bestemte oss for å instrumentere
en haug med funksjoner. Det ble fort tydelig at dette var en nokså repetetiv
oppgave:

```clj
(defn vask-navn [navn & [senter]]
  (tracing/with-span ["vask-navn" {:senter senter}]
    ,,,))

(defn finn-senter [state {:keys [NAVN adresse]}]
  (tracing/with-span ["finn-senter" {:state state
                                     :NAVN NAVN
                                     :adresse adresse}]
    ,,,))

(defn hent-senter-adresse-ref [senter]
  (tracing/with-span ["hent-senter-adresse-ref" {:senter senter}]
    ,,,))
```

Dette er så repetetivt arbeid at det er lite egnet for menneskehender. Og det er
akkurat for tilfeller som dette -- kode som er formmessig lik -- at Clojure sine
makroer kan være til hjelp.

## Hva er en makro?

En makro er en kodesnutt som skriver kode til compileren. Makroen kjører idet
koden blir kompilert. Det er det litt vanskelig å vri hodet sitt rundt, så la
oss starte med et eksempel.

Clojure har `when`, en slags `if` uten `else`, men ingen `unless`. Så la oss
lage den:

```clj
(defmacro unless [test expr]
  `(when (not ~test)
     ~expr))
```

Vi kan bruke den som dette:

```clj
(unless (.exists (io/file "deps.edn"))
  (spit "deps.edn" "{}"))
```

Hvis `unless` var en helt vanlig funksjon, så ville `(spit "deps.edn" "{}")`
blitt evaluert på vei inn i funksjonskallet. Da ville vi med andre ord
overskrevet fila, enten den finnes eller ikke. Ikke helt ideelt.

Med makroen vår får vi heller _koden_ som argument, og kan returnere koden vi
ønsker at compileren skal jobbe videre med.

Eksempelet vårt blir dermed skrevet om til:

```clj
(when (not (.exists (io/file "deps.edn")))
  (spit "deps.edn" "{}"))
```

## Og nå: Detektimen

I en Lisp er (nesten) alt enten en funksjon eller en makro. Eksempelvis er
`defn` en makro som kombinerer `def` og `(fn [] ,,,)`. Idéen vår var å lage en
`defn` som automatisk tok seg av tracing. Etter litt diskusjon landet vi på
dette billedlige API-et:

```clj
(defn🕵️‍♂️ vask-navn [navn & [senter]]
  ,,,)

(defn🕵️‍♂️ finn-senter [state {:keys [NAVN adresse]}]
  ,,,)

(defn🕵️‍♂️ hent-senter-adresse-ref [senter]
  ,,,)
```

Altså: hvis du ønsker å trace en funksjon er det bare å "slenge på detektiven".
Vi syns det var ganske kostelig. Men hvordan ser implementasjonen ut? Hold deg
fast:

```clj
(defmacro defn🕵️‍♂️ [fn-name & forms]
  (let [[docstring arg-list & body] (if (string? (first forms))
                                      forms
                                      (cons "" forms))]
    `(defn ~fn-name
       ~docstring
       ~arg-list
       (with-span [~(str *ns* "/" fn-name)]
         ~@body))))
```

Som du kanskje kan få øye på innimellom [all
syntaksen](https://clojure-doc.org/articles/language/macros/), lager `defn🕵️‍♂️` en
helt vanlig `defn`, men med et kall til `with-span` rundt koden i funksjonen.
Særs observante lesere vil legge merke til at denne varianten ikke tracer
argumentene, slik vi gjorde manuelt lenger opp. Vel...

## Les videre på egen risiko

Vi skrev opprinnelig en variant som også tracet argumentene. Den forstod
destrukturering, og alt mulig. Vi endte opp med å droppe det, ettersom all data
allerede er tilgjengelig på den ytterste span-en i tracet. La oss ta en kikk på
hvordan det kan løses.

Først har vi denne hjelpefunksjonen:

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

Den finner alle symbolene i en vilkårlig datastruktur, som feks en
argumentliste:

```clj
(get-syms '[senter]) ;;=> (senter)
```

Den ignorerer `&`, som brukes til å samle rest-argumenter, og finner nøsta
symboler:

```clj
(get-syms '[navn & [senter]]) ;;=> (navn senter)
```

Den "forstår" destrukturering:

```clj
(get-syms '[state {:keys [NAVN adresse]}]) ;;=> (state NAVN adresse)
```

Dersom noe er strukturert, men også bundet i sin helhet med `:as` trenger vi
bare helheten:

```clj
(get-syms '[state {:keys [NAVN adresse] :as payload}]) ;;=> (state payload)
```

Ved å kalle den fra makroen vår med argumentlista til funksjonen vi definerer,
kan vi gjenskape det manuelle eksempelet til punkt og prikke:

```clj
(defn get-syms [xs]
  (->> xs
       (tree-seq coll? (fn [x]
                         (cond-> x
                           (and (map? x) (:as x))
                           (select-keys [:as]))))
       (filter symbol?)
       (remove #{'&})))

(defmacro defn🕵️‍♂️
  {:clj-kondo/lint-as 'clojure.core/defn}
  [fn-name & forms]
  (let [[docstring arg-list & body] (if (string? (first forms))
                                      forms
                                      (cons "" forms))]
    `(defn ~fn-name
       ~docstring
       ~arg-list
       (with-span [~(str *ns* "/" fn-name)
                   ~(into {} (for [sym (get-syms arg-list)]
                               [(keyword sym) sym]))]
         ~@body))))
```

Som en liten bonus har jeg også lagt på et hint til
[clj-kondo](https://github.com/clj-kondo/clj-kondo) om at bruk av `defn🕵️‍♂️` skal
forstås som bruk av helt vanlig `defn`.

Hvis du hang med helt hit, så kan vi bare gratulere. Heldigvis er dette lite
representativt for Clojure-koden vi skriver, men det er utrolig kult å kunne
gjøre ved behov.
