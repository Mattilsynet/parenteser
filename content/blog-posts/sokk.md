:page/title Har du røyka sokka dine?
:blog-post/author {:person/id :person/sigmund}
:blog-post/published #time/ldt "2025-12-02T10:00:00"
:blog-post/tags [:testing]
:open-graph/image "/images/scarface.jpg"
:blog-post/description

En fredag ettermiddag tryna en test som ikke hadde noe med endringene våre å
gjøre. Forrige commit tryna også, selv om den passerte i CI…

:blog-post/body

En fredag ettermiddag kjørte vi testene før vi skulle sende inn endringene våre.
En test tryna, men den så ikke ut som den hadde noe med vår kode å gjøre. Vi
kjørte den med tidsmaskinen, og den snubla utfor stupet igjen. I CI hadde den
passert med den koden… Vi sendte inn endringene våre. De var ganske uskyldige,
og jeg måtte gå. Vi tok sjansen på at det var noe lokalt hos meg. 🏃Commit and
run, baby!

## Testen som røyka sokk

Nå gikk testen på snørra i CI. Nesa begravd i en snøfonn. Den passerte hos noen,
men ikke hos andre. Testen var like upålitelig som en svartbjørn med kilovis av
kokain innabords.

```clojure
(testing "Sender e-post til inspektøren når adressen allerede har et aktivt lokale"
  (is (= (->> (commands/plan (📦/lag-state [📦/christian 📦/humbug-cafe 📦/mandelhuset])
           {:command/kind :commands.serveringssted/oppdater-håndskrevet-adresse
            :command/mastermind [:bruker/id (:bruker/id 📦/christian)]
            :command/data
            {:mats/id (:mats-id 📦/humbug-cafe)
             :håndskrevet-adresse/gateadresse "Heiavegen 38"
             :håndskrevet-adresse/postnummer "5685"
             :håndskrevet-adresse/poststed "Tysnes"}})
           :plan/effects
           (filter (comp #{:effects.email/send} :effect/kind))
           first
           :effect/data)
         {:email/subject "Adresse-oppdatering på Humbug Café"
          :email/body [:div
                       [:h1 "Hei Christian"]
                       [:p (str "Vi fikk problemer med adressen du la inn på Humbug Café. "
                                "Du kan løse problemet ved å gå til
                                 adressevask for Café Humbug.")]]
          :email/to ["mister.inspektør@mattilsynet.no"]})))
```

Den skulle sjekke at et serveringssted som flyttet, ikke tok over noen andres
lokaler uten videre. Noen ganger ble varselet om at lokalet var i bruk sendt,
men tydeligvis ikke alltid. Hvordan hadde vi klart å få til noe sånt med en test
som var en ren funksjon? Dataene som kom inn, måtte jo være forskjellig mellom
hver gang.

Den eneste databasen i testene, som ikke ble bygd opp fra bunnen i hver test,
var adressedatabasen. Hadde vi noen endringer av den i noen tester? Jo, da, men
det ble bare lagt inn noen nye tulleadresser, så ingenting som skulle påvirke
denne testen.

Kanskje vi gjorde noen endringer som vi ikke fant i søket. Da burde det være
avhengig av rekkefølgen testene kjører i. Flaks for oss at Kaocha skriver ut
hvilket tall den bruker for sin stokking av testene, så vi kan kjøre testene på
nytt med samme grunnlag. Bare synd at narkotikamisbruken til denne testen var
enda verre enn vi hadde fryktet. Rekkefølgen på testene påvirket ingenting; den
var fortsatt like ustabil.

Lagre planen på fil! Ja, der fikk den adresse uten videre. Jeg så ingenting rart
med resultatet. Vel, ikke utover at det skulle ha gitt en e-post om at lokalet
var opptatt. Jeg gravde en del i koden, før jeg bestemte meg for å se nærmere på
resultatet en gang til. Jeg tok like gjerne med info om hva vi dyttet inn i
databasen i denne testen. Og der stod det og lyste meg rett i ansiktet som et
neon-skilt:

```clojure
{:matrikkel.adresse/id "192607886" ...}
{:matrikkel.adresse/id "502820314" ...}
```

Serveringsstedet som hadde flyttet, hadde fått en annen adresse en tiltenkt. Det
viste seg at denne adressen var "Heiavegen 38A", mens vi skulle flytte den inn i
"Heiavegen 38". Det var bare det at i testene, manglet bokstaven, så vi hadde to
"Heiavegen 38". Datomic gir ikke garantier om rekkefølgen på resultatene i en
spørring, så det var helt naturlig at vi fikk en tilfeldig adresse av disse to.
Et klassisk "dritt inn, dritt ut"-problem, men her fikk jeg noen skikkelige
flashbacks til upålitelige tester i imperative systemer.

Og til slutt en liten etterpåklokskap: se etter enkle feil før du kaster bort
flere timer på graving etter store feil. Lett å si, hakket vanskeligere å gjøre,
så det kan ikke sies ofte nok.

## PS

Visste du at "flaky", som vi gjerne bruker om upålitelige tester, kommer fra at
kokain ble kalt "flake" på 1920-tallet? Oppførselen til kokain-misbrukere, ble
etter hvert beskrevet som "flaky".
