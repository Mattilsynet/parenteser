:page/title Når meldingene presser på
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-06-11T09:00:00"
:blog-post/tags [:nats :clojure]
:open-graph/image /images/nats.png
:blog-post/description

Å ta i bruk ny teknologi er en reise. Fra at alt er nytt og desorienterende via
en slags oversikt, til noen realitetsorienterende feilsituasjoner som minner deg
på at det er stadig mer å lære. La meg fortelle deg om et konfigflagg i NATS som
nylig ga oss en sånn påminner.

:open-graph/description

En liten historie om hvor lett det er å snuble med ny teknologi, og hvordan
løsningen på tilsynelatende graverende problemer noen ganger ligger i ett enkelt
konfigflagg.

:blog-post/body

Vi bygger et nytt system med køer i [NATS](/nats/). Dette er første gang vi
bruker denne teknologien, og tradisjonen tro er ferden ikke helt fri for stein i
skoen. Jeg har tidligere skrevet om [eksport og import via en
kø](nats-import-eksport), men uten å nevne den gangen eksporten vår feila så
spektakulært at vi endte opp med en evig voksende kø av importmeldinger.

## Hva skjedde?

Vi eksporterer altså data fra et system til en kø. Når denne jobben er ferdig er
det noen tusentalls meldinger på køen -- ikke big data akkurat, men nok til å ta
knekken på noen som ikke helt vet hva de holder på med.

Konsumenten som leste fra køen var konfigurert slik:

```clj
{:nats.consumer/stream-name "matnyttig-commands"
 :nats.consumer/name "matnyttig-commands-executor"
 :nats.consumer/ack-policy :nats.ack-policy/explicit
 :nats.consumer/deliver-policy :nats.deliver-policy/all
 :nats.consumer/durable? true}
```

Her er det mange ting vi ikke sier noe om. Vi lener oss med andre ord i stor
grad på default-verdier, normalt sett et trygt valgt for nybegynnere. Men den
gang ei.

Da vi spant opp systemet som skulle håndtere disse meldingene gikk det bra en
stund. Etterhvert så vi at en del meldinger ble levert på nytt. Meldinger skal
bare leveres på nytt dersom de ikke ack-es. I vårt system ville det kun skje
dersom vi fikk en exception mens vi håndterte meldingen.

Vi bretta opp ermene og kasta så mye feilhåndtering og tracing på
meldingsmaskineriet vårt som vi klarte. Ny import, samme oppførsel. Denne gangen
hadde vi nok innsikt i maskineriet til å si sikkert at alle meldinger ble
håndtert og ack-et. Allikevel vokste køen fordi stadig flere meldinger ble
levert på nytt. WTF?!

## Er vi for treige?

Ettersom vi beviselig ack-er meldinger så var neste teori at vi kanskje brukte
for lang tid på meldingene. Så hvor lang svartid har vi da?

```clj
(consumer/get-consumer-info
  conn
  "matnyttig-commands"
  "matnyttig-commands-executor")

;;=>
;; {:nats.consumer/ack-wait #time/dur "PT30S"
;; ...
;; }
```

30 sekunder. Ifølge tracene fra maskineriet vårt bruker vi under ett sekund per
melding. Da blir neste spørsmål: Hva er det vi egentlig har 30 sekunder på å
gjøre?

## Hva betyr egentlig "levert"?

Etter litt graving fant vi ut at disse 30 sekundene er tiden vi har på oss til å
ack-e en melding _etter at den er levert_. Systemet vårt bruker beviselig mindre
enn ett sekund fra det henter meldingen til den er ack-et. Da kan man jo lure på
hva som menes med "levert"? På dette tidspunktet så Magnar og jeg på hverandre,
og sa nærmest i kor: "Nå mistenker jeg at vi er iferd med å lære noe". Det var
vi.

Konsumenter har sin tilstand på NATS-serveren. Vi begynte dermed å lure på om
det kunne ha seg sånn at konsumenten vår fikk "levert" meldinger raskere enn vi
dro dem ned. Og det skulle vise seg å være nesten riktig.

Her er mer konfigurasjon av konsumenten vår:

```clj
(consumer/get-consumer-info
  conn
  "matnyttig-commands"
  "matnyttig-commands-executor")

;;=>
;; {:nats.consumer/ack-wait #time/dur "PT30S"
;;  :nats.consumer/max-ack-pending 1000
;; ...
;; }
```

Hva betyr det at defaulten til `max-ack-pending` er 1000? Jo, konsumenten vår
påstår å klare unna opptil 1000 meldinger i det tidligere nevnte tidsvinduet på
30 sekunder. Visst brukte systemet vårt under ett sekund per melding, men all
datavasken som foregår på import tok mer enn 30 millisekunder. I tillegg ønsket
vi å prosessere meldingene i serie. Dermed klarte vi ikke 1000 meldinger på 30
sekunder.

## Løsningen

Dette var en interessant øvelse som lærte oss ett og annet om NATS. Løsningen er
helt triviell når vi nå vet hva disse bryterne egentlig gjør: Ikke be om flere
meldinger enn du klarer å tygge unna!

```clj
{:nats.consumer/stream-name "matnyttig-commands"
 :nats.consumer/name "matnyttig-commands-executor"
 :nats.consumer/ack-policy :nats.ack-policy/explicit
 :nats.consumer/deliver-policy :nats.deliver-policy/all
 :nats.consumer/durable? true
 :nats.consumer/max-ack-pending 100} ;; <== Bingo!
```

Nå er bare spørsmålet: Hvilke flere valg er det som bærer på en "spennende"
læringsopplevelse for oss i fremtiden?
