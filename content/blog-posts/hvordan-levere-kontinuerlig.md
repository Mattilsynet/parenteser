:page/title Hvordan levere kontinuerlig?
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-09-10T09:00:00"
:blog-post/tags [:samarbeid :metodikk]
:blog-post/series {:series/id :foredrag}
:open-graph/image "/images/javazone-2024-christian.png"
:blog-post/description

På årets JavaZone holdt jeg et innlegg om hvordan vi får til å levere
kontinuerlig. Fokuset var på de små detaljene. Jeg viste hvordan vi bryter opp
arbeidet sånn at vi kan jobbe rett på main og rett i prod, enten vi gjør litt
refaktorering eller bygger en helt ny innloggingsløsning.

:open-graph/description

Se video fra mitt foredrag på JavaZone 2024 der jeg snakker om hvordan vi
praktisk innretter oss for å kunne levere kontinuerlig.

:blog-post/body

[DORA-initiativet](https://dora.dev/) forsker på effekten av hvordan vi
strukturerer arbeidet vårt. De finner stadig mer bevis på at [kontinuerlig
integrasjon](/kontinuerlig-integrasjon/) (ja, det betyr [ingen pull
requests](/pull-requests/)) og hyppige leveranser er nøkkelen til suksess.

På JavaZone holdt jeg et praktisk rettet foredrag som fokuserte på _hvordan_ vi
får til det. Ta en titt og se konkret hvordan vi jobber med kode for å høste
disse gevinstene.

<div class="video-responsive">
  <iframe class="video-responsive-item" src="https://player.vimeo.com/video/1006253754?h=0084e31028&color=ff9933&portrait=0" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>
</div>

Foredraget er ikke overdrevent teknisk og passer for alle som driver med
produktutvikling. Hvis du vil vite mer om forskningen bak så anbefaler jeg [Line
Moseng sitt glimrende innlegg om akkurat
det](https://2024.javazone.no/program/67548ebd-21cb-495f-a21a-5432e95757d4).
