:page/title Bokstavpønt til bekymring
:blog-post/published #time/ldt "2023-12-19T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:sok :unicode :java :javascript]
:blog-post/description

Unicode er overalt. Men noen steder er det fortsatt tryggest med det gode gamle
ascii-tegnsettet, og i dette innlegget skal vi se på Unicode-normalisering som
en vei dit.

:open-graph/description

Unicode-normalisering kan hjelpe deg med å gjøre tekst URL-vennlig, søkbart og
mer. Lær om hvorfor og hvordan.

:blog-post/body

Jeg har nylig skrevet [litt om søk](/fulltekstsok/), og hvordan man kan få til å
[treffe på omtrent det brukeren skrev](/sok-vekting/). En ting jeg ikke har
dekket er hvordan man kan hjelpe folk å finne "el niño" når de skriver "el
nino", eller "kafé" når de skriver "kafe".

Tegn som ñ og é kan være litt kronglete å jobbe med fordi ingen av dem er ett
tegn - det finnes flere Unicode-former som gir samme visuelle resultat. ñ kan
for eksempel være en av disse to:

- **Latin Small Letter N with Tilde (ñ)**, Unicode code point `U+00F1`
- En kombinasjon av `U+006E` (**n**) og `U+0303` (**"Combining Tilde"**)

Heldigvis finnes det en løsning på miserien: Unicode-normalisering.

Både Java og JavaScript har innebygget verktøy for å normalisere Unicode til en
gitt form. Det finnes flere former, men den som hjelper oss med ornamenterte
bokstaver er `NFD` - Normalization Form Decomposition. Denne formen bryter alle
sammensatte tegn ned i en base-karakter og kombinerende "diakritiske tegn" (så
som tilde og aksent).

Strenger kan normaliseres på følgende vis i Java:

```java
import java.text.Normalizer;

String s1 = "El niño";
String s2 = Normalizer.normalize(s1, Normalizer.Form.NFD);
```

Tilsvarende i JavaScript:

```js
var s1 = "El niño";
var s2 = s1.normalize("NFD")
```

Du kan gjerne printe ut resultatet, men du vil bli skuffet, for det er ingen
synlig forskjell. Normaliseringen endrer kun på hvordan innholdet er
representert. Det kan være at du ser forskjell på `.length` - litt avhengig av
hvilken representasjon du startet med. Etter normaliseringen vil ihvertfall
`.length` være 8 - som indikerer at tilden er skilt ut fra n-en.

Når dette er gjort er det bare å rydde unna kodepunktene fra `U+0300` til
`U+036F`:

```javascript
var s = "El niño";
s.normalize("NFD").replace(/[\u0300-\u036F]/, "");
//=> "El nino"
```

Og der har du det! Et kjekt lite triks du kan bruke neste gang du skal lage en
"slug" til en URL, søke bredt, eller gjøre andre former for tekstanalyse.
