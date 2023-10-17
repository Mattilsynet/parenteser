:page/title Lange flate filer
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2023-10-17T09:00:00"
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
starte på full sprint. Januar ble ille moro, som vi sier i Fredrikstad. Det ble
så bra som jeg hadde drømt. På prodserveren lå all HTML og CSS ferdig trykket
opp på disk, servert av NGINX, med Varnish foran.

Takket være Varnish ble filene endatil servert *rett fra minnet.*

Så ja, nettsidene ble lynraske, men det var noe annet som gikk fort også. Mye fortere,
faktisk, enn jeg hadde trodd.

## En enklere modell

Har du tenkt på hvor mye tid og mental kapasitet som går med fordi vi må
forholde oss til en kjørende serverprosess?

Ikke jeg heller.

I hvert fall ikke før dette prosjektet.

I tillegg til kompilering og byggesteg, så har den kjørende prosessen sin egen
runtime, med sine egne avhengigheter, sin egen state, sine egne bugs. Det er
asynkrone kall, fra frontend, til frontend-for-backend, til backend, til
database. Det må driftes og vedlikeholdes, monitoreres og logges. Exceptions og
nedetid. Model, View, Controller. Det er et ræl.

Okay, så er det ofte nødvendig ... et nødvendig ræl. Men hva med de gangene vi
ikke *må* ha denne kjørende prosessen? Mange nettsider skal bare presentere
informasjon. Med ett kan løsningen være noe dramatisk enklere.

Det var det som overrasket meg den gangen for ti år siden. Uten en server
kjørende var det så mange hensyn jeg kunne dumpe, som en bør jeg ikke lenger
trengte bære. Min jobb ble mye enklere. Jeg kvernet noe data og spyttet litt
HTML ut på en disk. Som et byggesteg. Ferdig.

Ikke rart det gikk fort å lage.

## Serverniks

Eller var det servernada?

Uansett, modellen for å få dette på nett ble såre enkel. Den gangen brukte jeg
nginx og et filsystem. I nyere tid, enda bedre: Vi lagrer alle filene i en bøtte
på S3 / Google Cloud Storage og peker lastbalansereren rett på den.

Det er så lite stress at man kan begynne å le.

Det er dette som er serverless.

## Til slutt

Dette er en måte å bygge nettsider vi har hatt
[stor](https://www.kodemaker.no)
[glede](https://techdocs.spid.no)
[av](https://emacsrocks.com)
[opp](https://strom.fortum.no)
[gjennom](https://cjohansen.no)
[årene](https://www.parens-of-the-dead.com).
Her i Team Mat hos Mattilsynet har vi enn så lenge to nettsteder som lages på denne måten:
Denne bloggen, og den nye [matvaretabellen.no](https://matvaretabellen.no) som
vi driver og jobber med.

Så ja, jeg tror vi kommer til å skrive mer om statiske sider på denne bloggen
fremover. Mistenker at Christian har et og annet å fortelle om [Stasis
Powerpack](https://github.com/cjohansen/powerpack), for eksempel.
