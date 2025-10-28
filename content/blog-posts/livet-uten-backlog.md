:page/title Livet uten backlog
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-10-28T07:31:10.388267"
:blog-post/tags [:metodikk :samarbeid :strategi :teamet]
:open-graph/image /images/loglady2.jpg
:blog-post/description

Hvordan skal vi huske de viktige tingene om vi ikke har en backlog?

:blog-post/body

Etter innlegget om [problemene med
backlogen](https://parenteser.mattilsynet.io/backlog/) har jeg fått mange
spørsmål om hva vi skal gjøre med alt vi ikke må glemme når vi ikke har en
backlog.

Det korte svaret: skriv det ned.

Jeg argumenterer ikke mot å dokumentere, bare mot å samle alt i en evig voksende
gjøremålsliste. La oss se på noen konkrete systemer som kan støtte arbeidet
bedre enn en liste med tickets i JIRA.

## Strategi og langsiktig mål

Det er viktig for et team å ha et langsiktig mål og en strategi, men en backlog
er ikke en strategi. Så hvor har vi strategien vår? Vi har jobbet mye med å
finne ut av hvor vi skal og hvordan vi ønsker å komme oss dit, og det
langsiktige målet lever i den kollektive bevisstheten i teamet.

Men vi har konkrete nedfellinger også. Du husker kanskje [denne videoen fra
ifjor](https://parenteser.mattilsynet.io/utviklingsstrategi/) der Magnar legger
frem utviklingsstrategien vår for å flytte et domene ut av et eldre system.

Teamets tjenestedesigner Henriette har ledet an et omfattende innsiktsarbeid som
gir oss en forståelse av økosystemet vi opererer i, og hvilke utfordringer og
muligheter folka som skal bruke systemene vi bygger står i. Dette arbeidet er
grundig dokumentert i presentasjoner, svære visualiseringer (som henger på
veggen!), post-it-lapper, videoer, notater, flytdiagrammer og mer.

Mesteparten av denne dokumentasjonen beskriver nåsituasjon og problemer som skal
løses. De har svært få løsninger. Det er risikabelt nok å spare på en
nå-situasjon om vi ikke også skulle foregripe begivenhetenes gang ved å notere
ned masse oppgaver som understøtter hva vi tror er løsningen akkurat nå, for så
å implementere dem om et halvt år.

## Alt vi ikke må glemme

Mange jeg har snakket med er bekymret for at de kommer til å glemme viktige ting
om de kvitter seg med backlogen. Som jeg også sa sist: de viktige tingene har
det med å dukke opp igjen og igjen og blir som regel ikke glemt.

Vi er som sagt midt i et omfattende arbeid med å flytte ut av en eldre monolitt.
Underveis i dette arbeidet har vi oppdaget en del detaljer som må være på plass,
men som ikke er så åpenbare. Heldigvis kan vi fortsatt skrive ned ting selv om
vi ikke har en backlog.

Vi har rett og slett en tekstfil liggende sammen med koden. Der står det ting som:

> Klienten av dette API-et forventer å kunne sortere data kronologisk ved å
> sortere på id-en. Grøss.

Dette er definitivt viktig, og samtidig ikke noe som ville tvunget seg fram
naturlig. Sånt er lurt å notere ned. Denne fila er ikke en backlog, men snarere
et sett med ferdigkriterier for deler av arbeidet vårt.

## Backlogen skuffer igjen

Vi har pleid å ha noen små todo-filer sammen med koden. Disse bryter gjerne ned
en større oppgave i mindre, gjennomførbare biter. Det startet uskyldig, men
plutselig innså vi at disse tilsammen hadde blitt en slags backlog. Denne
oppdagelsen trigget mitt [forrige
innlegg](https://parenteser.mattilsynet.io/backlog/).

I etterkant av innlegget har vi plukka fra hverandre disse todo-listene for å
bli kvitt dem. Dette arbeidet bragte med seg nok en skuffelse. Jeg fant nemlig
flere notater til ferdige oppgaver som det føltes feil å kaste. Det sto
interessante ting der. Ting vi egentlig skal skrive ned, med litt kontekst, i
små [ADR-er](https://parenteser.mattilsynet.io/beslutninger/).

Backlogen har altså forledet oss til å undergrave systemet vi selv har valgt for
å dokumentere beslutninger. Den fristet oss med enda mindre seremoni, men til
hvilken pris? Ingen leter blant ferdige todos etter viktig informasjon.

Så nå har vi fjerna listene. Den viktige informasjonen har blitt skrevet om til
ADR-er. Oppgavelister er ikke en team-effort. Utviklerne noterer ned ting som er
viktig for ukas arbeid lokalt på egen maskin.

Uten et system rundt enkeltoppgaver holder vi fokuset på ferskvare — og lar
fakta, innsikt og beslutninger leve der de hører hjemme. Samtidig sparer vi de
konkrete løsningene til vi faktisk skal jobbe med dem.
