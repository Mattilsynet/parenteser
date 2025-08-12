:page/title Plukk opp såpa!
:blog-post/author {:person/id :person/sigmund}
:blog-post/published #time/ldt "2025-08-12T11:00:00"
:blog-post/tags [:clojure :xml]
:blog-post/description

SOAP-tjenester lever i beste velgående. Innimellom må man plukke opp SOAP igjen,
men hva gjør man når man ikke har en masse kodegenerering fra WSDLer?

:blog-post/body

Den siste tida har jeg jobbet litt med å få oppdaterte adresser fra Kartverket
inn i systemet vårt. Det åpnes restauranter rundt om i det ganske land, og noen
finner til og med på å skaffe seg lokaler på adresser som ikke fantes da vi
startet prosjektet. Heldigvis har Kartverket en tjeneste for å få vite om
endringer i adresser: nye veier, nye bygninger, endrede postnummere eller hva
det nå måtte være. Denne tjenesten er riktignok en SOAP-tjeneste. Det finnes
fortsatt en del sånne der ute.

I Clojure pleier vi ikke å bruke en haug med generert kode, sånn som man gjerne
gjør i Java. Og har du jobbet med SOAP i Java, har du sikkert genereret en masse
klasser med JAX-WS basert på WSDLen til tjenesten.

## XML

SOAP er bare XML-forespørsler med XML-svar over HTTP. Så da må vi kunne skrive
og lese XML. Til det kan vi bruke `clojure/data.xml`-biblioteket, som bruker
`javax.xml` under panseret. Hvert element blir en datastruktur som ser omtrent
slik ut:

```clojure
{:tag :xmlns.http%3A%2F%2Fexample.com%2F/Example
 :attrs {:attribute "Value"}
 :content (...)}
```

Dette er jo vel og bra, men vi må fiske ut taggen for å kunne plukke ut det vi
ser etter, så vi kan ikke bare bruke vanlige map-funksjoner som `get-in`, men
det er jo heller ikke så rart når det kan finnes mange søsken-noder i et
XML-tre.

Biblioteket kan lage XML for oss fra Hiccup-syntaks:

```clojure
(xml/sexp-as-element
  [::example/Example
   {:attribute "Value"}
   [::example/Child "This is the best XML ever!"]])
```

```xml
<example:Example attribue="Value">
  <example:Child>
    This is the best XML ever!
  </example:Child>
</example:Example>
```

Det er dette vi bruker for å bygge opp forespørslene våre. Da har vi bare lagd
noen funksjoner for å bygge opp litt Hiccup som vi sender over i en POST til
Kartverkets Matrikkel-API.

Det kommer masse ting vi ikke er så fryktelig interessert i tilbake i
SOAP-svarene, så det kan være greit å lage noen hjelpefunksjoner for å få de
dataene vi er interessert i. Jeg vil gjerne ha en funksjon som gjør omtrent det
samme som `get-in`. Med `(def my-nested-map {:a {:b {:c "Wahoo"}}})` trekker
`(get-in my-nested-map [:a :b :c])` ut "Wahoo" fra den nøstede strukturen.

```clojure
(defn children [node]
  (let [content (:content node)]
    (if (= 1 (count content))
      (first content)
      content)))

(defn get-in-xml* [xml path]
  (loop [node xml
         ks (seq path)]
    (cond (not ks) node

          (= (:tag node) (first ks))
          (recur (children node) (next ks))

          (seq? node)
          (->> (filter #(= (:tag %) (first ks)) node)
               (map #(get-in-xml* % ks))))))

(defn get-in-xml [xml path]
  (let [res (get-in-xml* xml path)]
    (if (sequential? res)
      (flatten res)
      res)))
```

`children` henter ut ett eller flere elementer som er barn av et XML-element.
`get-in-xml` kan ta inn ett eller flere elementer og en sti for hva vi ser
etter. `get-in-xml*` gjør jobben, men kan returnere nøstede lister eller ett
enkelt elements innhold. Vi vil gjerne slippe å forholde oss til nøstede lister,
så vi flater ut strukturen til en vanlig liste med denne wrapperen.

Hvis stien `get-in-xml` får, er tom, har vi funnet det vi leter etter. Hvis
taggen til noden er lik den første taggen i stien, fortsetter vi å søke i barna
med resten av stien. Og hvis vi har fått inn en liste med elementer, kjører vi
søket på alle elementene i lista. Med denne lille snutten kan vi da gjøre ting
som:

```clojure
(-> (xml/sexp-as-element
      [::example/an-element
        [::example/another-element
          [::example/a-third-element
            "Test 123"]
           [::example/a-third-element
            "Test 456"]]
          [::example/another-element
           [::example/a-third-element
            "Test 789"]]])
    (xh/get-in-xml [::example/an-element
                    ::example/another-element
                    ::example/a-third-element]))
;; ["Test 123" "Test 456" "Test 789"]
```

