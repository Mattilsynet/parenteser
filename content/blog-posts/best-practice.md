:page/title Best practice, eller?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-06-25T09:00:01"
:blog-post/tags []
:blog-post/description

Er "best practice" alltid best, eller kan det tenkes at det vi kaller beste
praksis først og fremst er vanlig praksis? En spennende psykologistudie kan gi
oss et hint.

:blog-post/body

I 1984 beskrev Eliyahu Goldratt [theory of
constraints](https://en.wikipedia.org/wiki/Theory_of_constraints) i den
skjønnlitterære boken [The
Goal](https://archive.org/details/goalprocessofo00gold). Boken ble en
braksuksess og er fortsatt en bestselger på Amazon. Selskapene som har
implementert lærdommen fra boken har nærmest uten unntak hatt tilsvarende
suksess som Goldratt beskrev.

"Alle" var enige om at The Goal beskriver en bedre måte å drive en fabrikk på,
men allikevel fant Goldratt 20 år senere at kun 2% av verdens fabrikker hadde
implementert idéene hans. Resten gjorde ting på den gamle måten, som så grundig
blir plukket fra hverandre i boka. Hvordan kan det ha seg, og hva sier det oss
om "best practice"?

## En studie av konformitet

På 50-tallet forsket Solomon Asch på hvordan individer tilpasser seg en gruppe,
gjennom forsøkene kjent som [the Asch conformity
experiments](https://en.wikipedia.org/wiki/Asch_conformity_experiments).

I eksperimentene fikk deltakerne presentert to bilder, og skulle svare ut
hvilken av linjene A/B/C som hadde samme lengde som linjen til venstre.

<img src="/images/asch.png" class="img" style="max-height: 400px; margin: 0 auto;
display: block;">

Oppgaven i seg selv er enkel nok. Men, i gruppene på 7-9 mennesker var det kun
én reell deltaker -- resten var skuespillere. Deltakeren svarte alltid sist.
Skuespillerne ga alltid samme svar, men ikke alltid det riktige. Asch ønsket å
finne ut av om deltakerne våget å stå i sitt rette svar når alle andre i rommet
ga et annet (åpenbart feil) svar.

Funnene er ikke oppløftende. 74% av deltakerne ga samme **feil** svar som
skuespillerne i minst ett av forsøkene. Som forskeren selv sa etterpå: "That
intelligent, well-meaning young people are willing to call white black is a
matter of concern."

## Best practice, eller?

Hva kan vi trekke ut av dette til vårt virke som softwareutviklere? Mange valg
vi tar informeres helt eller delvis av at mange andre gjør det samme: det er
"best practice", industristandard, eller har flest stjerner på Github. Jo fler
som følger etter, jo vanskeligere blir det å gå mot strømmen.

Å gå i ferdig opptråkkede løyper kan føles trygt. Dessverre ignorerer vi ofte
kontekst når vi velger etter popularitet. Ja, mange andre gjør dette, men hadde
de samme utgangspunkt som oss? Hvem vet. Som regel betyr beste praksis bare at
noe er utbredt eller vanlig. Men betyr det at det er best? Eller i det hele tatt
bra?

Så hva kan vi gjøre? Vi bør pensjonere beste praksis og lignende som argument,
for det er de ikke. Kanskje bør vi også se litt ekstra nøye på valgene vi har
tatt som har endt opp med de mest populære løsningene. Er React virkelig rett
svar for oss, eller endte vi opp der fordi det er industristandard?

Vi bør øve oss på å snakke om fordeler og ulemper med idéene vi legger frem og
teknologiene vi vurderer. Bare når vi har begge delene på bordet kan vi ta gode
avgjørelser. Da kan vi ta gode valg som vi tør å stå i, selv om de går imot
trendene i bransjen. Husk, noen ganger er det bare 2% som har plukket opp de
aller beste idéene -- ikke la lav utbredelse stoppe deg fra å ta de rette
valgene.
