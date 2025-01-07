:page/title Tenke sjæl
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-01-07T08:30:00"
:blog-post/tags [:metodikk :parprogrammering :ai]
:blog-post/description

Hva går egentlig tiden med til når vi programmerer? Bli med på én økt i desember
hos oss for et anekdotisk datapunkt.

:blog-post/body

Rett før jul satt Teodor og jeg sammen for lage en feature. På forhånd hadde vi
diskutert utkastet til en løsning med Magnar, uten å gå veldig i detaljene.

Vi satt oss ned og kodet iterativt og test-drevet, omtrent sånn Magnar og jeg
demonstrerer i [parens of the dead](https://parens-of-the-dead.com/). Etter å ha
rydda unna trivialitetene stoppet det dermed litt opp når vi måtte blåse liv i
detaljene vi tidligere hadde utsatt å diskutere.

Vi prøvde oss litt frem og tilbake, skissa litt på papir og diskuterte nærmere
hva slags adferd vi egentlig ønsket oss. Og vi måtte konkludere med at vi hadde
startet med å bygge feil datastruktur. Og verre enn det, vi var på vei til helt
feil løsning.

Noen diskusjoner senere hadde vi en ny plan, som fordret en litt annerledes
datastruktur, og vi kunne kode videre. Men hva gjorde vi først? Jo, vi
slettet koden vi allerede hadde skrevet.

Hvis du vet at et stykke kode bommer på mål er det uendelig mye tyngre å prøve å
dytte den i riktig retning enn det er å starte med blanke ark og fargestifter
til, og løse problemet med ny innsikt. Dette er
[forankringseffekten](https://no.wikipedia.org/wiki/Ankring) i praksis.

4-5 timer etter at vi starta var vi i mål. I mål med hva lurer du kanskje på?
Jo, 25 linjer kode og 10 små enhetstester.

Det er 5 linjer produksjonskode i timen. På to personer. Er det bra eller
dårlig? Det får bli tema for en annen dag, men én ting er er sikkert: Det er
ikke selve handlingen å taste kode inn i editoren som er den begrensende
faktoren når vi skriver kode.

Programmering er problemløsning, og koden vi skriver er (forhåpentligvis)
resultatet av et lengre resonnement.

Mange bruker Copilot og lignende verktøy som utvider
auto-complete-funksjonaliteten i IDE-ene deres til å inkludere hele blokker med
AI-generert kode som kan fullføre det du akkurat starta på å skrive. Hjelper det
oss å tenke bedre? Snarere tvert imot: det bruker
[forankringseffekten](https://no.wikipedia.org/wiki/Ankring) mot oss og gjør
jobben vanskeligere.

Så vi koder uten copilot. Gjør det oss til
[Luditter](https://no.wikipedia.org/wiki/Ludditter)? Langt ifra. Vi bruker
generativ AI som rubber duck, dokumentasjonsoppslag, og av og til for å få
utkast til kode som løser et veldig spesifikt problem. Men vi oppsøker LLM-en
når vi trenger denne hjelpen. Hjemme i editoren vår vil vi helst tenke sjæl.
