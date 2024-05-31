:page/title Ferdig programvare er stabil programvare
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-03-11T09:00:00"
:blog-post/tags [:produktivitet]
:blog-post/description

I IT-bransjen endrer alt seg hele tiden, sies det. Men må det det? Hvordan skal
vi få gjort noe nytt om vi til enhver tid skal sitte å endre på det vi allerede
har laget?

:open-graph/description

Hvordan skal vi få gjort noe nytt om vi til enhver tid skal endre på det vi
allerede har laget?

:blog-post/body

Her skal du få et slags paradoks av meg: Det som virkelig skiller gode
IT-systemer fra dårlige over tid er hvor endringsdyktig kodebasen er. Verden er
i konstant endring, og IT-systemer som er vanskelige å endre på blir hengende
etter og får til slutt "legacy"-stempelet. På den annen side: Hvordan skal vi få
gjort noe nytt om vi til enhver tid skal endre på det som allerede er levert?

Svaret er at systemene våre må være endringsdyktige, mens komponentene de består
av bør i stor grad ferdigstilles og ligge urørt. Hvordan får vi til det?

## Hva er det som endrer seg?

Det er mange ting som endrer seg når vi bygger programvare. Endringer i domenet
kan vi ikke kontrollere, så de må vi ta høyde for. Tekniske endringer derimot,
kan vi ha et strategisk forhold til. Det betyr at vi må velge tredjeparts
avhengigheter med omhu, og strukturere koden vår slik at ikke alt sammen er på
kritisk sti til enhver tid.

## Ferdigstilte moduler

Endringsdyktighet er en dyd, men ikke overalt. Blant alt systemet ditt driver
med finner du helt sikkert funksjonalitet som ikke har endret seg på flere år.
Er den tilsvarende koden urørt over samme periode? Det bør den være.

Vi har nylig skrevet litt kode for å vaske og rydde i adresse-data, inspirert av
[Stigs tilnærming](https://www.kodemaker.no/blogg/2019-11-jaro-winkler-svart-magi/).
Etter en initiell innsats og innkjøringsperiode kommer vi til å ha løst de
fleste problemene vi har med datakvalitet på adresser. Og da er i grunnen denne
delen av løsningen ferdig. Da er det ingen grunn til å fortsette å pille på
koden.

Men hva med nye adresser og endringer i kommune-Norge? At datagrunnlaget endrer
seg bør ikke ha konsekvenser for koden. Last inn ny data jevnlig, og gå videre.

At adressevasken er ferdig og ute av verden gir oss flere fordeler:

1. Vi trenger ikke å bære den kognitive lasten til kode som er ferdig
2. Vi unngår risikoen for å innføre nye feil ved å pille på kode som er ferdig
3. Denne delen av systemet vil oppleves stabil for brukerne

Veldig mye av det vi lager kan isoleres på denne måten og bli ferdig. Ved å
koble de løst sammen igjen beholder vi evnen til å gjøre endringer i systemet
når domenet krever det.

## Sikkerhet og bitrot

Hvis koden ligger urørt over tid, vil den ikke da råtne på rot mens den
akkumulerer sikkerhetshull? Nei, kode råtner ikke på rot av seg selv. Det skjer
gjerne når vi bruker mange tredjeparts biblioteker og rammeverk som endres
hyppig, særlig uten å ta hensyn til
[bakoverkompatibilitet](/bakoverkompatibilitet/). Dette bør vi ta med i
beregningen når vi velger disse verktøyene.

Hva med sikkerhet? Hvis koden er skrevet på usikkert vis må den selvfølgelig
endres. Når det ikke er tilfelle bør ikke din kode trenge å endre seg for å ta
inn sikkerhetsoppgraderinger fra plattform/biblioteker/rammeverk.

Ved å velge verktøy som har som mål å være ferdige og stabile heller enn i
konstant endring, åpner vi også for at større deler av våre systemer kan være
ferdige og stabile. Da får vi mer tid til å jobbe med nye ting. På den måten kan
man bygge store systemer med små team.
