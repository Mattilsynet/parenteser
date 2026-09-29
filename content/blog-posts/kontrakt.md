:page/title Kan vi teste kontrakter uten integrasjonstester?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2026-09-29T09:07:22.020504"
:blog-post/tags [:clojure :data :fk-is :arkitektur]
:blog-post/description

Klienten sier én ting, men serveren vil høre noe annet. Kanskje den mest
klassiske feilsituasjonen en web-app kan befinne seg i. Kan data bøte på
problemet?

:blog-post/body

Nylig oppdaget jeg en helt klassisk feil i systemet vårt. Jeg fullførte et løp i
UI-et vårt, men fikk til slutt fingeren av serveren. Hvorfor det? Alle
byggeklossene var riktige hver for seg, men UI-et og serveren hadde forskjellige
oppfatninger av kontrakten.

UI-et vårt ber om endringer i systemet ved å sende
[kommandoer](/handfaste-konsepter/), sånn som dette:

```clj
{:command/kind :commands.serveringssted/registrer
 :command/data
 {:serveringssted/navn "Burgeriet"
  :serveringssted/produksjonsformer [:produksjonsformer/restaurant-kafe]
  :enhet {:brreg.enhet/orgnummer "999000111"}}}
```

Problemet var bare at vi har ombestemt oss. Kommandoen heter ikke lenger
"registrer", den heter "opprett".

## Automatiserte tester

Hvis du vil at systemet ditt skal bli bedre over tid så burde en sånn feil føre
til litt refleksjon. Hvordan kan vi unngå at lignende feil oppstår igjen?

Denne feilen oppstod fordi klienten og serveren ikke er samkjørte om kontrakten.
En måte å unngå det på er å skrive en integrasjonstest. Men integrasjonstester
mellom en tykk klient og en kjørende server er kostbare (les: treige) greier.
Kompliserte og snublete er de også.

Når alt er data, og nesten all koden kjører like greit på serveren som på
klienten så kan vi skrive en test som har mesteparten av verdien til en
integrasjonstest men til kostnaden av en enhetstest.

Jeg har [vist noe lignende før](/gode-abstraksjoner/), men denne gangen er både
klienten og serveren i spill.

Den aktuelle flyten spør brukeren om navn og virke for et nytt serveringssted og
oppretter det. La oss se på den aller første testen:

```clj
(testing "Starter med å be om navn på serveringsstedet"
  (is (= (->> (nytt-serveringssted-ui/render {})
              (lookup/select-one :h1)
              lookup/text)
         "Hva heter serveringsstedet?")))
```

Denne testen kaller render-funksjonen til den aktuelle siden. Den returnerer en
data-struktur som representerer HTML. Det ser sånn ut:

```clj
[:body
  [:h1 "Halla!"]
  [:a {:href "/"} "Til forsiden"]]
```

[lookup](https://github.com/cjohansen/lookup) er et bibliotek som implementerer
CSS-selectors og andre støtteverktøy for denne datastrukturen. Så testen rendrer
sida og forventer at den starter med å stille spørsmålet "Hva heter
serveringsstedet?".

I HTML-en på denne siden er det et `form`, det ser omtrent sånn ut:

```clj
[:form {:class "grid"
        :data-gap "5"
        :on {:submit [[:skjema/oppdater-utfylling ,,,]]}}
 [:ds-field {:class "field"}
  [:input {:class "input"
           :type "text"
           :name "navn"
           :on {:blur [[:skjema/oppdater-utfylling ,,,]]}}]]
 [:button
  {:class "button"
   :data-variant "primary"
   :type "submit"}
  "Neste"]]
```

Inne i HTML-en er selv event-handlerne representert som data, vi kaller dem
"actions". Når de er data så kan vi velge hvilke vi prosesserer i en test.
Eksempelvis kan vi gjennomføre de som kun oppdaterer lokal tilstand, men
ignorere de som sender HTTP-requester til serveren.

På toppen av dette har vi laget noen hjelpefunksjoner som lar oss simulere at vi
interagerer med siden. Så testen som tar oss til neste spørsmål ser sånn ut:

```clj
(testing "Spørger så, hva driver dere med?"
  (is (= (->> (hh/interact nytt-serveringssted-ui/render
                [(hh/input-text :fritekst/verdi "Burgeriet")
                 hh/submit-form])
              (lookup/select-one :h1)
              lookup/text)
         "Hva driver Burgeriet med?")))
```

`hh/interact` er en hjelpefunksjon som tar en render-funksjon og en serie med
interaksjoner. Ut kommer HTML-en fra render-funksjonen etter siste utførte
interaksjon. Nå forventer vi at sida spør "Hva driver Burgeriet med?".

Dette er vel og bra, tenker du kanskje. Men det tester jo bare renderinga på
klienten? Det var ikke der feilen oppstod!

Om vi simulerer nok interaksjon, så vil en event-handler til slutt forsøke å
gjennomføre en action som ser sånn ut:

```clj
[:actions/execute-command
 {:command/kind :commands.serveringssted/opprett
  :command/data
  {:serveringssted/navn "Burgeriet"
   :serveringssted/produksjonsformer [:produksjonsformer/restaurant-kafe]
   :enhet {:brreg.enhet/orgnummer "999000111"}}}]
```

Altså, kjør denne kommandoen. For å unngå at enhetstestene våre går på internett
er `:actions/execute-command` noop når testene kjører. Heldigvis kjører denne
testen på serveren, så vi kan rett og slett plukke ut kommandoen og kjøre den
gjennom kommando-systemet:

```clj
(testing "Oppretter serveringssted på serveren"
  (is (= (let [kommando
               (-> {:location {:params
                               {:brreg.enhet/orgnummer "999000111"}}}
                   (hh/interact vis-side
                     [(hh/input-text :fritekst/verdi "Burgeriet")
                      hh/submit-form
                      (hh/click-button "restaurant/kafé")])
                   hent-siste-kommando)]
           (-> (📦kommando/kjør (📦/bruk-ctx [📦/ragna]) kommando)
               hent-serveringssted))

         {:serveringssted/navn "Burgeriet"
          :serveringssted/produksjonsformer #{:produksjonsformer/restaurant-kafe}
          :serveringssted/drift
          {:drift/enhet
           {:brreg.enhet/orgnummer "999000111"}}})))
````

Ok, la oss se hva som skjer:

1. Vi simulerer at vi kom inn på sida med et gitt orgnummer i URL-en med
   `{:location {:params {:brreg.enhet/orgnummer "999000111"}}}`
2. Vi simulerer å oppgi at serveringsstedet heter "Burgeriet" og er en
   restaurant.
3. Vi bruker støttefunksjonen `hent-siste-kommando` til å se gjennom alle
   actions som er utstedt og finne den siste som forsøkte å kjøre en kommando.
4. Så bruker vi en test-tilpasset versjon av kommandosystemet vårt til å kjøre
   den kommandoen - mot en helt ekte (in-memory) database.
5. Til slutt henter vi ut det nye stedet med en hjelpefunksjon, og sjekker at
   den har fått verdiene som kom fra UI-et.

Første gang jeg kjørte denne testen fikk jeg beskjed om at kommandoen
`:commands.serveringssted/registrer` ikke finnes. Så da endra jeg den til
`:commands.serveringssted/opprett`, testen passerte, og den opprinnelige feilen
var løst.

Denne testen kjører på noen få millisekunder, uten server og uten nettleser.
Likevel verifiserer den at klienten og serveren fortsatt er enige om kontrakten
mellom seg. Det er vanskelig å få til når kontrakten bare eksisterer implisitt i
kode.
