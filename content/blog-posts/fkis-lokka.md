:page/title Prosedyrale greier i et funkis-bygg
:blog-post/author {:person/id :person/sigmund}
:blog-post/published #time/ldt "2026-08-26T09:00:00"
:blog-post/tags [:fk-is :tdd]
:open-graph/image "/images/funkis-hus.jpg"
:blog-post/description

Skal du bygge et hus, er det lurt å lage taket etter at grunnmuren er på plass.
Så hvordan løser vi sånne prosedyrale greier når vi bor i et FunKIS-hus?

:blog-post/body

Vi har tatt over arkivering av dokumentene for smilefjestilsyn. Dette innebærer
å gjøre en rekke sekvensielle API-kall. De som lagde API-et, har ikke hørt om
idempotens, så det gjelder å ikke drite seg ut. Vi bæsja på leggen en del ganger
før vi kom helt i mål.

Marta, klienten som brukes til tilsynene, har en nesten like chatty protokoll.
Så da vi begynte, tenkte vi at vi skulle direkte oversette disse til API-kall
mot arkivet. For å støtte nedetid hos arkivet, ble arbeidet som skulle gjøres
lagt i en kø med fingranulerte oppgaver. Noen ganger gikk det tilsynelatende
dårlig, selv om arkivet hadde klart å lagre ting, så da ble det en rekke
duplikate dokumenter i noen saker.

Vi fikk stadig behov for mer avansert arbeidskøhåndtering, så vi måtte krype til
korset og legge vår hjemmesnekrede trehytte av en arbeidskø på hylla. Det var jo
egentlig ikke noe krav at vi skulle ha så fingranulerte oppgaver, bare vi hadde
fått det vi trengte fra klienten og gjorde API-kallene sekvensielt.

