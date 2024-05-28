:page/title Køer i praksis: Flytting av data
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-05-28T09:00:00"
:blog-post/tags [:nats :clojure]
:open-graph/image /images/nats.png
:blog-post/description

Jeg har skrevet mye om hva [NATS](/nats/) er og hvordan det funker. Det er på
høy tid å si noe om det viktigste: hva skal vi egentlig bruke det til?

:open-graph/description

Om hvordan køer kan forenkle og forbedre prosesser som flytter data mellom
systemer.

:blog-post/body

Teamet vårt bygger et nytt fagsystem for inspektører i Mattilsynet som jobber
med
[smilefjesordningen](https://www.mattilsynet.no/mat-og-drikke/forbrukere/smilefjesordningen).
Inspektørene bruker i dag i et eldre system, og en del av vår jobb dreier seg om
å flytte data fra dette systemet til det vi nå bygger.

Det nye systemet har en rikere datamodell enn det gamle og bruker en [annen
database](/smakebiter-av-datomic/), så når jeg sier "flytte data" mener jeg
egentlig å eksportere de dataene vi kan bruke, massere disse og så importere de
i det nye systemet.

## Synkron eksport/import

Den mest tilforlatelige måten å flytte data fra et system til et annet er å
skrive litt kode som leser fra en database eller et API, løper gjennom
resultatsettet og skriver til ny database/API. Denne tilnærmingen har en del
begrensninger:

- Koden som eksporterer og koden som importerer må være samlokalisert.
- Uforutsette problemer underveis stopper hele prossessen.
- Du har lite innsikt i hvilke data som er i transit (utover hva du har husket
  på å logge).
- Eksport og import må foregå samtidig.

## Asynkron eksport/import via en kø

Vi kan løse alle disse problemene ved å plassere en kø i midten av prossessen.
Så hvordan gjør vi det?

1. Opprett en kø med passende semantikk.
2. Skriv om eksporten til å lese fra kildesystemet og publisere meldinger på
   køen.
3. Skriv om importen til å lese fra køen, massere meldingen og importere den.

Nå er eksport og import to separate prosesser, og kommunikasjonen mellom dem
ligger på en fysisk plass som vi kan inspisere for å få innblikk i hva som
skjer.

Med to separate prosesser trenger ikke kode for eksport og import lenger å være
samlokalisert. Siden eksport-koden handler om det gamle systemet har den liten
verdi i kodebasen til det nye systemet, og kan dermed flyttes ut. Eksport og
import trenger heller ikke å kjøre på samme tid.

"Problemer underveis" kan nå deles i to: problemer med eksport eller problemer
med import. Siden eksporten kun skal lese data og legge dem på en kø er det
lite som kan gå galt her.

Oppstår det problemer underveis i importen har vi nå køsemantikk å hvile oss på.
Vi kan feks la være å "ack"-e meldingen som feiler, slik at den blir liggende
igjen på køen for å prøves igjen senere -- etter at vi har rettet feilen i
koden. Vi kan også legge meldingen på en dedikert kø for feil.

Siden kommunikasjonen nå foregår over en kø har vi full innsikt i all data som
flyttes, og vi kan til og med bruke NATS-tooling til å følge med på hvor mange
meldinger som er publisert, hvor mange som er prosessert, og hvor mange som har
feilet.

## En kø med "passende semantikk"

Så, hva er egentlig passende semantikk i dette tilfellet? Vi har valgt å løse
import med en arbeidskø, "WorkQueue" i NATS-terminologi. Meldingene på en
arbeidskø forsvinner idet en konsument har "ack"-et den: altså kan hver melding
kun behandles én gang. Det er i grunnen akkurat det vi ønsker for en import.

Med en arbeidskø er ikke eksporten persistent etter at meldingen er prosessert,
men vi har andre mekanismer på plass for å spare på disse dataene.

Sånn ser kø-konfigurasjonen vår ut:

```clj
(import '[clj-nats.core :as nats]
        '[clj-nats.stream :as stream])

(def conn (nats/connect "nats://localhost:4222"))

(stream/create-stream conn
  {:nats.stream/name "matnyttig-mats-import"
   :nats.stream/description "Work queue for å importere tilsynsobjekter fra MATS"
   :nats.stream/subjects #{"matnyttig.mats-import.>"}
   :nats.stream/retention-policy :nats.retention-policy/work-queue
   :nats.stream/allow-direct? true
   :nats.stream/allow-rollup? false
   :nats.stream/deny-delete? false
   :nats.stream/deny-purge? false})
```

Den viktigste delen av denne konfigurasjonen er:

```clj
  :nats.stream/retention-policy :nats.retention-policy/work-queue
```

Dette er policyen som gjør at NATS fjerner meldinger fra køen etterhvert som de
prosesseres. Forøvrig er det verdt å merke seg at `:nats.stream/allow-direct?
true` lar oss inspisere meldingene på køen uten å konsumere dem.

Konsumenten vår ser sånn ut:

```clj
(consumer/create-consumer conn
  {:nats.consumer/stream-name "matnyttig-mats-import"
   :nats.consumer/name "matnyttig-mats-importer"
   :nats.consumer/ack-policy :nats.ack-policy/explicit    ;; 1
   :nats.consumer/deliver-policy :nats.deliver-policy/all ;; 2
   :nats.consumer/durable? true                           ;; 3
   :nats.consumer/max-deliver 3})                         ;; 4
```

1. Eksplisitt "ack" betyr at vi må si fra til NATS når meldingen er trygt
   importert (som forteller NATS at den kan fjernes fra køen).
2. Vi ønsker å få alle meldingene siden tidenes morgen (ikke kun de som
   publiseres etter at konsumenten kommer opp osv)
3. Konsumentens tilstand lagres på serveren. Om vi avbryter importen vil den
   fortsette der den slapp når den kommer opp igjen.
4. Hvis vi ikke klarer å importere meldingen etter 3 forsøk gir vi opp.

Og der har du det: Ved å flytte kommunikasjonen mellom to prosesser til en kø
har vi fått mye bedre kontroll og innsikt i dem, samtidig som vi har fjernet
avhengigheten mellom dem. "I like it a lot!"
