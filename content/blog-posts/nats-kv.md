:page/title NATS key/value store
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-05-07T09:00:00"
:blog-post/tags [:nats]
:blog-post/series {:series/id :nats}
:blog-post/description

At et meldingssystem også skal kunne fungere som en key/value-database ga ikke
så mye mening for meg før jeg forstod hvordan det hang sammen. Så la oss plukke
NATS sin key/value store fra hverandre.

:open-graph/description

En gjennomgang av hvordan NATS implementerer sin key/value store.

:blog-post/body

[Sist](/nats-jet-stream/) lærte vi hvordan det å persistere emner i NATS åpnet
for en haug med nye bruksmønstre. Men hvordan åpner det egentlig for at du i
mange tilfeller kan bytte ut Redis med NATS?

Når du oppretter en konsument for en strøm i NATS angir du hvilke meldinger du
ønsker å få levert med <span style="white-space: nowrap;">`--deliver`</span>:
`all`, `new`, `last`, `subject`, `1h`, `msg`, eller `sequence`. I forrige
innlegg brukte vi `all` til å lese alle meldinger siden tidenes morgen. Men hva
om vi heller hadde brukt `last`?

En key/value store i NATS er bare en strøm der nøklene er emner som starter med
navnet på en key/value bucket, og hvor du alltid konsummerer meldinger med
`last`-strategien -- altså, hvor du til enhver tid får siste verdi for en gitt
nøkkel. Så enkelt og samtidig så genialt!

Siden key/value stores er strømmer så har du også tilgang til alle flaggene vi
så på sist. Vil du ha en cache? Gi verdiene en TTL ved å sette `--max-age`. Vil
du ha historikk på nøklene? Be NATS om å ta vare på så mange versjoner du har
disk til. Verden er din østers, som det heter.

Det er verdt å nevne her at NATS i skrivende stund mangler støtte for least
recently used-semantikk, slik feks Redis har, men dette er visstnok under
arbeid.

## Et eksempel

La oss starte med å lage en bøtte for key/values:

```sh
nats kv add chat-channels
```

Med `chat-channels`-bøtta opprettet kan vi skrive noen verdier til den:

```sh
nats kv put chat-channels \
    public.general \
    '{"id": "3d5570f5-1651-4f54-9657-534dba9a78b8",
      "members": ["christian", "magnar"],
      "isPublic": true}'

nats kv put chat-channels \
    public.random \
    '{"id": "9d22fb55-bf76-4385-a955-33a944644e98",
      "members": ["christian", "magnar", "slackbot"],
      "isPublic": true}'
```

Vi kan hente ut dataene igjen med `nats kv get`:

```sh
nats kv get chat-channels \
    public.general \
    --raw \

{"id": "3d5570f5-1651-4f54-9657-534dba9a78b8",
      "members": ["christian", "magnar"],
      "isPublic": true}
```

Vel og bra. Det er en key/value store. For å få et innblikk i hvordan NATS
implementerer disse kan vi sette opp en abonnent på alle meldinger i NATS før vi
skriver nøklene:

```sh
nats subscribe '>'
```

Hvis du nå gjentar `put`-operasjonene over vil du se flere meldinger for hver,
hvor den mest interessante er denne:

```sh
[#4] Received on "$KV.chat-channels.public.general" with reply "_INBOX.0C7AtbDyU37lmByZTGAMRw.XLKKqeCX"
{"id": "3d5570f5-1651-4f54-9657-534dba9a78b8",
      "members": ["christian", "magnar"],
      "isPublic": true}
```

Her ser vi at key/value-paret sendes som en helt vanlig melding på emnet
`$KV.chat-channels.public.general`, altså `$KV.<bucket>.<key>`. Vi kan finne
strømmen også, hvis vi bruker `-a` til `stream ls` for å be NATS om å også
inkludere systemstrømmer:

```sh
nats stream ls -a

Name             │ Created             │ Messages │ Size  │ Last Message
                 |                     |          |       |
KV_chat-channels │ 2024-04-02 22:12:38 │ 2        │ 359 B │ 2m43s
```

Det er ikke så nyttig å bruke denne direkte -- de spissede key/value-verktøyene
er bedre egnet. Men jeg fant det veldig opplysende at key/value stores rett og
slett er litt convenience på toppen av strømmer, som igjen "bare" er persistens
på toppen av NATS sin emnebaserte meldingsutveksling. Så fokusert og simpelt,
men allikevel så bredt anvendbart.

## Bonus-poeng: Object store

NATS kommer forresten også med en object store ala S3, altså en key/value store
der verdiene er større elementer (NATS sin key/value kan ikke ha verdier på
større enn 64MB). Hvordan tror du den er implentert?

Kjør opp en terminal som kun ser på headerne på meldingene (med mindre du er
veldig glad i binær output):

```sh
nats subscribe '>' --headers-only
```

Opprett så en object store og last opp en eller annen stor fil (jeg fant en
video av en CSS-animasjon):

```sh
nats object add photos
nats object put photos ~/Downloads/wiggle.mp4
```

Du vil se at det går en hel del meldinger på formen:

```sh
[#827] Received on "$O.photos.C.8nio3J3KG4xr7ZlZkOpJB6" with reply "_INBOX.zIeGkC.bc859g"
```

Object storen er selvfølgelig også bare en strøm, hvor verdien er splitta ut
over flere keys.
