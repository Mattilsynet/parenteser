:page/title Se på alt jeg ikke gjør
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2025-01-21T08:30:00"
:blog-post/tags [:metodikk]
:blog-post/description

Er det noen programmeringspraksiser som har større glede av auto complete av en
LLM enn andre? La oss ta en titt.

:blog-post/body

Da jeg skrev [litt om hvorfor vi ikke bruker proaktive LLM-er i editoren
vår](/tenke-selv/) kom det reaksjoner. Noen var enig, andre var kritiske.
Enkelte gikk så langt som å antyde at jeg ikke har peiling på hva jeg holder på
med. Kanskje. Men kan det være noe med måten vi jobber på som også kaster lys på
situasjonen?

Da vi intervjuet folk til teamet vårt i høst hadde vi med oss personallederen
for utviklerne. Han er selv utvikler, men jobber ikke i Clojure. Han hadde to
bemerkninger etter kodeøvelsen i intervjuene som jeg syns var interessante:

- Fy søren, så herlig med den raske feedback-loopen ([REPL](https://www.kodemaker.no/blogg/2022-10-repl/)-et)
- Trenger ikke disse folka å lese dokumentasjon?

Hvordan kan det ha seg at vi ikke leser dokumentasjon? Vi gjør det altså, men
åpenbart mindre enn mange andre. Og grunnene til det kan også være med på å
forklare hvorfor vi ikke får så mye glede av proaktiv AI auto complete:

- Vi kan vårt valgte programmeringsspråk inn og ut
- Vi bruker ikke rammeverk
- Vi bruker få og avgrensede biblioteker
- Vi kan editoren vår til fingerspissene

Dette ser nok umiddelbart ut som renspikka skryt. Men det er ikke så enkelt. Jeg
vil heller kalle dette strategiske valg over en hel karriere:

- Vi har valgt bort å prøve hvert eneste nye programmeringsspråk som kommer ut.
- Vi har stått over minst tre store byggesystemer i JavaScript-økosystemet.
- Vi hoppa av React før de totalrenoverte API-et sitt første gang.
- Vi har sittet i Emacs siden før smarttelefonene(!)

Ok, så vi lærer ikke nye ting vi da? Og det er denne bakstreverskheten som gjør
at vi ikke bruker Copilot? AHA! Nok en gang er det ikke så enkelt. Vi lærer nye
ting ukentlig, men vi legger hovedfokuset på ting som gir oss større
mulighetsrom, i stedet for å prøve ørten forskjellige måter å svare på en HTTP
request. Scroll gjerne gjennom bloggen for eksempler på læring fra det siste
året.

I Clojure jobber man stort sett med generiske datastrukturer som maps, lister og
sets. Språket har et enestående standardbibliotek for å manipulere disse. Dermed
trenger vi ikke så mange biblioteker.

Clojure er et uttrykksfullt språk med gode abstraksjoner. En enkelt linje kode
kan fort bli 10 eller flere linjer med Java eller Kotlin. På toppen av dette har
vi valgt oss en arkitektur og data-drevet tilnærming som holder
informasjonstettheten i koden høy. Dermed trenger vi ikke så mye boilerplate.

Etter å ha holdt på sånn en stund har vi kanskje blitt gode nok til den kodinga
til at det ikke er så mye hjelp i autogenerert kode. Særlig når det uansett ikke
er så mye kode som skal til. Da oppsøker vi heller en LLM ved behov -- pull
istedenfor push, om du vil.

"Look at all the things I'm not doing" sa en entusiastisk David Heinemeier
Hanson da han [presenterte verden for Rails](https://youtu.be/Gzj723LkRJY). Den
gangen handlet det om å lene seg på noen konvensjoner for å fjerne repetetivt
arbeid med lav verdiskapning. Det målet står seg sørenmeg godt fortsatt, tjue år
senere.
