:page/title Når abstraksjonene sitter
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2026-07-08T07:25:24.967878"
:blog-post/tags [:clojure :data :framsideutvikling]
:blog-post/description

Gode abstraksjoner muliggjør tester som kommuniserer godt. Da blir det moro å
sikre at koden oppfører seg som den skal.

:blog-post/body

I går satt jeg og skrev tester for UI-et vårt, og fikk en gledelig overraskelse:
Testene endte opp nærmest som en visualisering av logikken. Du kan se hva jeg
prøver å få til, rett der i editoren. Det var gøy. Jeg tenker at det viser at vi
har truffet godt med abstraksjonene våre.

Vi jobber for tiden med "Mitt serveringssted", der virksomheter kan logge seg
inn og forvalte informasjonen om serveringsstedene de driver. Denne sida har
flere vertikale seksjoner som viser forskjellig informasjon, som i dette
eksempelet (som viser syntetiske data):

<a href="/images/mitt-serveringssted-1.png" target="_blank">
<img src="/images/mitt-serveringssted-1.png"
     width="300"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted med syntetiske data">
</a>

Noen av serveringsstedene mangler data til enkelte seksjoner, og når det er
tilfelle så har vi en alternativ visning som ber pent om at de gir oss mer
informasjon:

<a href="/images/mitt-serveringssted-2.png" target="_blank">
<img src="/images/mitt-serveringssted-2.png"
     width="400"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted hvor det mangler kontaktpersoner">
</a>

Problemet oppstår når vi har flere slike tomme seksjoner etter hverandre, for da
er ikke lenger den skillelinja mellom så stilig:

<a href="/images/mitt-serveringssted-3.png" target="_blank">
<img src="/images/mitt-serveringssted-3.png"
     width="400"
     style="display: block; margin: 0 auto"
     alt="Mitt serveringssted hvor det mangler kontaktpersoner">
</a>

For å tegne denne sida så ønsker vi altså for hver seksjon å enten vise
seksjonen med data, eller vise en blå boks som ber om data som mangler. Det skal
legges til en skillelinje mellom hver seksjon, men **ikke** mellom to blå
bokser.

Vanligvis bruker vi [Portfolio](https://github.com/cjohansen/portfolio)
([Storybook](https://storybook.js.org/) for Clojure) til å demonstrere/"visuelt
teste" variasjon i visningene våre, men dette er akkurat infløkt nok til at jeg
ønsket meg en god gammaldags enhetstest.

## HTML som data

Koden vår bygger HTML med Clojure sine datastrukturer, noe som kalles "hiccup".
Det ser sånn ut:

```clj
[:div
  [:h1 {:class "heading"} "Hei sveis"]
  [:p "Hvorfor dette heter hiccup er det ingen som vet"]]
```

Med denne datastrukturen kan vi lage HTML-strenger på serveren, eller bygge
levende grensesnitt på klienten.

Via [rendering-biblioteket vi bruker](https://replicant.fun) kan vi også lage
våre egne tags, med såkalte aliases:

```clj
(defn vis-serveringssted [kontekst serveringssted]
  [:div {:class :grid
         :data-gap 6}
   [:div {:class :card
          :data-pad "3-12"}
    [::nøkkelinfo kontekst serveringssted]
    [::besøksadresse kontekst serveringssted]
    ,,,]])
```

Her er `::nøkkelinfo` og `::besøksadresse` aliaser som tegner deler av
grensesnittet, og som kan representeres som data.

Og da begynner vi å nærme oss selve testen. Eller, testene, for det er to av
dem. Den første passer på at det er en skillelinje mellom hver bolk når vi har
masse data:

```clj
(testing "Viser skillelinje mellom hver hovedbolk"
  (is (= (->> (mitt-serveringssted-ui/vis-serveringssted {} testdata) ;; 1
              (lookup/select-one "[data-center=lg]")                  ;; 2
              lookup/children                                         ;; 3
              (map first))                                            ;; 4
         [::mitt-serveringssted-ui/nøkkelinfo
          :hr
          ::mitt-serveringssted-ui/besøksadresse
          :hr
          ::mitt-serveringssted-ui/åpningstider
          :hr
          ::mitt-serveringssted-ui/kontaktpersoner
          :hr
          ::mitt-serveringssted-ui/detaljer
          :hr
          ::mitt-serveringssted-ui/drift])))
```

1. Kall visningsfunksjonen med testdata som er definert over denne testen. Den
   returnerer en stor hiccup-struktur som beskriver hele siden.
2. `lookup/select-one` bruker biblioteket
   [lookup](https://github.com/cjohansen/lookup) til å kjøre CSS-selectoren
   `[data-center=lg]` på resultatet - dette gir oss `div`-en som inneholder
   seksjonene.
3. `lookup/children` henter ut alle barnenodene (som hiccup) fra `div`-en vi
   nettopp fant.
4. `(map first)` gir oss en liste med tag-navnet på hvert barn.

Resultatet er en test som på en veldig visuell måte illustrerer ønsket layout,
uten å bli sittende fast i masse flyktige detaljer om spacing, farger og andre
visuelle detaljer.

Og så til problemet jeg satte meg fôre å løse:

```clj
(testing "Dropper skillelinje mellom tomme bolker"
  (is (= (->> {:serveringssted {:serveringssted/navn "KFC"
                                :ui.serveringssted/lokasjon {}
                                :ui.serveringssted/omfattet-vurderinger [{}]}}
              (mitt-serveringssted-ui/vis-serveringssted {})
              (lookup/select-one "[data-center=lg]")
              lookup/children
              (map first))
         [::mitt-serveringssted-ui/nøkkelinfo
          :hr
          ::mitt-serveringssted-ui/besøksadresse
          :hr
          ::mitt-serveringssted-ui/manglende-informasjon
          ::mitt-serveringssted-ui/manglende-informasjon
          :hr
          ::mitt-serveringssted-ui/detaljer
          :hr
          ::mitt-serveringssted-ui/drift])))
```

Denne testen bruker ikke testdataene fra den forrige testen, men tråder inn et
serveringssted med data for bare noen av seksjonene, og forventer da å finne
noen blå bokser uten skillelinje mellom.

Hvorfor ble jeg så begeistra for disse to testene?

1. De tester overordnet struktur uten å også overspesifisere flyktige
   design-detaljer
2. De forklarer den forventa strukturen på en visuell måte
3. De er korte og konsise
4. De tester en litt vanskelig layout-utfordring
5. De jobber kun på data
6. I kraft av å være enhetstester, og ikke Storybook-eksempler eller lignende,
   kjører de i CI

I tillegg er det noe litt vakkert over å kunne skrive en så kompakt og
uttrykksfull test for visningslogikken i UI-et, som ofte er den delen av
kodebasen som har aller minst (eller ingen) testdekning.

Tester som dette, som kan fortelle oss akkurat det vi ønsker å vite, og samtidig
ignorere alle detaljene vi ikke trenger å "låse ned", vitner om at vi har
truffet med de underliggende abstraksjonene.

Når abstraksjonene sitter, blir både implementasjonen og testene enklere å lese.
Da blir det også morsommere å skrive dem.

----

For ordens skyld: Ja, dette kunne også vært løst med borders og
sibling-selectorer i CSS, men her ønsket jeg å bruke [designsystemet
vårt](https://design.mattilsynet.no/) medhårs.
