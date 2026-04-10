:page/title Commit-meldinger med kategori
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2026-04-10T09:00:00"
:blog-post/tags [:metodikk]
:open-graph/image "/images/git.png"
:blog-post/description

Denne uka har gjorde vi en liten endring i hvordan vi skriver commit-meldinger
som ser ut til å gi oss en langt mer scanbar logg.

:blog-post/body

Denne uka har gjorde vi en liten endring i hvordan vi skriver commit-meldinger
som ser ut til å gi oss en langt mer scanbar logg. I den forbindelse har vi
dokumentert hvordan vi skriver commit-meldinger i en [ADR](/beslutninger/) som
du kan lese i sin helhet under.

## Vi skriver imperative commit-meldinger med en kategori

#### Kontekst

Fra starten av har commit-meldingene i Matnyttig blitt skrevet etter en fast
struktur (inspirert av, men ikke identisk med [commit message
rules](https://www.git-basics.com/docs/git-commit/commit-message-rules)) som
ikke er skrevet ned noe sted. Nye på teamet har i stor grad fulgt denne
strukturen til tross for at den ikke er dokumentert.

Systemet vårt er en monolitt og arbeidet foregår for tiden på flere fronter:

- Kontoret: verktøy for Mattilsynets inspektører
- Kjøkkenet: verktøy for serveringsbransjen
- Ikke-funksjonelt/støttefunksjonalitet: Arkivering, gebyr, osv
- Teknisk arbeid: Verktøy/ergonomi for utviklerne, testing osv

Utviklingstakten - og dermed antall commits - har økt den siste tiden.

Teamet committer løpende til `main`, så commits er ikke gruppert på noe annet
vis enn i tid.

Martin Solli på Clojure-slacken [gjorde oss
oppmerksomme](https://clojurians.slack.com/archives/C061XGG1W/p1773232670540839?thread_ts=1773230869.535409&cid=C061XGG1W)
på [Subject-First Commit
Messages](https://github.com/aaronjensen/software-development/blob/master/commit-messages.md)
av Aaron Jensen.

#### Beslutning

Vi skriver commit-meldinger med en ledende kategori, men beholder imperativ form
på meldingene. Kategorien er inspirert av subjektet i "Subject-First Commit
Messages".

Vi følger dermed disse reglene når vi skriver commit-meldinger:

**1. Start med en kategori etterfulgt av kolon**

Valg av kategori er ikke så lett. Målet med kategorien er at commit-loggen skal
være scanbar. Ofte vil ukens mål være gode kandidater. Emacs-oppsettet vårt har
støtte for å [foreslå tidligere brukte
kategorier](https://github.com/magnars/emacsd-reboot/commit/c1ecb8383f6b9e6dbb980d346e0335d45902872d)
ved å trykke `TAB`.

For tekniske småting kan vi bruke kategorier som "Rydding", "Testing", osv. Det
er også greit med enkelte commits uten kategori hvis det ikke er mulig å komme
opp med noe meningsfylt.

**2. Skriv en kort imperativ overskrift som forklarer effekten av endringen**

Fokuser på effekten av endringen (hva og hvorfor), ikke en beskrivelse av
diffen.

For noen trivielle endringer er det ikke så mye mer å si. Det er helt lov å
skrive "Rydding: Fjern en ubrukt require".

Overskriften skal være med stor forbokstav og uten punktum.

Husk at den korte meldingen nå er kontekstualisert av kategorien, og trenger
sannsynligvis ikke å gjenta den. Eksempel:

> Registrering: Vask telefonnummerne for visning i oppsummeringen

"I oppsummeringen" ville normalt sett vært for åpent. Hvilken oppsummering?
Hvor? I kontekst av "registrering" er det derimot ikke så mange kandidater.

**3. Begrens den første linja til 70 tegn så langt det er mulig**

En kort melding blir ofte mer presis og bidrar til en commit-logg som er lettere
å lese. Når det ikke er mulig å uttrykke seg meningsfylt innenfor denne grensa,
er det helt lov å tråkke litt over.

**4. Skriv en lengre forklaring om nødvendig**

En commit-melding er et glimrende sted å fange kontekst. Se [Skriv bedre
commits](https://parenteser.mattilsynet.io/bedre-commits/) for noen forslag til
ting man kan skrive i selve meldingen.

**5. Beskjeder til datamaskinen legges nederst i meldingen**

Tekniske instrukser ala `[skip ci]` og `Co-Authored-By` legges helt nederst i
meldingen. Rekkefølgen på disse er ikke viktig.

#### Konsekvenser

Vi får en commit-logg som er lettere å scanne, og hvor det er lettere å se
hvilke commits som "hører sammen".

Vi får dokumentert dagens forventninger til commit-meldinger.

#### Alternativer

Det ble ikke aktivt jaktet på flere alternativer annet enn dagens praksis som er
en slags uskreven regel.

Conventional Commits er tidligere forkastet fordi det bruker den mest verdifulle
delen av meldingen til maskinlesbare instrukser.