<img class="img" style="max-width: 500px; margin: 0 auto; display: block"
src="/images/funkis-hus.jpg"> _Vi ville at arkiveringen skulle få bo med oss i
et fint funkis-hus, ikke en falleferdig trehytte. Bilde: Frode Inge Helland, [CC
BY-SA 3.0](http://creativecommons.org/licenses/by-sa/3.0/) via [Wikimedia
Commons](https://commons.wikimedia.org/wiki/File:GriegLouEide02.JPG)_

## Fønky løkker

I Matnyttig har vi en byggekloss, *fcis-loop*, som lar oss bygge opp sånne
sekvensielle greier som en
[tilstandsmaskin](https://en.wikipedia.org/wiki/Finite-state_machine). Dette er
bygget opp av tre deler:

1. En sentral funksjon som kjører løkka
2. En funksjon som tar imot en tilstand og gir ut et resultat
   * Dette kan inneholde en beskrivelse av steg for å oppdatere tilstanden
3. En mapping til funksjoner fra steg-beskrivelsene

Hva er vitsen med å bygge opp en tilstandsmaskin på denne måten istedenfor å
bare lage en funksjon som gjør alle kallene i sekvens? For det første gir det et
skille mellom en funksjonell del, som ser på tilstanden og bestemmer hva planen
videre er. All imperativ kode ligger i egne funksjoner som kjøres basert på den
planen som blir beskrevet. Da kan vi enkelt teste at vi tar riktig valg for
veien videre, uten at vi trenger å bygge masse mocks av eksterne systemer.

I vårt tilfelle, gjør det det også veldig enkelt å fortsette arkiveringen fra
der vi slapp om noe skulle gå galt underveis. Vi kan avbryte løkka, og neste
gang vi plukker oppgaven fra arbeidskøen, kan vi gjenopprette tilstanden.

Arkivering av en rapport består av en rekke steg:

1. Start løkka med kun saksbehandlingen i tilstanden
2. Hent data fra arkivet, `arkivsak`, og dokumenter vi skal laste opp
3. Lag en *journalpost* med rapporten om den ikke finnes fra før
4. Last opp smilefjesplakaten til journalposten
5. Journalfør journalposten (sendes til serveringsstedet og lukkes)
6. Hvis de har fått smil, er saken avsluttet. Marker så eventuelle svar fra
serveringsstedet, *restanser* som behandlet (avskriv dem)
7. Lukk saken i arkivet om vi er ferdig med den.

Avskrivning av restanser er egentlig flere mindre steg, for å holde rede på
hvilke restanser vi har avskrevet allerede. Dette gjør vi bare for å slippe å
avskrive de samme restansene på nytt om vi må plukke opp oppgaven igjen.

Siden vi jobber test-drevet, begynte vi med å skrive tester. Da var det naturlig
å se på første steg i hele prosessen når vi bare får inn saksbehandlingen fra
databasen:

```clojure
(testing "Henter sak fra arkivet, samt rapport og plakat fra NATS som første steg"
  (is (= (arkiv/arkiver-saksbehandling
           {:saksbehandling (hent-saksbehandling-smil)})
         {:fcis/requests
          {:arkivsak [:arkiv/hent-arkivsak {:saksnummer (:saksnummer 📦/tilsynsløp)}]
           :rapport [:nats.kv/get "arkiv-fildata" (:smilefjesrapport 📦/inspeksjon)]
           :plakat [:nats.kv/get "arkiv-fildata" (:smilefjesplakat 📦/inspeksjon)]}})))
```

Her kan du også se hvordan en plan ser ut. Vi har tre imperative funksjoner som
skal oppdatere tilstanden. Vi må hente saken fra arkivet og legge den i
`:arkivsak` på tilstanden, så må vi hente rapporten og plakaten fra NATS. Da har
vi nok tilstand til å gå videre til neste steg:

```clojure
(testing "Oppretter journalpost når den ikke finnes"
  (is (= (arkiv/arkiver-saksbehandling
          {:saksbehandling (hent-saksbehandling-smil)
           :arkivsak {:body {}}
           :rapport {:tittel "Mattilsynet avslutter saken"
                     :fildata "..."}})
         {:fcis/requests
          {:journalpost
           [:arkiv/opprett-journalpost
            {:saksnummer (:saksnummer 📦/tilsynsløp)
             :mottaker {:adresse {:postnummer "0666"}
                        :brreg.enhet/navn "Enhet AS"
                        :brreg.enhet/orgnummer (-> 📦/restauranten :entiteter :drift :orgnummer)}
             :tidspunkt "2022-08-19T00:00:00Z"
             :tittel "Mattilsynet avslutter saken"
             :fildata "..."
             :saksbehandler {:ansatt/nummer (:ansatt/nummer 📦/sigmund)
                             :enhet "seksjons-id"}}]}})))
```

Og sånn kan vi teste hvert eneste lille steg i prosessen. Det gjør det lett å
bygge opp flyten gradvis, og lett å justere når vi tester det mot arkivet på
ordentlig og har driti på draget i noen antakelser. Vi endte opp med 25 tester
for den ene funksjonen, og føler vi har ganske god kontroll på at vi ikke gjør
altfor mye feil i denne prosessen. Den funksjonen vi endte opp med for
planleggingen, ser slik ut:

```clojure
(defn arkiver-saksbehandling [{:keys [saksbehandling arkivsak rapport plakat
                                      journalføring avskrivning restanser]
                               :as data}]
  (let [journalpost (or (-> data :journalpost :body)
                        (finn-tilhørende-journalpost data))
        vedlegg (or (:vedlegg data)
                    (finn-tilhørende-vedlegg journalpost plakat))]
    (cond
      (some (comp #{:arkivering.forespørsel/mislykket} :status) (vals data))
      {:status :arkivering.status/feilet}

      (contains? data :avslutning)
      {:status :arkivering.status/fullført}

      (not arkivsak)
      (be-om-data saksbehandling)

      (not journalpost)
      (opprett-journalpost saksbehandling rapport)

      avskrivning
      (marker-restanse-avskrevet saksbehandling restanser (:id avskrivning))

      (mangler-restanser-i-nats? data)
      (legg-nye-restanser-på-nats saksbehandling arkivsak restanser)

      (alle-restanser-avskrevet? data)
      (avslutt-sak saksbehandling)

      restanser
      (avskriv-neste-restanse restanser)

      (journalført? journalføring journalpost)
      (if (= :smilefjes/smil (:smilefjesvurdering/smilefjes saksbehandling))
        (hent-restanser saksbehandling)
        {:status :arkivering.status/fullført})

      (or vedlegg (trenger-ikke-plakat? data))
      (journalfør-journalpost saksbehandling journalpost)

      journalpost
      (legg-ved-plakat journalpost plakat))))
```

Den er riktignok delt opp i mange små funksjoner for å avgjøre hvilket steg som
er riktig, og funksjoner for å spytte ut selve planen. Husk at `cond` er som en
lang rekke `else if`s, så den må mer eller mindre leses baklengs for å matche
lista med steg vi så tidligere.
