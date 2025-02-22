:page/title Terminal kos
:blog-post/tags [:terminal]
:blog-post/author {person/id :person/mathias}
:blog-post/published #time/ldt "2025-02-25T09:00:00"
:blog-post/description

Jeg holder på med å utvikle et spill — et spill i terminalen. I denne prosessen
har jeg lært litt om hvordan man temmer terminalen til å oppføre seg rett.

:blog-post/body

[<img src="/images/ungandr.gif" style="max-width: 100%" alt="Ungandr">](https://github.com/boosja/ungandr)

Jeg holder på med å utvikle et spill — et spill i terminalen. I denne prosessen
har jeg lært litt om hvordan man temmer terminalen til å oppføre seg rett.

## Up the ANSI

Først ut er [ANSI escape
codes](https://en.wikipedia.org/wiki/ANSI_escape_code#Examples). Disse kodene
kan brukes til å manipulere teksten og markørens plass i terminalen. Om
du vil fargelegge noe tekst rødt kan du prefikse teksten med `\033[31m`. Prøv
f.eks. å kjøre denne i din foretrukne terminal:

```
$ echo "\033[31mDenne teksten er rød"
```

- `\033` starter escape sekvensen
- `[..m` forteller terminalen at du vil farge teksten som kommer etter (litt
  forenklet)
- `31` er fargekoden for rød

`\033[0m` nulstiller fargen tilbake til originalen. Om du vil ha det ekstra
gøy kan du prøve denne:

```
echo "Det \033[31msmalt\! \033[34mBukken\033[0m stupte \033[32mbums\033[0m i \033[35mbakken\033[0m."
```

> Fantastisk 🎨

For at spillet mitt skal funke bra må jeg finne en måte å tegne opp linjene på
nytt, for å unngå å printe de samme linjene under hverandre og søle til
scrollback-bufferet. Tenk på forskjellen i oppførsel mellom git og vim. Jeg
ønsker meg samme adferd som i vim — når du lukker programmet ser du ikke noen
spor etter å ha kjørt det.

ANSI escape codes lar deg også gjøre nettopp dette! 🎉

`\033[?1049h` åpner et alternativt buffer – akkurat det jeg trenger, og
`\033[?1049l` tar deg tilbake til det vanlige bufferet. Helmaks! 👌

> Men sa du ikke at du må tegne opp linjene på nytt? Dette løser jo ikke
> problemet 🤨

Helt riktig!

For å rense allerede printet tekst, har vi flere alternativer. Det finnes
escape-koder for å bevege markøren og viske ut tekst relativt til den, men
sammen med det alternative bufferet er det klart enklest å rense hele skjermen
med `\033[H\033[2J`.

## Interager, da vel!

Vanlig oppførsel i terminalen er å lese input på `<ENTER>`. Du må skrive inn
passord — og det er først når du trykker `<ENTER>` at inputtet blir sendt til
programmet.

Dette kommer ikke til å funke 🤔 Hvordan skal man få slangen til å bekjempe,
ødelegge og rasere uten raske tastetrykk?

`stty` til unnsetning 💪

`stty` er en kommando for å endre oppførselen til terminalen. Jeg vil særlig
fremheve to flagg: `-echo` og `-icanon` (lokale moduser). <small>Takk til
[Teodor](https://play.teod.eu/) og
[bbslideshow](https://github.com/teodorlu/bbslideshow) for å hjelpe meg på
vei.</small>

- `-echo` gjør at tastetrykk ikke blir skrevet til bufferet. Så når jeg trykker
  `s` vil det ikke komme en `s` i terminalen med dette flagget aktivert. Sagt
  med andre ord fjerner du terminalens echo-evne.
- `-icanon` endrer måten terminalen leser input. I stedet for å lese input linje
  for linje, blir inputtet lest tegn for tegn.

`stty -icanon -echo` gir oss da de perfekte forholdene for et spill i
terminalen. Vi får ikke unødvendige tegn rundt omkring og vi trenger ikke å
trykke `<ENTER>` hver gang vi ønsker å gjøre noe. For å gjenopprette terminalens
daglige kår er det bare å kjøre samme kommando uten strekene: `stty icanon
echo`.

I spillet mitt har jeg planer om også å bruke ctrl-tastebindinger. Men også her
kommer jeg i veien for terminalens normale oppførsel. Om jeg vil bruke
`CTRL-c` f.eks. vil det ha ganske så katastrofale følger for spillet mitt.
Spillet ville bli fortalt om å lukke porten og si farvel til oss dødelige.

Dette må også tas hånd om ✋

Her kan vi også bruke `stty` til å rebinde tastebindingene. Vi kan rebinde
`CTRL-c` med `stty intr ''`. Dette vil i praksis deaktivere muligheten for å
avbryte en prosess – ikke for sarte sjeler. Tilbakestill med `stty intr '^C'`.

Med `stty -e` får du listet opp alle flagg, kommandoer og bindings.

I spillet mitt kan jeg da kjøre:

```
$ stty stop '' discard '' intr '' susp '' lnext '' reprint '' werase '' dsusp '' eof '' status '' start '' kill ''
$ stty stop '^S' discard '^O' intr '^C' susp '^Z' lnext '^V' reprint '^R' werase '^W' dsusp '^Y' eof '^D' status '^T' start '^Q' kill '^U'
```

og forårsake så mye kaos jeg vil 💥❤️‍🔥😈 — og rydde pent opp etter meg igjen 😇.

> Yippie ‼ 🙌

## Følg instinktene dine 👀

Noe annet som skurrer for meg, er at markøren er synlig. Den har ikke
noen rolle i spillet mitt så da trenger man heller ikke beskue dens eksistens.

`tput` er kommandoen for dette 😁 `tput civis` skjuler markøren og `tput cnorm`
visualiserer den på nytt. **Tada!** 🎉

## Oppsummering

- `\033[..m` fargelegger teksten etter i den angitte fargen
- `\033[?1049h` går til alternativt buffer
- `\033[?1049l` bringer deg tilbake til det vanlige bufferet
- `\033[H\033[2J` renser hele skjermen for skittenskap
- `stty -icanon -echo` leser input tegn for tegn
- `stty icanon echo` leser input linje for linje
- `stty <ctrl> <binding>` rebind tastebindinger
- `tput civis` skjuler markøren
- `tput cnorm` bringer tilbake markøren

#### Hederlig omtale

`trap <action> <condition>`

`trap` lar deg innskyte et signal med ekstra kommandoer før det eksekveres eller
endre oppførselen fullstendig. F.eks. `trap "exiting..." SIGINT` vil printe ut
strengen `"exiting..."` før prosessen blir kansellert.

Man kan da bruke denne til f.eks. å rydde opp i volatile elementer før en prosess
avsluttes, eller innskyte kommandoer før et vilkårlig signal blir eksekvert.

### Takk for meg [🙇‍♂️](https://github.com/boosja/ungandr)
