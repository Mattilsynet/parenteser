:page/title Informasjon for hånden
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-08-25T10:40:03.445661"
:blog-post/tags []
:blog-post/description

Å bli kjent med Clojure-kode handler om å bli kjent med informasjon, ikke typer.
La oss se hvilke konsekvenser det har i praksis.

:blog-post/body

Da jeg først skulle bli kjent med Matnyttig-kodebasen prøvde jeg å gjøre noe som har fungert for meg tidligere: lese koden, se på typene og prøve å "skjønne greia" derfra.
Det fungerte svært dårlig!

Clojure-kode kan være vanskeligere å lese enn kode skrevet i språk med typer, fordi du ikke kan lene deg på typene.
Du kan ikke vite hvilke argumenter en funksjon blir kalt med.
Du kan ikke se på typene for å få oversikt.

Hvis jeg kunne sendt et råd tilbake i tid til meg selv, ville jeg sendt følgende:

> Gjør deg kjent med koden ved å få informasjonen under koden for hånden.

Målet er at vi skal kunne stille systemet vi jobber med spørsmål, og få informasjonen vi søker som svar.
Da hjelper det ikke å se etter typesignaturer, eller å stirre på kode!

I stedet kan man gjøre følgende:

1. Bli kjent med datamodellen.
   Er det et databaseskjema?
   Er det en "kjerne" i hvordan informasjon representeres her?
2. Bli kjent med informasjonsflyten i kodebasen.
   Hvordan sendes så denne informasjonen gjennom systemet?
   Mange gode Clojure-kodebaser følger en streng informasjonsflyt.
3. Bli kjent med mekanismene du har for å hente informasjon.

I Matnyttig-koden kan jeg svare på spørsmålene som følger:

1. Vi bruker typisk nøkkelord med navnerom for informasjon som skal flyte gjennom systemet vårt.
   Data som lever lenge lever i Datomic, og har et databaseskjema.
   Data som lever kortere beskrives ofte med Clojure-specs på attributter, for eksempel `:feil/melding` eller `:nats.kv/bucket-name`.
   Vi unngår på det sterkeste å bruke samme nøkkelord med navnerom til å bety forskjellige ting.
2. Vi skiller strengt på kommandoer og spørringer ved å følge funksjonell kjerne-imperativt skall.
   Det betyr at hvis du skal lagre data, ser du etter kommandoer, og skal du hente data, ser du etter spørringer, eller skriver en ny.
3. Vi lager mekansimer for å hente informasjonen vi har bruk for.
   Fra REPL kan vi plukke ut data fra lokal database, fra prod-databasen, fra NATS, og hente ned feil.

Når jeg skal bli kjent med min neste Clojure-kodebase, kommer jeg til å jobbe for å få informasjonen fra koden for hånden, i stedet for å prøve å få oversikt over all koden.
