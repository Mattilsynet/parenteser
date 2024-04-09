:page/title PubSub med NATS
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-09T09:00:00"
:blog-post/tags [:nats]
:blog-post/series {:series/id :nats}
:blog-post/description

[NATS](https://nats.io) er et meldingssystem med mange bruksmønstre: fra pubsub,
arbeidskøer og strømming av persistente logger til key/value og object storage.
Alt fra den samme primitiven -- emnebasert meldingsutveksling -- som vi i dag
ser litt nærmere på via PubSub med NATS.

:open-graph/description

En introduksjon til emnebasert meldingsutveksling i NATS med PubSub som
eksempel.

:blog-post/body

[NATS](https://nats.io) er et meldingssystem med så mange bruksmønstre at det
kan fremstå som litt overveldende. Vi snakker om et system som alene kan
erstatte både Redis, RabbitMQ og Kafka. Heldigvis er NATS elegant designet rundt
primitiven emnebasert meldingsutveksling ("subject-based messaging"), og med en
god forståelse av hva det innebærer kan vi lett forstå hvordan alt henger
sammen.

## Hei på deg med NATS

La oss starte med et eksempel. Det er fort gjort å installere NATS: du trenger
bare å laste ned [den frittstående kjørbare fila](https://nats.io/download/) og
starte den. På OSX kan du slippe unna med `brew install nats-server` for
serveren, og `brew tap nats-io/nats-tools` og så `brew install
nats-io/nats-tools/nats` for CLI-et.

For å starte serveren åpner du en terminal og skriver:

```sh
nats-server
```

For å få meldinger i PubSub må du aktivt lytte etter meldinger i det øyeblikket
de blir sendt -- meldingene blir ikke persistert på serveren. Vi må derfor
starte med å lytte etter meldinger:

```sh
nats subscribe chat.general
```

Dette setter opp en abonnent som lytter på meldinger på emnet `chat.general`. Vi
kan publisere dit fra en annen terminal:

```sh
nats publish chat.general "Hei, verden!"
```

Den første terminalen din vil nå få meldingen:

```sh
$ nats subscribe chat.general
13:37:58 Subscribing on chat.general
[#1] Received on "chat.general"
Hei, verden
```

PubSub er mange-til-mange. Med andre ord kan du både publisere til emnet fra
vilkårlig mange steder, og lytte så mange ganger du vil. Alle som lytter får
alle meldingene.

Meldingene trenger ikke å være tekst som i disse eksemplene. Under panseret er
meldingene rå bytes, så du kan i prinsippet sende hva som helst. Som JSON, EDN
eller protobuf.

## Emnebasert meldingsutveksling

Når du bruker NATS kan du bygge integrasjoner mellom systemer over emner, ala
`chat.general` over. Så heller enn å snakke om hvor forskjellige systemer bor,
og så gjøre REST-kall eller lignende til dem, så kan du bruke NATS som en
informasjonshub der du heller snakker om hvilke emner som interesserer deg. Det
er en mer dataorientert tilnærming til integrasjon.

For å understøtte slik kommunikasjon har NATS noen verktøy for å jobbe med
emner. Spesifikt skiller punktumet *tokens* fra hverandre, slik at et emne
representerer et hierarki. Du kan bruke `*` som wildcard for ett token, og `>`
som et wildcard for "alle nivåer herfra og ut". La oss belyse med et eksempel.

I det første eksempelet delte vi chat-meldinger. La oss raffinere eksempelet ved
å publisere alle meldinger til et emne `chat.<kanal>.<bruker>`. Alle meldingene
bærer med seg informasjon om hvilket emne de ble publisert til, så selv med kun
tekst som meldings-body vil dette gi oss mer informasjon.

Vi kan nå lytte til alle meldinger på en kanal på følgende vis:

```sh
nats subscribe chat.general.*
```

Dette gir oss alle meldinger under topp-emnet `chat.general`. Ved å publisere
meldinger på emner som `chat.general.christian` og `chat.general.magnar` kan vi
observere følgende meldingsutveksling:

```sh
$ nats subscribe 'chat.general.*'
14:19:23 Subscribing on chat.general.*
[#1] Received on "chat.general.christian"
God morgen

[#2] Received on "chat.general.magnar"
Halla

[#3] Received on "chat.general.christian"
Hva skal vi gjøre i dag?

[#4] Received on "chat.general.magnar"
Skrive litt Clojure?
```

For å lytte på alle meldinger på alle kanaler kan vi bruke stjernen også for
kanal, altså `nats subscribe 'chat.*.*'` -- eller bruke `nats subscribe
'chat.>'`, som oppnår det samme, men for et vilkårlig antall nivåer.

Stjernen kan brukes til å "wildcarde" et hvilket som helst nivå. Dette vil
eksempelvis gi meg alle meldingene til Magnar, uavhengig av kanal:

```sh
$ nats subscribe 'chat.*.magnar'
14:25:04 Subscribing on chat.*.magnar
[#1] Received on "chat.general.magnar"
Halla

[#2] Received on "chat.general.magnar"
Skrive litt Clojure?

[#3] Received on "chat.team-mat.magnar"
Jeg og Christian setter oss og skriver litt Clojure
```

Det er verdt å merke seg at filtreringen av meldinger skjer på NATS-serveren --
ikke på klienten. Med andre ord gir NATS deg stor frihet til å fordele
arbeid/interesse mellom klienter med lav kostnad på klientsiden.

I senere innlegg skal vi se hvilke spennende muligheter som dukker opp når vi
legger persistens på denne modellen.
