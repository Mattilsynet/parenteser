:page/title NATS JetStream: Persistente køer og logger
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-12T09:00:00"
:blog-post/tags [:nats :koer]
:blog-post/series {:series/id :nats}
:open-graph/image /images/clj-nats.png
:blog-post/description

JetStream er en bryter som gir [NATS](https://nats.io) persistens. Det åpner opp
for en haug med spennende bruksmønstre, så som distribuert arbeidskø (tenk
RabbitMQ med flere) og strømming av persistente logger (tenk Kafka). Vi ser på
hvordan det funker.

:open-graph/description

En introduksjon til persistent meldingsutveksling i NATS med JetStream.

:blog-post/body

I [forrige innlegg om NATS](/intro-til-nats/) ble vi kjent med emnebasert
meldingsutveksling via PubSub. I dag ser vi litt på
[JetStream](https://docs.nats.io/nats-concepts/jetstream), et persistenslag som
åpner opp for en haug med interessante bruksmønstre.

JetStream er en liten bryter unna:

```sh
nats-server --jetstream
```

Med persistens kan vi opprette en "strøm". En strøm er rett og slett bare litt
konfigurasjon for ett eller flere emner som sier noe om hvordan meldingene der
skal persisteres:

```sh
nats stream add jobs \
    --subjects 'jobs.>' \
    --retention limits \
    --storage=file \
    --replicas 1 \
    --discard new \
    --max-msgs=-1 \
    --max-msgs-per-subject=-1 \
    --max-bytes=-1 \
    --max-age=-1 \
    --max-msg-size=-1 \
    --dupe-window="2m" \
    --no-allow-rollup \
    --deny-delete \
    --deny-purge
```

Dette oppretter en strøm ved navn `jobs`. Strømmen består av alle meldinger på
alle emner som starter med `jobs.`. `--subjects` kan ta én eller flere emner,
med eller uten wildcards -- men det er ikke mulig å opprette flere strømmer med
overlappende emner.

Retention policyen "limits" er kanskje det mest interessante valget her. Den
forteller NATS hvilke regler som skal gjelde for lagring og sletting av data.
"Limits" sier at data skal slettes når en av grensene er nådd. Siden vi har satt
alle grensene til en ubegrenset verdi vil NATS aldri slette data fra denne
strømmen.

## Publisering til en strøm

Siden strømmen kun inneholder konfigurasjon for hvordan NATS skal persistere
meldinger på noen emner har det ingen synlig effekt for meldingsprodusenten. Den
publiserer meldinger som før:

```sh
nats publish jobs.emails \
'{"id": "06a4915a-2437-40ea-be39-e4fe48ea239c",
  "recipient": "christian.johansen@mattilsynet.no",
  "subject": "Hallo!",
  "body": "Hei via NATS!"}'
```

## Konsummering av en strøm

Vi _kan_ også konsummere emnet som før om vi vil:

```sh
nats subscribe jobs.emails
```

Her får vi kun meldinger som publiseres etter at vi abonnerte. En av fordelene
med persistering er at vi slipper å koordinere i tid for å snakke sammen.

For å konsummere en strøm kan vi opprette en "consumer". Akkurat som strømmer er
disse persistente, og tilstanden deres bor på NATS-serveren. Og akkurat som
strømmer er litt konfigurasjon for noen emner er konsumenter litt konfigurasjon
for noen abonnenter:

```sh
nats consumer add jobs email-sender \
    --deliver all \
    --filter 'jobs.emails' \
    --pull \
    --ack none \
    --replay instant \
    --no-headers-only
```

Her oppretter vi konsumenten `email-sender` for strømmen `jobs`. `--deliver all`
sørger for at vi får alle meldinger på denne strømmen, også de som ble levert
før vi begynte å lese. Vi er kun interessert i meldingene på `jobs.emails`.
Siden vi har satt `--ack none` så vil NATS anse meldingen som behandlet av
konsumenten så fort den er levert. Dersom denne heller settes til `explicit` må
vi sende en melding tilbake til NATS når vi har lykkes å prosessere meldingen.

Vi kan nå begynne å lese meldinger fra strømmen:

```sh
$ nats consumer next jobs email-sender
[16:02:35] subj: jobs.emails / tries: 1 / cons seq: 1 / str seq: 2 / pending: 1

{"id": "06a4915a-2437-40ea-be39-e4fe48ea239c",
  "recipient": "christian.johansen@mattilsynet.no",
  "subject": "Hallo!",
  "body": "Hei via NATS!"}

Acknowledged message
```

Hvis vi gjentar øvelsen så får vi ikke samme melding -- den er nå ferdig
prosessert. Fordi det ikke er flere meldinger på strømmen så vil NATS til slutt
gi opp, og så må du forsøke på nytt til det dukker opp noe.

## Lastdeling

Dersom det er høyt volum på emnene i strømmen, og/eller jobben som skal gjøres
er tung kan det være at én eneste konsument blir for treigt. Heldigvis er du
ikke begrenset til én. Dersom du lytter på samme konsumer fra flere prosesser
vil NATS fordele lasten mellom hver av dem. Med andre ord tilsvarer en NATS
consumer en Kafka consumer group.

## En eksplosjon av muligheter

Ved å skru på de forskjellige bryterne her får du et meldingsystem for mange
forskjellige bruksmønstre, samtidig som de alle i bunn og grunn bruker samme
mekanisme: meldinger over et emne.

Ved å sette `--retention work` får du en såkalt [Work
Queue](https://docs.nats.io/nats-concepts/core-nats/queue#stream-as-a-queue).
Meldingene i en kø slettes når de er prosessert, et bruksmønster som passer godt
med bakgrunnsjobber som for eksempel utsendelse av eposter.

Ved å finjustere de forskjellige grenseverdiene i "limits" policyen kan du ha en
persistent logg som sletter data etter en gitt alder, når den totale datamengden
når et tak, eller etter et visst antall meldinger på et emne.

Når strømmen har begrenset kapasitet kan du bruke `--discard` for å si noe om
hvilke meldinger som skal slettes. `old` fjerner gammel data, som kan passe i et
system der du er interessert i historikk, men ikke ubegrenset. `new` nekter å ta
i mot nye meldinger, som kan fungere fint for en jobbkø -- da får også klienten
beskjed om at det ikke er plass til meldingen.

Det er virkelig bare fantasien som setter grenser for hva du kan bruke
NATS-strømmer til, og i neste innlegg skal vi se på et noe overraskende
bruksmønster, nemlig NATS sin key/value store.
