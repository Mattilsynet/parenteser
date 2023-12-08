:page/title All PR er god PR?
:blog-post/published #time/ldt "2023-12-05T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:samarbeid :metodikk]
:blog-post/description

Pull requests har blitt en så vanlig arbeidsform at det for mange er synonymt
med å bruke git. Men er det en bra ting? Må alle endringer komme via en pull
request? Jeg er ikke så sikker.

:open-graph/description

Er pull requests en god samarbeidsform internt i utviklingsteam? Jeg er ikke så
sikker.

:blog-post/body

Jeg vet ikke helt når det skjedde, men det virker som om nesten alle
utviklingsteam nå koordinerer arbeidet sitt gjennom pull requests. Det syns jeg
er rart, all den tid vi vet at [kontinuerlig
integrasjon](/kontinuerlig-integrasjon/) er [høyt
korrelert](https://dora.dev/devops-capabilities/technical/trunk-based-development/)
med høy kvalitet, kortere tid til feilretting og jevnt over gode resultater.

La oss dykke litt ned i materien og undersøke hvorfor mange velger bort
kontinuerlig integrasjon til fordel for pull requests.

## Pull requests i åpen kildekode

Pull requests kommer fra fri programvare-bevegelsen. Teknisk sett kan du si at
en epost-patch på en mailingliste er "the O.G. pull request". Github og lignende
verktøy har finpolert brukeropplevelsen rundt å bidra endringer til en kodebase,
og tilbyr gode verktøy for både automatiserte kontroller og manuell review og
diskusjon om endringene.

I fri programvare kommer endringene som oftest fra folk utenfor teamet som har
det primære ansvaret for programvaren. Disse bidragsyterne har (heldigvis) ikke
direkte commit-tilgang til kildekoden, og må gjennom noe mer byråkrati for å tilby
endringene sine. Når vi ikke engang kjenner den som har skrevet koden er det
greit med en fot i bakken før den går inn i programvare som potensielt skal
distribueres til millioner av mennesker.

Pull requests er en arbeidsflyt som er designet for miljøer med lav grad av
tillit. Hvor godt fungerer det å importere denne arbeidsflyten til team-arbeid,
altså til det som forhåpentligvis er et miljø med høy grad av tillit?

## Kode-review

Når jeg spør hva folk ønsker å oppnå med pull requests så er review av koden den
vanligste årsaken. Men får man gode reviews i en pull request? Det er nok mulig,
men jeg tror det motsatte er vanligere. Sammenlign tilbakemeldingene på små og
store pull requests: ofte får disse tilsvarende mengder tilbakemelding. Kanskje
kommer det til og med flere tilbakemeldinger på små endringer. Hvordan kan det
ha seg?

Små endringer er lette å reviewe, fordi man relativt raskt kan ta innover seg
alt som skjer og danne seg noen meninger om detaljene i koden. Tilbakemeldingene
på små pull requests vil ofte kunne kategoriseres som ["bike
shedding"](https://en.wikipedia.org/wiki/Law_of_triviality): "Kanskje denne
funksjonen heller skulle hete sånn?", "Bør vi bytte om rekkefølgen på disse
parameterne?" osv.

Store endringer er veldig krevende å reviewe, og ettersom vi tross alt er travle
med våre egne greier er det lettest å se over fra et fugleperspektiv og si "ser
bra ut for meg!" Kanskje til og med finne en eller annen liten detalj å
bike-shedde om, for å gi inntrykk av å ha satt oss skikkelig inn i det som er
gjort.

Kanskje jobber du et sted der dere har bedre kode-reviews i pull requests enn
jeg skisserer her. Det hjelper deg likevel ikke med det største problemet med å
bruke pull requests til kode-review, nemlig at man tar diskusjonen for sent.

Når noen på teamet allerede har skrevet 500 linjer med kode er lista ganske høy
for å påpeke at koden angriper feil problem, at tilnærmingen delvis gjenskaper
kode vi har fra før, eller andre større problemer. Kort sagt, at man egentlig
burde ha valgt en helt annen tilnærming. Den mest produktive (og hyggeligste)
måten å unngå dette på er å ta diskusjonen om tenkt løsning _før_ man starter
arbeidet.

## Mindre erfarne utviklere

Noen velger pull requests fordi det er utviklere på teamet som er mindre
erfarne, og ikke kan/ønsker/bør dytte ting rett på main-branchen. Det er ingen
skam i å være mindre erfaren, men å bli bedt om å lage pull requests for så å få
alle feilene sine påpekt skriftlig er både utrivelig og lite hjelpsomt.

Dersom en på teamet ikke skal drive med kontinuerlig integrasjon så bør de
heller sitte sammen med noen andre på teamet til de er trygge nok til å gjøre
det. Parprogrammering er en mye mer konstruktiv, lærerik og hyggelig måte å lære
av andre på enn skriftlig review.

## Kostnaden med pull requests

La oss si at man fikk gode kode-reviews ut av pull requests. De kommer allikevel
med en stor kostnad: De står i veien for kontinuerlig integrasjon. Koden blir
liggende på vent til noen har tid til å se på endringene dine.

Pull requests fører også til økt kontekstsvitsjing. Det er usannsynlig at noen
står klare til å vurdere endringene dine med en gang du har laget en pull
request. Når du først får tilbakemeldinger dagen etterpå er du kanskje allerede
igang med noe annet, og må tilbake inn i tidligere arbeid. Når du så får pull
requesten igjennom så kan det hende at noe går galt når koden går i produksjon.
Da må du slippe det du har i henda, og gå inn i potensielt flere dager gammelt
arbeid for å finne ut av hva som skjer. Ikke gøy.

## Alternativer

Så hvis vi ikke skal drive med pull requests, hva skal vi da gjøre? Crazy
forslag: commit kode, push til main-branchen. Men hva med kode-review? Her har
jeg to forslag. Noe kode er viktigere enn annen. Valgene vi tar i starten av et
prosjekt, eller under utformingen av nye større features, er viktigere enn de vi
tar mens vi legger til feature #39 i en etablert kodebase. Disse bør dermed
behandles ulikt.

Når vi utformer datamodellen og bygger eller videreutvikler systemets arkitektur
så bør vi jobbe sammen. Altså, parprogrammering, eller -- om det er flere --
såkalt mob programmering. Det er ingen god grunn til å la enkeltpersoner sitte
alene med så viktige oppgaver og tro at man kan redde det i land med et pull
request review etterpå.

Kode som blir til under parprogrammering blir mer gjennomtenkt, mer solid, og
kommer ferdig reviewet ned på papiret. I tillegg bygger man "vår kode" sammen, i
stedet for din og min kode.

Når man sammen har funnet et godt fundament kan man få gjort mer på den samme
tiden ved å dele seg og jobbe med hver sine features - eller forskjellige
aspekter av samme feature. Hvordan skal da denne koden reviewes?

Se for deg at du har laget noen commits, men du får ikke dytta dem til Github.
Noen andre har kommet deg i forkjøpet. Ettersom du har commitet har du også
nettopp avsluttet et stykke arbeid. Perfekt timing for å dra ned de siste
commitene på main-branchen og kikke litt på de andres arbeid. Hvis du ser noe du
er nysgjerrig på så rusler du bort til personen som har signert arbeidet og slår
av en prat. Det er faktisk helt mulig å endre litt på kode etter at det kommer
på main-branchen også.

## Skal jeg aldri bruke pull requests?

Pull requests hører ikke hjemme i team-arbeid. Men det er en fantastisk
samarbeidsform for fri programvare. Hvordan kan det ha seg? Kontekst! Man kan
ikke bare rive en prosess ut av sin opprinnelige kontekst (miljøer med lav
tillit), kalle dem for "best practice" og stappe dem inn i helt forskjellige
kontekster (miljøer med høy tillit) og forvente samme resultat. Dessverre er
bransjen vår veldig god på akkurat dette.

Pull requests kan allikevel ha en funksjon på arbeidsplassen, nemlig mellom
team. Dette er en kontekst som ligner på pull requestens naturlige habitat: Du
vil bidra til noen andres kodebase.

## Et liv uten pull requests

Vårt lille team har nylig lansert nye
[matvaretabellen.no](https://www.matvaretabellen.no). Hvis du kikker på
[commit-loggen](https://github.com/Mattilsynet/matvaretabellen-deux/commits?after=e9299371a774a33ce7920bc467008e259c045a93+664)
vil du se at mange av de tidlige commitene har to committere -- parprogrammert
kode. Akkurat som jeg foreslo over. Men du trenger ikke bare å ta mitt ord for
at dette er gode greier. Det finnes masse
[forskning](https://cloud.google.com/devops/state-of-devops/) som bygger opp
under de positive effektene av kontinuerlig integrasjon, som atpåtil er
oppsummert i [en veldig god
bok](https://www.amazon.com/Accelerate-Software-Performing-Technology-Organizations/dp/1942788339).
Se også ["trunk-based
development"](https://dora.dev/devops-capabilities/technical/trunk-based-development/)
fra DORA for oppdatert forskning.
