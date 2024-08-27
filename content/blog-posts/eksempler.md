:page/title Når eksempler ikke er nok
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-08-27T15:45:00"
:blog-post/tags [:clojure :testing]
:blog-post/description

Automatiserte tester er som regel eksempelbaserte. Men hvordan tester vi kode
der en håndfull eksempler ikke er i nærheten av å være dekkende?

:blog-post/body

Automatiserte tester er som regel eksempelbaserte: "Gitt disse argumentene så
forventer jeg denne returverdien" for en håndfull konkrete eksempler. For de
fleste funksjoner er dette godt nok.

Men hvordan tester vi kode hvor utfallsrommet er stort og testene våre stadig
løper etter nye grensetilfeller oppdaget i produksjon? En mulighet er å rett og
slett generere uendelig mange tester. Hva? Jepp!

## Et ambisiøst prosjekt

I et usedvanlig ekstremt tilfelle av "det kan da ikke være SÅ vanskelig" satte
jeg meg i fjor høst fore å skrive en [virtuell
DOM-renderer](https://github.com/cjohansen/replicant) (et alternativ til React)
for Clojure og ClojureScript. Jeg har [skrevet litt om hvordan det
funker](/virtuell-dom/) tidligere.

Dette biblioteket er nå såpass brukandes at det allerede er i produksjon
[her](https://www.adventur.no/) og [der](https://smilefjes.mattilsynet.no/).
Under utvikling har jeg fått god hjelp av en nokså [omfattende samling
tester](https://github.com/cjohansen/replicant/blob/main/test/replicant/core_test.cljc)
av den tradisjonelle sorten.

Ved hjelp av noen hjemmesnekra hjelpefunksjoner kan jeg skrive tester som
tydelig viser hva som rendres, og deretter oppsummerer hva algoritmen bestemmer
seg for å gjøre, sånn som denne:

```clj
(testing "Adds node in the middle of existing nodes"
  (is (= (-> (h/render
              [:div
               [:h1 {} "Title"]
               [:p {:replicant/key :p1} "Paragraph 1"]
               [:p {:replicant/key :p2} "Paragraph 2"]])
             (h/render
              [:div
               [:h1 {} "Title"]
               [:p {:replicant/key :p0} "Paragraph 0"]
               [:p {:replicant/key :p1} "Paragraph 1"]
               [:p {:replicant/key :p2} "Paragraph 2"]])
             h/get-mutation-log-events
             h/summarize)
         [[:create-element "p"]
          [:create-text-node "Paragraph 0"]
          [:append-child "Paragraph 0" :to "p"]
          [:insert-before [:p "Paragraph 0"] [:p "Paragraph 1"] :in "div"]])))
```

Forhåpentligvis klarer du å lese ut av denne testen at dersom du først rendrer
den øverste hiccup-en, og så den nederste, så klarer
[Replicant](https://github.com/cjohansen/replicant) å oppdatere DOM-en ved å kun
opprette den nye noden og kirurgisk plassere den på rett sted.

Enda så mye hjelp jeg har fått av disse testene så kommer det av og til
bugrapporter om spesielle sekvenser som ikke gir ønsket oppførsel. Her er den
siste jeg fikk:

```clj
(testing "Updates every other node correctly, case 1"
  (is (= (-> (h/render
              [:div
               [:div {:replicant/key "A"} "A"]
               [:div {:replicant/key "B1"} "B1"]
               [:div {:replicant/key "C"} "C"]
               [:div {:replicant/key "D1"} "D1"]])
             (h/render
              [:div
               [:div {:replicant/key "A"} "A"]
               [:div {:replicant/key "B2"} "B2"]
               [:div {:replicant/key "C"} "C"]
               [:div {:replicant/key "D2"} "D2"]
               ])
             h/get-mutation-log-events
             h/summarize)
         [[:create-element "div"]
          [:create-text-node "B2"]
          [:append-child "B2" :to "div"]
          [:insert-before [:div "B2"] [:div "B1"] :in "div"]
          [:remove-child [:div "B1"] :from "div"]
          [:create-element "div"]
          [:create-text-node "D2"]
          [:append-child "D2" :to "div"]
          [:insert-before [:div "D2"] [:div "D1"] :in "div"]
          [:remove-child [:div "D1"] :from "div"]])))
```

Før jeg løste det underliggende problemet så ønsket Replicant i det siste steget
å fjerne noden med teksten `"D2"`, altså den som nylig var satt inn.

Etter at jeg hadde lagt inn denne så tok jeg meg i å lure på hvor mange flere
sånne jeg må lage før Replicant er mer eller mindre feilfritt -- og hvor lang
tid det vil ta å finne alle sammen.

## Invarianten

Alle regresjonstestene i Replicant tester hvordan den oppfører seg gitt to
eksempler på hiccup fra en eller annen produksjonsapp. Altså er det ikke det å
skrive testene som er vanskelig, men å komme opp med eksemplene.

Hvis vi kunne uttrykke denne generelle formen på en test som en regel som alltid
skal gjelde når Replicant har gjort jobben sin så kunne vi kjørt så mange
eksempler som vi klarer å lage gjennom den for å sjekke at denne regelen --
eller invarianten -- aldri brytes.

Her hadde Magnar et knakende godt forslag: Om jeg rendrer hiccup "B" etter
hiccup "A" så skal jeg ende opp med samme DOM som om jeg bare rendrer "B".
Altså:

```clj
(let [a [:div
         [:div "A"]
         [:div "B1"]
         [:div "C"]
         [:div "D1"]]
      b [:div
         [:div "A"]
         [:div "B2"]
         [:div "C"]
         [:div "D2"]]]

;; Å rendre b etter a...
  (= (-> (h/render a)
         (h/render b)
         h/->dom)

     ;; ...skal være det samme som å kun rendre b
     (-> (h/render b)
         h/->dom)))
```

Med denne regelen i lomma er vi plutselig i stand til å teste alle eksempler.
Men hvor mange eksempler er nok? Fem? Ti? Kanskje vi må diske opp med femti for
å føle oss sikre?

## Ti tusen tester

Clojure har et bibliotek som gjør [property based
testing](https://clojure.org/guides/test_check_beginner), inspirert av
QuickCheck for Haskell. Dette biblioteket har masse fine verktøy for å generere
data av forskjellig art:

```clj
(require '[clojure.test.check.generators :as gen])

(gen/generate gen/string) ;;=> "Ãi æÇ!PA`\"ËçtQÁ"
(gen/generate gen/int) ;;=> 25
```

`gen/generate` genererer ett eksempel fra en generator. Som du ser har
test.check ansatt en QA-person til å generere strenger for seg. `gen/sample`
spytter ut en håndfull eksempler:

```clj
(gen/sample gen/int) ;;=> (0 0 -2 0 2 -3 1 2 -5 2)
```

Ved hjelp av byggeklossene i `test.check` kan vi bygge mer spissede generatorer.
Som denne, som gir oss et hiccup tag-navn:

```clj
(def gen-tag
  (gen/one-of
   (map gen/return #{:div :span :p :h1 :h2 :h3})))

(gen/generate gen-tag) ;;=> :span
(gen/sample gen-tag)   ;;=> (:h3 :h2 :h2 :span :h3 :h3 :span :h3 :p :p)
```

Med god hjelp fra [Teodor](https://play.teod.eu/) fikk jeg bygget en generator
som kan lage tilfeldig dypt nøsta hiccup:

```clj
(gen/generate gen-hiccup)

;;=>
;; [:h2
;;  {:class [:s7 :k "ES4" "e" "2" "1RS" :o :n :y :U :DZ :_ "21" :G "Zq" :? "YH9"],
;;   :title "i",
;;   :alt "",
;;   :src "L",
;;   :border -1}]

(gen/sample gen-hiccup 50)

;;=>
;;([:h1
;;  {:alt "cr", :border -2, :replicant/key {}}
;;  [:span {:replicant/key nil}]
;;  [:a
;;   {:replicant/key [1N \g]}
;;   [:span {:class [:G :s+], :alt "VR", :src "c", :border 0} "6D54Nr1FQ291"]]]
;; ,,,
;; )
```

Hele generatoren [finnes på
Github](https://github.com/cjohansen/replicant/commit/14c53311bbaba351a57e4e7f05619223d572ed77)
for spesielt interesserte.

Hvordan blir dette til ti tusen tester?

## Ti tusen tester i én

Når vi har både en invariant og en generator som lager relevante data kan vi
formulere en property og be testverktøyet vårt kjøre den:

```clj
(defspec compare-incremental-renders-to-initial-renders 10000
  (prop/for-all [a gen-hiccup
                 b gen-hiccup]
   ;; Rendering b directly, or rendering b after first rendering a
   ;; should both produce the same final DOM structure.
   (= (-> (h/render a)
          (h/render b)
          h/->dom)
      (-> (h/render b)
          h/->dom))))
```

Tallet `10000` på den øverste linja angir hvor mange eksempler jeg ønsker å
teste. Med property-baserte tester så blir du i prinsippet aldri ferdig -- du må
bare sette av en gitt mengde tid/iterasjoner og se hva du finner.

## Resultatet

Så hvordan gikk det? Replicant viste seg å være feilfri, og jeg var lykkelig i
alle mine dager. Takk for nå!

Vel, egentlig tok det ca et sekund for testen over å finne en feil... Her er
casen den genererte som ikke tilfredsstilte invarianten:

```clj
[[:h2 {:class ["I" :D "" :It], :replicant/key nil} "8M4LdoV6nIb3" "p"]
 [:h2 {:title "Y188", :alt "", :replicant/key nil} "k11aT94" "G8P" "hdAtM"]]
```

Det er ikke åpenbart ut fra dette hva som er galt. Akkurat det er ikke testen
vår så godt egna til å si noe om. For å løse denne spesifikke casen kan jeg lage
en eksempelbasert test og finne en løsning. Når det er gjort så kan jeg kjøre
property-testen på nytt for å se om det dukker opp nye feil.

Alle generatorene i test.check lager verdier som er reduserbare. Dermed kan den
faktisk finne det minimale eksempelet som er nok til å reprodusere feilen. Og
her er det:

```clj
[[:h2 {} "0" "0"]
 [:h2 {} "1"]]
```

Dette er altså nok til å sette en tilsynelatende grundig gjennomtenkt algoritme
ut av spill. Man blir ydmyk av mindre.
