:page/title Kan jeg by deg på en nydelig nebbdyromelett?
:blog-post/published #time/ldt "2023-12-05T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/tags [:modellering]
:blog-post/description

:open-graph/description

:blog-post/body

Når vi modellerer data er det fristende å gå for en hierarkisk modell. Dette har
vi holdt på med siden tidenes morgen -- ihvertfall siden vi starta med å
[dokumentere alt liv på jorda](https://en.wikipedia.org/wiki/Taxonomic_rank).
Men er hierarkier en god måte å organisere informasjon på? Kanskje når man
organiserer papir, som bare kan ligge på ett fysisk sted, men kanskje ikke i
like stor grad når man organiserer bits og bytes som lett kan representeres på
mange steder på en gang.

## Fordelene med et hierarki

Hierarkier er enkle å forholde seg til. All informasjon ligger kun på én plass,
og vi kan greit finne informasjon ved å gjøre et slags manuelt [binært
søk](https://en.wikipedia.org/wiki/Binary_search_algorithm). Mange opplever
dette som en god og strukturert måte å organisere informasjon på.

Hierarkier passer også svært godt med de innebyggede begrepene i mange
tradisjonelle programmeringsspråk. Java, Kotlin og lignende språk har
klassehierarkier og arv som er veldig fristende å bruke til å representere
informasjonshierarkier.

## Begrensningene i et hierarki

Tenk deg for eksempel at du blar gjennom en [digital
matvaretabell](https://www.matvaretabellen.no). Du velger kategorien "drikke",
men plutselig er melken borte. Hvorfor? Fordi den er plassert under
"meieriprodukter" i hierarkiet. Så sitter du der da, tørst og forvirret, og
lurer på hvor i all verden melken tok veien.



## Lagring og visning


Ikke gå i hierarkifella!

Dataene dine ser kanskje hierarkiske ut nå, men bare vent. Snart finner du din
nebbdyromelett, og da kommer du til å angre på at du ikke gikk for en mer
fleksibel modell.
