:page/title Et fint, lite system for skjemaer
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2025-02-18T09:00:00"
:blog-post/tags [:design :framsideutvikling :fk-is]
:blog-post/description

Bli med på en vandring gjennom systemet vårt for skjemaer i klienten. Det e'kke
så aller verst!

:blog-post/body

I bloggposten om [Førsteklasses parametere i praksis](/1ste-klasses-parametere/)
lovet jeg å komme tilbake til hvordan vi håndterer skjemaer i klienten vår. Det
er nemlig ganske stilig.

Sånn kan et skjema se ut hos oss, etter at styling er strippet bort:

```html
<form data-form-id="skjema/sjekk-adresse">
  <input type="text" name="gate/navn">
  <input type="text" name="adresse/nummer">
  <input type="text" name="adresse/bokstav">
  <input type="text" name="postnummer/nummer">
  <button type="submit">Sjekk adressen</a>
</form>
```

Som du ser er det et helt vanlig HTML-skjema, bortsett fra at det mangler
`method` og `action`. Det er fordi vi har tenkt å håndtere dette skjemaet i
klienten. Likevel er det ingen `onSubmit` på `form`-en, eller -- gud forby --
`onClick` på `button`-en. <sup>†</sup>

<small> †: Det er jo flere måter å submitte en form - slikt som å trykke
enter i et av tekstfeltene. </small>

Istedenfor har vi en `data-form-id` som plukkes opp av maskineriet. Sånn omtrent
ser den koden ut:

```clj
(defn hijack-form-submits []
  (js/document.body.addEventListener
   "submit"
   (fn [event]
     (when (some-> event .-target (.getAttribute "data-form-id"))
       (.preventDefault event)
       (submission-center/handle-form-submit (.-target event))))))
```

Det er altså én sentral event handler for alle form submits - og hvis den ser
`data-form-id`-attributtet, så håndterer den eventen selv.

Hver side har sin egen liste med skjemadefinisjoner:

```clj
{:page/forms
  [{:form/id :skjema/sjekk-adresse
    :form/fields {:gate/navn {:validations [{:kind :required}]}
                  :adresse/nummer {:validations [{:kind :required}]}
                  :adresse/bokstav {}
                  :postnummer/nummer {:validations [{:kind :required}]}}
    :form/handler #'sjekk-adresse}]}
```

Det er denne definisjonen som brukes av det sentrale maskineriet til å håndtere
eventen.

Attributtet i form-taggen ...

```html
<form data-form-id="skjema/sjekk-adresse">
```

... brukes til å finne skjemaet `:skjema/sjekk-adresse` over. 👆

Deretter finner maskineriet verdiene i input-felter i skjemaet ved å løpe over
`new FormData(form)` og bygge opp et map:

```clj
{:gate/navn "Bingeveien"
 :adresse/nummer 1
 :postnummer/nummer "3158"}
```

La du merke til at `:adresse/nummer` er et tall, ikke en streng? Det er fordi
alle attributtene blir vasket av systemet vårt for [førsteklasses
parametere](/1ste-klasses-parametere/), som sørger for at vi har full kontroll
på våre [velkjente parameternavn](/forsvar-mot-svartekunster/).

Deretter sjekkes alle `:validations`. Om noen feiler, så blir valideringsfeil
tilgjengeligjort som data for UI-koden, slik at den kan vises frem til brukeren.
Det kan se omtrent sånn ut:

```clj
[:div
 [:input {:type "text" :name "gate/navn"}]
 (when-let [error (submission/get-validation-error state :gate/navn)]
   [:p.error (:text error)])]
```

Hvis alt går bra, blir endelig `:form/handler`-funksjonen kalt. I vårt tilfelle,
`sjekk-adresse`:

```clj
(defn sjekk-adresse [_ form-data]
  [[:action/query
    {:query/name :adressevask/sjekk-adresse
     :query/kind :query/analyser-adresse
     :query/data form-data}]])
```

Vi jobber for å holde så mye kode som mulig i den funksjonelle kjernen.
<sup>‡</sup> Det inkluderer handler-funksjoner for skjemaene våre. Det betyr at
funksjonene er rene, og bare returnerer data: en liste med actions som skal
gjennomføres av maskineriet.

<small>‡: Vi må åpenbart skrive mer om arkitekturen vår (functional core,
imperative shell) på denne bloggen.</small>

I dette tilfellet blir det kjørt en query, som vil hente noe data fra serveren.
Adressen blir analysert og sammenstilt med informasjon i kartverket sine
adressedata.

Det er mange andre actions tilgjengelige også:

- `:action/execute-command` - når man vil sende endringer til serveren
- `:action/go-to-location` - navigerer til en annen side
- `:action/copy-to-clipboard` - kopierer tekst for brukeren
- ...

Det kule her er at dette er akkurat de samme actions som er tilgjengelige for
event-handlere i DOM-en, som `onClick` og `onChange`. Det føyer seg inn i
mønsteret hvor vi prøver å løse ting én gang.

### Det kule

Kort fortalt:

- Vi lener oss på helt standard forms. Det stryker nettleseren medhårs
  og gir oss god UU.

- Vi skriver skjemaer med data og rene funksjoner. Det gir oss bedre tester, og
  flytter bevegelige deler ut til maskineriet.

- Vi får full glede av førsteklasses, velkjente parametere. Det lar oss unngå
  masse konverteringskode og håndsøm for kjente greier.

- Vi bruker de samme byggeklossene som ellers i systemet. Det gjør det lettere å
  lære, og lettere å skjønne hvordan ting henger sammen.

- Vi løser problemer sentralt én gang, istedenfor mange steder rundt i koden, hver gang.

Ganske stilig.
