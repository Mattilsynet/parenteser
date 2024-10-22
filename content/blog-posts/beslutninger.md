:page/title Voffor gör dom på detta viset?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-10-22T09:00:00"
:blog-post/tags [:metodikk]
:blog-post/description

Dokumentasjon er et viktig tillegg til kode for at et system skal ha et langt og
fruktbart liv. Men hva skal vi dokumentere?

:blog-post/body

Dokumentasjon er et viktig tillegg til kode for at et system skal ha et langt og
fruktbart liv. Men hva skal vi dokumentere?

Ved å lese koden får vi (som regel) svar på både hva den gjør, og hvordan.
Dokumentasjon kan hjelpe oss med å forstå hvorfor koden er som den er. Hvilke
antagelser som ligger til grunn. Hvilke forenklinger av verden vi har gjort.

## Dokumenter viktige beslutninger

I våre kodebaser skriver vi såkalte
[ADR-er](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions),
"architecture decision records". Det høres veldig flott ut, men er enkle greier.

Målet med en ADR er å dokumentere en beslutning som påvirker strukturen på
koden, ikke-funksjonelle karakteristikker, avhengigheter, måten vi løser et
spesifikt problem på, eller som på annet vis anses som viktig for koden vi
skriver.

En ADR er en tekstfil, og handler om én konkret beslutning. Omtrent som et
blogginnlegg, men med en noe mer regissert form. En ADR har alltid disse
punktene:

- **Tittel:** En kort og poengtert oppsummering av beslutningen
- **Kontekst:** En **nøytral** beskrivelse av relevante fakta som utgjør
  konteksten vi tar beslutningen i. Tekniske og ikke-tekniske omstendigheter,
  problemer vi trenger å løse, eksterne begrensninger og krav (eksempelvis
  tidsfrister), og mer til.
- **Beslutning:** En kort beskrivelse av hva vi har besluttet.
- **Konsekvenser:** En beskrivelse av situasjonen etter at beslutningen har trådt
  i kraft. De færreste beslutninger har kun positive konsekvenser, og det er
  viktig å få ned både fordeler og ulemper med beslutningen man har tatt.
- **Alternativer:** Alternative løsninger som ble vurdert og deres styrker og
  svakheter.

## Hva er aktuelle temaer for ADR-er?

Hva skal vi så lage ADR for? La oss si at det kommer en ny utvikler på teamet.
Hva er noen styrende prinsipper i kodebasen du ville formidlet til vedkommende?
Hva er ting de ville spurt om? Sånne ting er flotte kandidater for en ADR. Andre
eksempler kan være valg av rammeverk, REST vs GraphQL vs noe annet, elementer av
kodestil osv.

Her er noen eksempler fra kodebasen vi jobber med for tiden:

- ADR 1: "Omfattet av ordningen" er utledet informasjon som ikke lagres
- ADR 2: Bruk av Datomic entities i koden
- ADR 3: Bruk av id-er mens MATS er master for serveringssteder
- ADR 4: Innsikt i kjøretidsmiljøet med OpenTelemetry tracing, ikke tekst-logger
- ADR 8: Funksjonell kjerne, imperativt skall
- ADR 10: Defensiv kode og parametere inn i systemet

Hvis du syns den siste ser kjent ut, så er det nok fordi Magnar nylig
omformulerte den som [en bloggpost](/forsvar-mot-svartekunster/).

Da vi bygget [Matvaretabellen](https://matvaretabellen.no) i fjor skrev vi også
[noen
ADR-er](https://github.com/Mattilsynet/matvaretabellen-deux/blob/main/adr/),
blant annet om det strategiske valget [å bruke en statisk generert
site](https://github.com/Mattilsynet/matvaretabellen-deux/blob/main/adr/02-statisk-site.md).
Her er et eksempel på fordeler og ulemper fra dens seksjon om konsekvenser:

> Fordeler
> - Løsningen blir rask, stabil og krever få ressurser.
> - Løsningen får lavt behov for drift og vedlikehold.
> - Løsningen krever lite monitorering og overvåkning.
> - Løsningen kan verifiseres grundig før deploy (lenkesjekk på hele nettstedet,
>   sjekke at alle bilder og assets er gyldige, etc).
> - Løsningen får billig drift.
>
> Ulemper
>
> - Kan ikke ha dynamisk server-generert innhold, feks brukerdefinerte
>   Excel-filer (disse lages heller som CSV på klienten).
> - Bruker relativt mange byggeminutter på Github actions.
> - Bygget blir nokså treigt (10-20 minutter forsinkelse til deploy ved push).

Vi innbiller oss ikke at beslutningen er utelukkende positiv, men dokumentet
forteller at vi har bestemt oss for at fordelene veier opp for ulempene, og sier
litt om hvorfor.

## Når skal vi skrive ADR-er?

Etter å ha samlet opp en liten håndfull havarerte ADR-er har jeg en oppfordring:
Skriv ADR-en i det øyeblikket beslutningen er tatt! Start gjerne på den mens
diskusjonen pågår for den saks skyld.

ADR-er belyser hvilke aspekter vi vurderte da vi tok en beslutning, og kan
argumentere litt for og i mot både valgt beslutning og forkastede alternativer.
Når beslutningen er tatt har vi det med å internalisere den så grundig at det er
vanskelig å retroaktivt skrive ned argumentasjonen. Beslutningen virker så
åpenbar når den er tatt at et dokument som beskriver den fremstår som
meningsløst.

Så pass på å fange verdifull kontekst ved å skrive ADR-en med én gang
beslutningen er tatt. Ikke på slutten av dagen.

## Formater og variasjoner

Mange har sikkert erfaring med ADR-er som ser litt annerledes ut enn våre. Vi
bryter sågar litt med [Michael Nygaard sin opprinnelige definisjon av
ADR](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
ved å utelate status. Det er helt ok. Disse dokumentene er ment å være
lettvektsdokumentasjon som gir andre viktig kontekst for å forstå koden. Det
kan vi gjøre på en måte som er tilpasset vårt behov.

I tillegg til variasjon i formater finnes det [bøttevis med tooling, templates
og gudene vet
hva](https://github.com/joelparkerhenderson/architecture-decision-record), men
min anbefaling er å hoppe over det. Ikke gjør det så vanskelig. Lag en tekstfil,
skriv ned kontekst, beslutning og konsekvenser, og kall det en dag. Fremtidige
kolleger -- og deg -- kommer til å takke deg.
