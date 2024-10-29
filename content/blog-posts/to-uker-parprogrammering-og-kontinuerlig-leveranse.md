:page/title Parprogrammering og kontinuerlig leveranse: to uker i Mattilsynet
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2024-10-29T09:00:00"
:blog-post/tags [:samarbeid :metodikk :parprogrammering] ; kontinuerlig leveranse, parprogrammering, domeneproblemer
:blog-post/description

Hva gir det mening å fokusere på når man starter i en ny jobb?
Hva driver Øyvind, Christian og Magnar med fra dag til dag?
Jeg deler mine erfaringer fra mine første to uker.

:open-graph/description

Om de første to ukene i ny jobb som utvikler med parprogrammering og kontinuerlig leveranse.

:blog-post/body

Hva gir det mening å fokusere på når man starter i en ny jobb?
Hva driver Øyvind, Christian og Magnar med fra dag til dag?
Jeg deler mine erfaringer fra mine første to uker.

En bit av meg stritter imot.
"Nei! Det er ikke ferdig fordøyet! Jeg vil ha mer tid i tenkeboksen!"
Det er jobben til en annen bit av meg å si
"Ja, det er ikke ferdig fordøyd, men vi skal fremdeles skrive om det.
 Det har vi godt av.
 Ship it."
Mens man lærer er et godt tidspunkt å skrive.
Skriveprosessen gjør at det jeg lærer sitter bedre.
Kanskje blir det nyttig for andre også!
Teksten lar meg trekke fram ting jeg er interessert i, så kan andre diskutere og stille spørsmål.

Hvis du leser videre, får du mitt svar på følgende:

- Hvorfor finnes Team Mat i Mattilsynet? Hvilke mennesker er sentrale i jobben vår?
- Hvordan muliggjør parprogrammering og ukentlig fokus at vi kan levere kontinuerlig?

## Smilefjesinspektørene

Oppdraget til Team Mat er å sikre at maten du spiser på serveringssteder er trygg å spise.
Gjennom [Smilefjesordningen] besøker smilefjesinspektører flere tusen serveringssteder i året.

[Smilefjesordningen]: https://www.mattilsynet.no/mat-og-drikke/forbrukere/smilefjesordningen

På tre timer skal inspektøren:

- marsjere inn på kjøkkenet til et serveringssted
- kontrollere det som kan true matsikkerheten
- vurdere hva som må rettes
- og få med seg kjøkkensjefen på å rette problemene.

En krevende jobb!

Jeg var på Smilefjes-tilsyn med en inspektør (Hei, Tatiana!) på tirsdag forrige uke, og det var en super opplevelse.
Jeg vil trekke fram hvordan repetisjon sikrer at kjøkkensjefen vet hva som må rettes.

- Først går inspektøren gjennom punkter fra forrige tilsyn.
  Hva har blitt rettet opp?
  Hva står igjen?

- Når inspektøren finner et problem under inspeksjonsrunden, spør inspektøren kjøkkensjefen om rutiner.
  Ikke "Dette smørbrødet har ligget framme for lenge", men "Hva er rutinene deres for å sikre temperatur og tid smørbrødet ligger framme inntil smørbrødet blir solgt?"
  Spørsmål om rutiner gjør samtalen mer konstruktiv.
  Det egentlige problemet er ikke at man fant noe som er skittent -— problemet er hvordan man sikrer matsikkerhet hver dag.

- Etter inspeksjonsrunden skriver inspektøren smilefjesrapport mens inspektøren fremdeles er på stedet.
  Før rapporten sendes inn, går inspektøren gjennom rapporten med kjøkkensjefen punkt for punkt, og spør om kjøkkensjefen har spørsmål.

Når kjøkkensjefen til slutt får rapporten, er det liten tvil om hva rapporten betyr, og hvordan problemene kan fikses.

## Ukentlig fokus på Team Mat

Team Mat har strengere kontroll på work in progress enn alle andre team jeg har jobbet i.
Vi gjør én, smal ting hver uke.
På mandag setter vi målet for uka, og på fredag ser vi tilbake og spør oss selv hvordan det har gått.

Dette smalet fokuset hver uke løser en haug med utfordringer.
Vi vet hva de andre driver med, fordi vi jobber på samme ting.
Hvis jeg lager brukergrensesnitt og mangler data, løser vi det helst sammen, samme dag.

## Parprogrammering og kontinuerlig leveranse

Jeg diskuterte parprogrammering med en tidligere kollega for en uke siden.
Han spurte hvorfor det var vits i å parprogrammere når han kunne skrive koden raskere på egen hånd.

Å skrive koden er imidlertid ikke hele jobben.
Plutselig må noen andre ta i min kode, eller jeg må bruke andres kode.
Plutselig har jeg og en kollega løst samme problem på litt forskjellig vis hvert vårt sted i kodebasen.

Under parprogrammering skjer det flere ting enn skriving av kode.
Vi putter ikke bare tekst i en fil, vi innfører nye konsepter.
Er konseptene vi har laget i dag gode?
Skjønner andre hva vi mener?
Hvis jeg har puttet kode på master som Christian ikke skjønner bæret av, er ikke jobben ferdig.
Enten trenger Christian å få en forklaring, eller så trenger jeg å gjøre om på noe.
En annen måte å si at vi lager konsepter på, er å si at vi _lager teori_, som [Peter Naur beskrev i 1985][naur-1985].

[naur-1985]: https://pages.cs.wisc.edu/~remzi/Naur.pdf

Christian og Magnar praktiserer parprogrammering _og_ kontinuerlig leveranse, uten noen steg mellom.
Koden skrives, så går den i produksjon.

Ukentlig fokus, parprogrammering og kontinuerlig leveranse henger sammen.
Uten ukentlig fokus, ville terskelen for å diskutere hva koden gjør og bør gjøre blitt for høy.
At vi vet hva vi skal gjøre, og har jobbet gjennom "risikabel" kode med to personer foran skjermen gjør at vi tør å gå rett i produksjon.

## Endringsfart og kvalitet

Så, er denne måten å jobbe på noe bra?

Jeg tror det!

- Ukentlig fokus, parprogrammering og kontinuerlig leveranse unngår branching og work in progress (i mangel av norske begreper).
  Det sparer oss for en haug med jobb.

- Høyere endringsfart gjør det lettere å jobbe sammen på koden.
  Under en parprogrammeringsøkt for to uker siden manglet det en bit i maskineriet.
  Siden Magnar var med og så at biten manglet, kunne han fikse problemet.
  Først la vi til det vi fikk til i UI-et uten nytt maskineri.
  Så la vi til maskineri.
  Så la vi til mer UI, siden vi nå hadde maskineri til å støtte opp.
  Alt rett på master!

- Høyere endringsfart gjør det lettere å forbedre produktet.
  Vi har laget statistikk for inspektørene som viser resultater fra tidligere tilsyn.
  Da inspektørene fikk se statistikken, stilte de et spørsmål.
  Christian dyttet så ut en ny commit som svarte på spørsmålet.

Jeg kjenner at hvilke ting som går fort og sakte fremdeles er litt uvant for meg.
Men det er noe "slow is smooth, smooth is fast" over dette.
Pluss en ekte anerkjennelse av _folk_ i utviklingsprosessen.

Hvis du har lest helt hit, tusen takk for at du leste!
Jeg har skrevet litt for egen nytte, og litt for å dele.
Har du kommentarer?
Jeg tror det er sunt å snakke om disse hvordan vi jobber, og at diskusjon kan gi gode ideer om framtidige tekster.

Hilsen Teodor
