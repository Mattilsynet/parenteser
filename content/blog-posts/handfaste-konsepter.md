:page/title Håndfaste konsepter i koden
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2025-05-20T09:00:00"
:blog-post/tags [:design :clojure]
:blog-post/description

Etterhvert som en kodebase vokser seg til, hender det at vi oppdager at et
konsept trenger spesialbehandling. Kanskje sier en: "Vi må reify-e kommandoer."
Hva i all verden betyr det?

:blog-post/body

I Clojure-miljøet slenges det rundt et underlig uttrykk: *reify*. Dette verbet
betyr å gjøre noe som er abstrakt eller vagt til noe konkret eller *håndfast*.

Etterhvert som en kodebase vokser seg til, hender det at vi oppdager at et
konsept trenger spesialbehandling. Kanskje sier en: "Vi må reify-e kommandoer."
Det betyr "Kommandoer må bli et førsteklasses konsept i koden vår." De må bli
håndfaste.

Når dette blir sagt er situasjonen ofte slik:

- Vi hadde noen få kommandoer før, men nå begynner det å bli en del av dem.
- Koden for hver enkelt kommando er strødd rundt omkring.
- Jeg må inn i mange forskjellige filer for å legge til en ny kommando.

Kanskje må man:

- Legge til kommandoen som et endepunkt i routeren.
- Legge inn en funksjon som validerer kommandoen en plass.
- Legge inn rettigheter for kommandoen i autorisasjonskoden.
- Huske å legge kommandoen inn i systemet som lager kommandologgen.
- Og ikke glem å skrive koden for selve kommandoen.

Alle disse tingene er aspekter ved en kommando -- de beskriver kommandoen -- men
på grunn av kodens behov så blir de strødd utover hele kodebasen.

## Samle alt på et sted

Jobben som ligger foran oss er i grunn ganske grei. Vi må finne alle aspektene
ved en kommando, gi dem navn, og samle dem sammen -- definere dem på samme sted,
og på samme måte.

Slik kan det ende opp å se ut:

```clj
(def planlegg-tur-command
  (command/define
    {:command/kind :commands/planlegg-tur
     :command/plan #'turer/planlegg-tur
     :command/params {:serveringssted/id {:required? true}}
     :command/rights #{:rights/føre-tilsyn}}))
```

Jeg definerer her en konkret `planlegg-tur-command` i koden, og har samlet alt
jeg trenger om den på en plass:

- `:command/kind` er navnet på kommandoen, som brukes til å referere til denne
  kommandoen både når den skal utføres, puttes på en kø, traces, og vises i
  kommandologgen.

Legg merke til at bare det å gi kommandoen et offisielt navn, som kan sendes
rundt som data med kommandoen, så rydder jeg bort flere steder i koden jeg må
huske å fikse, slik som routing, tracing og logging.

Det samme gjelder endepunktet. Nå går alle kommandoer mot `POST /commands`, og
dispatches direkte på `:command/kind`.

- `:command/plan` er selve koden for kommandoen. Jeg refererer til den med det
  mystiske `#'`-prefikset for å referere til funksjonen ved navn, slik at
  innholdet i funksjonen lett kan redefineres mens jeg utvikler.

- `:command/params` beskriver hvilke parametere jeg forventer at kommandoen min
  mottar, og brukes av valideringssystemet til å sjekke at ting er i orden, slik
  at jeg [slipper å skrive defensiv kode](/forsvar-mot-svartekunster/).

- Tilsvarende vil `:command/rights` brukes av autorisasjonskoden til å begrense
  tilgang til å utføre kommandoen.

Nå har vi fått samlet all informasjon om en kommando på samme sted, som bare
data. Resten av systemet forholder seg til disse dataene - og trenger nesten
aldri røres. Valideringskoden, autorisasjonskoden, til og med maskineriet som
utfører kommandoene, trenger nå bare endres når de skal få nye egenskaper.

