:page/title Førsteklasses 404-sider
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-03-19T09:00:00"
:blog-post/tags [:design :clojure]
:open-graph/image /images/travolta.jpg
:blog-post/description

Vi har tidligere skrevet litt om hvor mye glede vi får av velkjente
parameternavn. De fortsetter å glede, senest i går da løsningen vår fikk en
404-side.

:blog-post/body

I [«Forsvar mot svartekun... parametere!»](/forsvar-mot-svartekunster/) skrev
Magnar om velkjente parameternavn. Senere har han vist hvordan vi bruker
førsteklasses parametere i praksis i [routing](/1ste-klasses-parametere/) og
[behandling av forms](/skjemakos/). I dag skal jeg fortelle litt om hvordan
velkjente parameternavn bidro til en skikkelig kul 404-side med minimal innsats.

Som tidligere beskrevet gjør vi vasking og validering av kjente parameternavn i
ytterkantene av systemet vårt. Gitt denne side-definisjonen:

```clj
(def page
  (page-definition/define
    {:id :pages/smilefjessteder
     :route ["smilefjessteder" :region/id]
     :render #'smilefjessteder-ui/render
     ,,,}))
```

Og URL-en `http://localhost:5555/smilefjessteder/stor-oslo/` så blir
`render`-funksjonen kalt med følgende parameter-map:

```clj
{:region/id "M21000"}
```

Du lurer kanskje på hva som skjer om du ber om smilefjesstedene i en region som
ikke finnes, eksempelvis `http://localhost:5555/smilefjessteder/lille-bergen/`?
Da får vi dette mappet:

```clj
{:region/id nil}
```

Altså har grensevakten vår vasket og validert parameteret, men likefullt sendt
den ufine gjesten rett inn i stua. Resultatet er vår HTTP 500 feilside, og enda
verre: Vi får en melding på Slack om "feilen".

En ugyldig URL er ikke en feil i systemet, så her ønsker vi bare at brukeren får
en nogenlunde hjelpsom 404-side, og at systemet anser det som et hendelig uhell
det ikke er verdt å skrive hjem om.

Så hvor angriper vi problemet? Ugyldige URL-er er ikke bare et problem på
smilefjessteder-siden, så en lokal løsning er til lite hjelp. Om jeg løfter
blikket langt nok, så finner jeg denne funksjonen:

```clj
(defn receive-request [request ctx]
  (when-let [location (router/arrive the-pages/routes request)]
    (case (sjekk-tilgang request location)
      :har-tilgang
      (page-assembler/render-location request location ctx)

      :mangler-tilgang
      (render-401 request)

      :må-logge-inn
      (send-til-login request))))
```

Page assembler er vårt system for å bygge og vise sider. En `location`
inneholder data om hvilken side brukeren ønsket å se, sammen med dens
parametere. For smilefjessiden over:

```clj
{:page-id :pages/smilefjessteder
 :params {:region/id "M21000"}}
```

Her har vi all informasjon vi trenger for å avgjøre om denne `location`
tilsvarer en side som finnes. Så jeg skrev en funksjon for å validere den:

```clj
(defn gyldig-parameter? [{:keys [db]} k v]
  (case k
    :kjede/id (d/entity db [k v])
    :matrikkel.adresse/id (d/entity db [k v])
    :region/id (region-finnes? db v)
    :senter/id (d/entity db [k v])
    :tettsted/slug (tettsted/gyldig? v)
    :tur/id (d/entity db [k v])
    :ui.serveringssted/mats-id (tilsynsobjekt-id-finnes? db v)

    true ;; Andre parametere har ingen globalt definert oppførsel,
         ;; og anses alltid som gyldige
    ))

(defn gyldig-location? [state location]
  (->> (:params location)
       (every? (fn [[k v]]
                 (gyldig-parameter? state k v)))))
```

Denne snutten går gjennom alle parametere til siden, og sjekker de velkjente
parameterne våre mot databasen eller andre kilder til sannhet. Dersom en
parameter er `nil` eller har en verdi som ikke finnes i databasen, så finnes ikke
siden som `location` snakker om, og vi viser brukeren en 404-side.

Siden snutten sjekker alle kjente parametere, slipper vi å skrive kode per
side-type. Her får vi mye hjelp av globalt definerte navn og konsekvent
begrepsbruk i systemet.

## 404-siden

Innledningsvis skrøt jeg av at 404-siden vår var kul. Den har i det minste noen
kule egenskaper. Her er et eksempel:

<img src="/images/404.png" alt="404-siden: Fant ikke regionen! Kanskje du mente å se etter smilefjessteder i en av disse: [liste med klikkbare regioner]" class="img">

La meg forklare: Vi har bare én helt generell 404-side. Men siden vi har data om
hva brukeren prøvde å nå i `location`, så kan vi være litt mindre generelle:

1. Vi kan si "Fant ikke regionen", for vi vet at det er den parameteren som er
   ugyldig
2. Vi kan snakke om "smilefjessteder", for vi vet at det var den siden du ville
   se
3. Lenkene tar deg til den URL-en du forsøkte å nå, men med en av de gyldige
   verdiene for `:region/id`-parameteret

Det er ikke alle parametere hvor det gir mening å gi en liste med lenker (det er
eksempelvis tusenvis av serveringssteder), men vi kan i det minste snakke til
deg som om vi forstod hva du prøvde på, selv om du er på en helt generell
feilside.

Denne ene siden tar seg av alle forekomster av "404 not found" i hele systemet.
Alt det kostet meg var et par timers innsats, to commits og 100 linjer kode.
Velkjente parametere gjorde jobben til en lek, og som bonus fikk vi en 404-side
som faktisk hjelper brukeren videre.

Og det beste? Neste gang noen legger til en ny side, følger funksjonaliteten med
– helt gratis.