Vi har også et par andre støtte-funksjoner som en variant av `select`, som lar
oss plukke ut flere barne-elementer og forkaste alle andre. I tillegg har vi en
sak for å hente ut XSI-typen og mappe om XML-navnerommet. XSI-typer er angitt i
strenger, så de har ikke en innebygd mapping av navnerommet i `data.xml`.

## Kna datadeigen

Så vil vi jo gjerne gjøre om all denne såpa til noe fin JSON som alle i
Mattilsynet kan få spise. Det gir oss noe å bruke disse funksjonene til noe.
Fordi vi skal spise mye forskjellig mat fra Kartverket, er det greit å ha en
liten hjelpefunksjon for det òg. Så `pakk-ut-entitet` tar inn XML, en mapping
fra XML-tagger til nye navn og eventuelle funksjoner som skal forandre verdiene
på en eller annen måte:

```clojure
(defn pakk-ut-entitet [xml tag-name-mappings tag-transforms]
  (let [shaved (-> (xh/get-in-xml xml [::dom/item])
                   (xh/select-tags (keys tag-name-mappings)))
        transformed (reduce (fn [updated [tag update-fn]]
                              (if-let [val (update-fn (get updated tag))]
                                (assoc updated tag val)
                                (dissoc updated tag)))
                            shaved tag-transforms)]
    (set/rename-keys transformed tag-name-mappings)))
```

For hver type entitet, har vi en liten funksjon for å pakke ut og kna svaret
litt. For å mørne opp fylkene gjør vi for eksempel sånn:

```clojure
(defn pakk-ut-fylke [xml-fylke]
  (pakk-ut-entitet xml-fylke {::dom/id :id
                              ::dom/versjon :versjon
                              ::kommune/fylkesnummer :nummer
                              ::kommune/fylkesnavn :navn
                              ::kommune/gyldigTilDato :gyldigTil
                              ::kommune/nyFylkeId :nyId}
                   {::dom/id pakk-ut-id
                    ::kommune/fylkesnavn normaliser-stedsnavn
                    ::kommune/nyFylkeId pakk-ut-id
                    ::kommune/gyldigTilDato #(xh/get-in-xml % [::dom/date])}))
```

Da får vi bare ut ID, versjon, fylkesnummer, navn, utløpsdato og ny ID for
sammenslåtte fylker. IDene har en hjelpefunksjon for å pakke ut IDer, som er litt
ekstra innpakket, som tall. Det likner på hvordan datoen pakkes ut.
Normalisering av stedsnavn gjør at `MØRE OG ROMSDAL` blir til `Møre og Romsdal`.

Før vi pakker ut fylket, er det som nevnt noen greier vi ikke er så fryktelig
interessert i:

```xml
<item xsi:type="ns15:Fylke" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <id xsi:type="ns15:FylkeId">
      <value>15</value>
   </id>
   <metadata>
      <item>avsluttetAv</item>
      <item>uuid</item>
      <item>nyFylkeId</item>
      <item>sluttdato</item>
      <item>versjonId</item>
      <item>id</item>
      <item>gyldigTilDato</item>
      <item>versjon</item>
      <item>fylkesnummer</item>
      <item>oppdateringsdato</item>
      <item>oppdatertAv</item>
      <item>fylkesnavn</item>
      <item>kommuneIds</item>
   </metadata>
   <oppdateringsdato>
      <timestamp>2020-06-17T20:30:11.727000000+02:00</timestamp>
   </oppdateringsdato>
   <versjonId>2</versjonId>
   <oppdatertAv>smatmynd</oppdatertAv>
   <versjon>1592418611727</versjon>
   <ns15:fylkesnummer>15</ns15:fylkesnummer>
   <ns15:fylkesnavn>MØRE OG ROMSDAL</ns15:fylkesnavn>
   <ns15:kommuneIds>
      <ns15:item>
         <value>1539</value>
      </ns15:item>
      <ns15:item>
         <value>1543</value>
      </ns15:item>
      <ns15:item>
         <value>1545</value>
      </ns15:item>

      <!-- Det er nesten 50 kommuner i Møre og Romsdal, så *snipp snipp*
           Kommuner henter vi uansett ut i en egen forespørsel, og de lenker til
           sitt fylke, så vi trenger egentlig ikke denne mila med kommunenummere.
       -->

   </ns15:kommuneIds>
   <ns15:uuid>
      <navnerom>https://data.geonorge.no/matrikkel</navnerom>
      <uuid>cd34b1ea-5545-5dd5-a009-9391a8b99ff5</uuid>
   </ns15:uuid>
</item>
```

Når vi har pakket ut denne, blir den bitte litt mindre:

```json
{"id": 15,
 "versjon": "1592418611727",
 "nummer": "15",
 "navn": "Møre og Romsdal"}
```

## Finn på en snerten konklusjonstittel'a!

Jeg synes vi fikk til noen ganske ergonomiske greier med denne XML-parsinga.
`clojure/data.xml` står for mye av jobben, men med funksjoner som likner de vi
bruker til standard Clojure-datastrukturer, ble det hakket enklere å jobbe med.