<small> PS! Funksjonen <code>command/define</code> gjør "ingenting" - den
returnerer map-et slik det er, men gjør litt dev-time validering for å gi gode
feilmeldinger hvis definisjonen ikke har blitt helt riktig. </small>

I koden vår hos Team Servering i dag har vi laget håndfaste konsepter av:

- **Commands**: kommandoer fra brukere som skal gjennomføres.
- **Queries**: forespørsler om data fra klienten.
- **Pages**: de forskjellige sidene nettstedet vårt består av.
- **Sources**: navngitte kilder til data.

Alle gangene vi har vært gjennom en slik øvelse, så har koden blitt ryddigere
-- samtidig som det ble lettere å legge til flere kommandoer (med venner). Det er
også ofte det som skjer når man går fra implisitte strukturer i koden, til
eksplitte data.

## Hvordan kables dette opp?

Dette blir litt Clojure-teknisk, så hvis du ikke skal sette opp et tilsvarende
system selv, så er det lov å gi seg her.

Frem til ganske nylig pleide vi å putte disse tingene inn i et globalt definert
atom, men vi fant en bedre løsning når vi satte opp Matnyttig-systemet vårt.

Kort fortalt:

- Vi har et navnerom hvis jobb er å samle alle kommandoene i ett map:

```clj
(ns matnyttig.commands
  (:require [matnyttig.commands.tur :as tur-commands]))

(def commands
  [tur-commands/avlys-tur-command
   tur-commands/planlegg-tur-command])

(def kind->command
  (into {} (map (juxt :command/kind identity) commands)))
```

Her blir det en lang oppramsing av alle kommandoer som finnes i systemet. Noen
må uansett require' disse, så da er det bedre å samle ansvaret på ett sted: her.

Dette navnerommet har masse avhengigheter ut i koden, og brukes dermed
bare langt oppe i systemet. Kanskje i et slags [imperativt skall](/fk-is/), om
du vil.

- Kommandoene valideres og defineres med en hjelpefunksjon.

Det er denne `command/define`-funksjonen. Legg merke til at dette ikke er
`matnyttig.commands` -- den lister opp alle -- men snarere et lite
hjelpenavnerom uten avhengigheter som alle kan bruke.

```clj
(ns matnyttig.command)

(defn define [command]
  (when-not (:command/kind command)
    (throw (ex-info "Kommandoen må ha en :command/kind." command)))
  command)
```

Her er det fritt frem å bruke clojure.spec eller andre ting - målet er å gi gode
feilmeldinger hvis kommandoen ikke er definert på en vettug måte.

- Gjør det hele REPL-vennlig

Her er grunnen til at vi pleide å bruke et atom. Med en atom-implementasjon så
kunne `define`-funksjonen kjøre `swap!` på atomet og erstatte seg selv med en ny
implementasjon.

Vi starter med å lage en dynamisk bundet variabel som kan fortelle oss om vi
kjører i dev-modus eller ei:

```clj
(ns matnyttig.runtime)

(defonce ^:dynamic *dev?* nil)
```

I `start`-funksjonen vår i dev-navnerommet sørger vi for at den er satt til
`true` når vi kjører lokalt:

```clj
(alter-var-root #'runtime/*dev?* (constantly true))
```

Til slutt oppdaterer vi `define` til å reloade lista over kommandoer når en
kommando redefineres:

```clj
(defn define [command]
  (when-not (:command/kind command)
    (throw (ex-info "Kommandoen må ha en :command/kind." command)))
  (when runtime/*dev?*
    (go (<! (timeout 10))
        (require '[matnyttig.commands] :reload)))
  command)
```

Fordelen med denne tilnærmingen over et atom er at koden er statisk analyserbar,
fordi navnerommene requires slik de brukes. I tillegg har vi fått fordelen som
atomet gir, nemlig reloadbarhet i replet. En skikkelig
ja-takk-begge-deler-i-pose-og-sekk-løsning der, altså.
