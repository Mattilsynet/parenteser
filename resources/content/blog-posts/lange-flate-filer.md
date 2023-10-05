:page/title Lange flate filer
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2023-10-10T09:00:00"
:blog-post/description

Du skal lete lenge etter noe enklere og mer snappy enn å servere statisk HTML og
CSS til folk. Hørt om "serverless"? Niks, her er det servernada!
Jeg skriver om en grovt undervurdert teknikk for å lage nettsider.

:open-graph/description

Et par ord om en grovt undervurdert teknikk for å lage nettsider.

:blog-post/body

Da jeg skulle lage [kodemaker.no](https://www.kodemaker.no) for ti år siden
hadde jeg nettopp lest og gjenlest boka [High Performance Web
Sites](https://biblio.co.uk/book/high-performance-web-sites-essential-knowledge/d/920516130?aid=frg&gclid=CjwKCAjwvfmoBhAwEiwAG2tqzJsOBzLsRGv24PNBTUuQZphsofoPkp-Fn4q0SWBPYuwRg-hMbYCfABoC7eoQAvD_BwE)
av [Steve Souders](https://stevesouders.com). Jeg elsket den boka! Raske
nettsider var i høyeste grad min greie. Steve og jeg var bestevenner -- i hvert
fall inni hodet mitt. Jeg husker at jeg stod på togperrongen. Ti minutter til
toget kom? Ned i sekken og opp med boka for å lese litt til (man lagde bøker
av døde trær den gangen).

Uansett, det måtte gå fort, det var jeg sikker på. Derfor var jeg fast bestemt
på at de nye sidene skulle trykkes opp på forhånd. Alt innholdet i flate filer,
servert rett fra disk. Ingen kjørende server. Man sparer mangfoldige
millisekunder på sånt.

Jeg brukte juleferien til å lage [Stasis](https://github.com/magnars/stasis), en
verktøykasse for å bygge statiske nettsider, slik at arbeidet i januar kunne
starte på full sprint. Og fort gikk det. Allerede første uka hadde jeg nye sider
ute. All HTML og CSS lå på disk, servert av NGINX, med Varnish foran.

Med Varnish ble filene ikke engang servert fra spinnende disker, men *rett fra
minnet!*

Så ja, nettsidene ble lynraske, men det var noe annet som gikk fort også. Mye fortere,
faktisk, enn jeg hadde trodd.

## En enklere modell

Har du tenkt på hvor mye tid og mental kapasitet som går med fordi vi må
forholde oss til en kjørende serverprosess?

Ikke jeg heller.

I hvert fall ikke før dette prosjektet.

Ikke bare skal det driftes og vedlikeholdes, men i tillegg til kompilering og
byggesteg, så har selve prosessen sin egen runtime, med sine egne avhengigheter.
Det er asynkrone kall, fra frontend, til frontend-for-backend, til backend,
til database. Det må monitoreres og logges. Exceptions og nedetid. Det er et
ræl.

Okay, så er det ofte nødvendig ... et nødvendig ræl. Men hva med når vi ikke *må* ha
denne kjørende prosessen? Hva om man bare bygger et nettsted fullt av innhold?

Det var det som skjedde den gangen for ti år siden. Det var så mange hensyn jeg
ikke trengte ta. Min jobb var ganske enkelt å kverne noe data og spytte ut litt
HTML på en disk.

Ikke rart det gikk fort å lage.

## Serverniks

Eller var det servernada?

Uansett, modellen for servering er såre enkel. Den gangen brukte jeg nginx og et
filsystem. I nyere tid, enda bedre: Vi lagrer alle filene i en bøtte på S3 / Google
Cloud Storage og peker lastbalansereren rett på den.

Det er så lite stress at man kan begynne å le. Det er dette som er serverless.

## Til slutt

Jeg tror vi kommer til å skrive mer om statiske sider på denne bloggen fremover.
Dette er en måte å bygge nettsider vi har hatt stor glede av opp gjennom årene.
Denne bloggen er naturligvis laget slik, men også den nye
[matvaretabellen.no](https://matvaretabellen.no) tror vi nå skal lages på samme
måte.

Hvis du skal ta med deg noe nyttig fra denne bloggposten, så er det dette:

Vi trenger ikke alltid en server kjørende, men kan heller trykke ut sider med
HTML i et byggesteg. Da får vi:

- lynraske sider
- færre kjørende prosesser å overvåke og betale for
- lavere karbonfotavtrykk

Når vi også bygger dette opp som krysslenka innholdssider på egen URL, så er vi i tillegg:

- lettere å lenke til
- lettere å bokmerke
- optimalisert for søkemotorer

Alt i alt et par Kinderegg* av godsaker der, altså.

\* forøvrig en merkevare som ikke er å finne i matvaretabellen i dag


