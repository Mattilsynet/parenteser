:page/title Følg dopamin-hitsene
:blog-post/tags [:metodikk :rewind]
:blog-post/author {:person/id :person/mathias}
:blog-post/published #time/ldt "2025-12-12T09:00:00"
:blog-post/description

Et blikk tilbake på året som var.

:blog-post/body

Nå har jeg snart jobbet på Team Servering i et helt år. Det har vært kjempegøy,
innholdsrikt og et mer stimulerende år har jeg aldri hatt. Hva jeg har lært i
arbeidet med Matnyttig, diverse fritidsprosjekter og editoren vår Emacs, er dét
jeg vil prøve å trekke frem nå. Mere presist: hvordan gjøre jobben vår enklere
og på samme tid mer stimulerende og ikke minst GØY!

## Små, hyppige commits

Den første biten er å holde commitsene små og committe de hyppig. Når du er
ferdig med en avgrenset liten bit av hva du vil få til og som kan stå for seg
selv: commit den ([gjerne
rett](https://parenteser.mattilsynet.io/hvordan-levere-kontinuerlig/) [på
master](https://parenteser.mattilsynet.io/om-kroner-og-flagg/)). Det kan være en
ny funksjon, en enkel linje eller sågar en skrivefeil på én karakter.

Hver commit du skriver vil gi deg en liten glede, <em>da er den biten ferdig og
ute av verden</em>, og du kan gi slipp på den. Som en TODO-liste, vil hver
commit være som et punkt huket av på arbeidslisten din.

<em>💉 Ensue dopamine-hit 💉</em>

## Rene funksjoner og tester

Det er ikke noe mer nydelig enn [rene
funksjoner](https://parenteser.mattilsynet.io/to-bestevenner/). Det skulle vært
data, da, men det er på siden av saken. En ren funksjon er pålitelig – med samme
input vil den gi nøyaktig samme output.

Den er derfor en perfekt kandidat å skrive tester for. Du trenger ikke mocke
ting eller bekymre deg om imperative greier, du har bare en fri, blå himmel på
en solskinsdag.

Start med en feilende test – og så fiks den! Det er ikke alltid man vet hvor det
er man skal, så start med en minimal test som bare tester det absolutt
nødvendige, bare for å komme i gang. [Gjør testen
grønn](https://parenteser.mattilsynet.io/lynraske-modultester-med-en-tastakombinasjon/)
og så utbygg testen lite grann, og gjør testen grønn igjen. Fortsett sånn inntil
funksjonen håndterer alt det den skal håndtere.

Når du er ferdig, vil du ha skrevet en eller flere tester og en funksjon som kan
stå tidens tann. Og hver gang du har gått fra en rød test til en grønn:

<em>💉 Ensue dopamine-hit 💉</em>

## Data er BRA!

Jeg føler det nesten har blitt
[mottoet](https://parenteser.mattilsynet.io/datadreven-frontend/)
[til](https://parenteser.mattilsynet.io/interpolering/)
[teamet](https://parenteser.mattilsynet.io/1ste-klasses-parametere/). Vi
<em>digger</em> data. Om det kan være data, da får det lov til å være data i
systemene våre. Matnyttig er bygd opp av egen-definerte, første-klasses
konsepter som i bunn er – drumroll – DATA! [Sider, feeds, kommandoer, effekter,
refiners, collectors](https://parenteser.mattilsynet.io/handfaste-konsepter/) og
mange fler er definert med data.
[Maskineriet](https://parenteser.mattilsynet.io/fk-is-101/)
[bak](https://parenteser.mattilsynet.io/fkis-jz/) får inn dataene, leser
dataene, prosesserer dataene og til sist utfører side-effekter basert på
dataene. Dette gjør det sykt behagelig å jobbe med. Det meste er rene funksjoner
som bare manipulerer med data, og når du skal lage en ny side, så skriver du
bare data. Oprette en ny feed? Skriv data og rene funksjoner. Alle de imperative
greiene som er vanskelige og snakker med verden utenfor er ferdig håndtert,
gjømt bort og bare skrevet én gang.

<em>💉 Ensue dopamine-hit 💉</em>

## Kontinuerlige forbedringer på verktøyene dine

Jeg skrev noen måneder tilbake om å være [ekspert på utviklerverktøyet
ditt](https://parenteser.mattilsynet.io/ekspert-paa-utviklerverktoy/).
[Dette](https://parenteser.mattilsynet.io/lyssabel/) er en bit av det, men det
er ikke bare det jeg mener her. I Matnyttig har vi mange første-klasses
konsepter (som beskrevet over), og en av de er feilhåndteringen vår. Her har vi
også gjort en jobb så vi fanger opp gode feilmeldinger og får det rapportert på
en god måte. Når det skjer en feil i systemet putter vi feilen på NATS og
rapporterer om feilen i Slack. I Slack-meldingen har vi en kodesnutt som vi kan
kopiere og evaluere hvor som helst i kodebasen vår, så lenge vi har en REPL
kjørende. Da får vi en full rapport med masse informasjon om feilen (SOM
[DATA](https://parenteser.mattilsynet.io/feilsoke-med-data/)!) inkl.
stack-tracen.

Så poenget her er: forbedre systemet ditt og verktøyene dine så du kan enklere
gjøre jobben din og raskere løse de problemene som kommer løpende.

Så når systemet ditt og verktøyene dine følger hva du møter i virkeligheten:

<em>💉 Ensue dopamine-hit 💉</em>

---

Om du er nysgjerrig på hva man får til når programmering har blitt så mye mer
stimulerende og gøy, så har jeg lagd en liste med highlights om hva jeg
har drevet med det siste året: [Massive Tanker i
2025](https://mathivethoughts.no/blog-posts/massive-tanker-2025/).
