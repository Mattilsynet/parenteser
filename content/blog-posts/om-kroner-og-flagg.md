:page/title Uferdige greier rett i prod: Om kronsteiner og brytere
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-02-27T09:00:00"
:blog-post/tags [:samarbeid :metodikk :design]
:blog-post/description

Vi dytter kode rett til main, som bygges rett ut i prod. Ingen feature branches.
Ikke noe staging-miljø. Men hvordan funker det når vi skal lage omfattende funksjonalitet?

:blog-post/body

Som Christian har gjort [ganske](/pull-requests/)
[tydelig](/kontinuerlig-integrasjon/) så dytter vi kode rett på main-branchen,
som deretter bygges rett ut i produksjon. Det første er kontinuerlig
integrasjon, det andre kontinuerlig leveranse. Det er ikke bare en bra måte å få
funksjonalitet raskere ut, men det gir også [færre
bugs](https://www.amazon.com/Accelerate-Software-Performing-Technology-Organizations/dp/1942788339) -
selv om det kan føles kontraintuitivt.

Vi har altså ingen feature branches, og ikke noe staging-miljø. Så hvordan
får vi det til funke når vi skal lage omfattende funksjonalitet?

Kort fortalt gjemmer vi ting til de er klare. La meg fortelle om tre ganske
greie løsninger.

## 1. Kronsteiner

I sitt foredrag [Software G Forces: The Effects of
Acceleration](https://www.youtube.com/watch?v=KIkUWG5ACFY) myntet den eminente
Kent Beck uttrykket "Keystoning".

[På norsk](https://no.wikipedia.org/wiki/Kronestein) heter det kronstein,
toppstein eller sluttstein. Det er den øverste, miderste steinen i en steinbue,
som settes på til sist og stabiliserer strukturen. Hvis du har bygget igloo noen
gang, så kjenner du til det.

Trikset her er altså å tilgjengeliggjøre funksjonaliteten i brukergrensesnittet først
når alt annet er ferdig. Ofte ender vi opp med å lage ferdig så godt som hele
UI-et -- bare untatt lenker og knapper som tar deg til nye sider.

Og det er alt. Det holder lenge. Trenger ikke noe spesielt system. Trenger ikke
være vanskelig.

## 2. Konfigurasjon

Andre ganger er den nye funksjonaliteten mer inngripende i UI-et, og lar seg ikke så
lett utvikle i isolasjon.

Det enkleste da er å skru funksjonaliteten av og på med konfigurasjon av app-en. Skrudd
av i prod, på i dev.

Et veldig artig ekstra-triks her er å la funksjonaliteten være helt skrudd på i
klienten, men bare hvis den får dataene som hører med fra serveren. Da kan man
senere skru på funksjonaliteten uten at folk trenger oppdatere klientene sine.

Igjen, veldig lite systemer som trengs.

## 3. Funksjonelle brytere

En sjelden gang har man behov for å rulle ut funksjonalitet til et subsett av
brukere. Her er det lett å starte unødig komplisert. Faktisk er det en [norsk
startup](https://www.getunleash.io) som lever av å selge enterprise-nivå
funksjonelle brytere (feature flags). Fett det! Sikkert også nyttig for mange.
Vi heier på dem. Men du, ikke *start* der - det er stor sannsynlighet for at [du
ikke kommer til å trenge
det](https://en.wikipedia.org/wiki/You_aren%27t_gonna_need_it).

Begynn med kronsteiner, og eskaler til konfigurasjon. Etter mange år med denne
arbeidsformen kan jeg telle på fingrene til en svært uheldig tømmerhogger antall
ganger vi har trengt noe mer enn det.
