:page/title Kopier, kopier, så langt øyet kan se
:blog-post/author {:person/id :person/sigmund}
:blog-post/published #time/ldt "2025-04-11T08:00:00"
:blog-post/tags [:funksjonell-programmering]
:blog-post/description

Når du skal lage uforanderlige data, kan det være lurt å tenke litt først.

:blog-post/body

Forrige uke oppdaget vi et problem der vi manglet noe data fra et av systemene
vi jobber med å erstatte. Omtrent samtidig som vi hadde nøstet opp i dette,
publiserte Magnar [sitt innlegg om FKIS og uforanderlige
data](/to-bestevenner/). Modellen vi hadde sett på i det gamle systemet, var
stort sett uforanderlig, men løsningen for å få til dette var ikke spesielt god
sett med funksjonelle programmeringsøyne. Alt er bare kopiert hver gang det er
en endring.

Vi manglet riktignok bare en kolonne fra databasen i dette systemet, og den fant
vi ganske fort, men vi ville jo gjerne forstå verdensbildet i modellen. Hvorfor
var den sånn?

Både vårt system og det gamle sendte ut fire "vedtak". Et pålegg og en
påfølgende lukking av pålegget, som er en fullstendig kopi med omtrent to
endrede felter og en referanse til det opprinnelige pålegget. I tillegg hadde vi
et varsel om tvangsmulkt og en påfølgende lukking, omtrent akkurat det samme som
pålegget:

<img src="/images/vedtak.svg"
    alt="Fire ellipser med tekstene pålegg, lukking av pålegg, varsel om
    tvangsmulkt og lukking av tvangsmulkt. Det går piler merket med grunnlag opp
    til pålegget og varselet fra de tilhørende lukkingen.">

Det gamle systemet derimot koblet varselet om tvangsmulkt til pålegget som
grunnlag:

<img src="/images/vedtak-synk-mats.svg"
    alt="Fire ellipser med tekstene pålegg, lukking av pålegg, varsel om
    tvangsmulkt og lukking av tvangsmulkt. Det går piler merket med grunnlag opp
    til pålegget og varselet fra de tilhørende lukkingen. Det går også en pil
    fra varselet til pålegget merket med grunnlag.">

Da vi så i databasen, så vi en litt annen modell. Der var det en enkel liste
koblet fra pålegget til lukkingen, og fra varselet til sin lukking. I tillegg
fantes det en ekstra kobling, fra pålegget til varselet, men også fra lukkingen
av pålegget til lukkingen av tvangsmulktvarselet.

<img src="/images/vedtak-mats-modell.svg"
    alt="Fire ellipser med tekstene pålegg, lukking av pålegg, varsel om
    tvangsmulkt og lukking av tvangsmulkt. Det går piler merket med
    vedtaksreferanse fra pålegget til sin lukking og fra varselet til sin
    lukking. I tillegg går det piler fra pålegget til varselet og lukkingen av
    pålegget til lukkingen av tvangsmulkten merket med tvangsmulktvedtaksreferanse.">

La dette synke inn mens vi ser raskt på hvordan vi kan lage uforanderlige data.

## Kopiering ved skriving

Kopiering ved skriving er den mest grunnleggende løsningen på uforanderlige
data. Hver gang det skjer en endring, kopierer du dataene og har en ny ting.
Fine greier:

```java
String a = "abc";
String b = a + "def";
System.out.println(a); // abc
System.out.println(b); // abcdef
```

Her lever både `a` og `b` videre i beste velgående. Men hva om det du skal lage
har et titalls attributter? Da er det kjedelig om alle dataene skulle bli
kopiert når du bare endrer ett felt. Heldigvis har vi jo pekere, så når du
lager en kopi av noe som består av ti strenger, så er det bare den endrede
strengen som tar noe særlig plass.

```java
String a = "abc";
String b = "a" + "b" + "c";
System.out.println(a == b); // false
System.out.println(a.equals(b)) // true

class Foo {
    String a;
    String b;

    Foo(String a, String b) {
        this.a = a;
        this.b = b;
    }
}

Foo x = new Foo("abc", "def");
Foo y = new Foo(x.a, "ghi");

System.out.println(x.a == y.a); // true
```

I relasjonsdatabaser finnes derimot ikke pekere på samme måte, så her blir
alle dataene kopiert med mindre man bygger en modell som oppnår det samme selv.

## Strukturell deling

Vi vil altså unngå å kopiere absolutt alt hver gang vi legger til noe, fjerner
noe eller endrer noe. Da benytter vi gjerne strukturell deling. Et enkelt
eksempel er en LIFO-kø (sist inn, først ut):

```js
const kø = {value: "C", next: {value: "B", next: {value: "A"}}};
const køTo = {value: "D", next: kø};
const køTre = {value: "Z", next: kø};
```

<img src="/images/fifo-strukturell-deling.svg"
    alt="En graf med tre noder i rødt A, B og C, der C peker på B som peker på A. Under
    vises to like grafer med hver sin nye node D og Z, der disse peker på C.
    Fargen på A, B og C er fortsatt rød, mens D er grønn og Z er blå.">

I eksemplet over deler både `køTo` og `køTre` innholdet i `kø`. Dette gjør at vi
ikke trenger å kopiere hele køen når vi skal legge til et nytt element.
Strukturell deling kan være litt vanskeligere å få til om du skal få til god
ytelse med litt mer komplekse datastrukturer, og da må man kanskje tenke litt.
Hvis du har lyst på noen verktøy for hvordan du kan tenke om uforanderlige
datastrukturer, vil jeg anbefale Okasakis [*Purely Functional Data
Structures*](https://www.cs.cmu.edu/~rwh/students/okasaki.pdf). Det er mulig jeg
er rar, men jeg synes denne boka er ganske leselig.

## Hva er et vedtak?

Så tilbake til denne datamodellen vi ikke hadde kopiert absolutt alt fra til
vårt eget system. Denne tabellen, `Vedtak`, skulle gjennom sin uforanderlighet
representere alle ting som skjedde i forbindelse med et vedtak. Det første som
gjøres, før man kan fatte et vedtak, er at man må varsle om at det kan bli
fattet et vedtak. Det hadde jo vært litt kjedelig om du bare fikk bøter uten å
bli varslet om at du hadde gjort noe feil, som kunne føre til bøter om det ikke
ble rettet opp.

Så dette er typisk det første leddet i en kjede med "Vedtak", et "Vedtak" med
typen "varsel om vedtak". Det er ikke helt uvanlig at det gis en utvidelse av
fristen, så da følges det kanskje opp av et nytt varsel, der det eneste som er
endret er en fremmednøkkel til en frist og en dato for når dette nye "vedtaket"
fant sted. Hvis et vedtak lukkes eller fattes, vil det også gi en liknende kopi
med et par endrede felter.

Jeg vil påstå at denne modellen er en fattig utgave av uforanderlige data. Den
stammer fra en tanke om at det finnes et vedtak der vedtaket endrer seg over
tid, og for å fange opp dette kan vi jo bare kopiere alt hver gang det skjer
noe. Henger dette egentlig sammen med det som skjer i virkeligheten? Hva er det
som fører til "endringer" i vedtaket? Kanskje en slags hendelse?

Hva om de dataene som normalt endrer seg levde i en egen tabell? Kanskje
Vedtakshendelse? Jeg vil påstå at "opphev vedtak etter medhold i klage"
(KLAGE\_OPPHEVE\_VEDTAK), er en slags hendelse knyttet til et vedtak og ikke en
type vedtak.

Denne modellen ville kanskje ikke dekket 100% av tilfellene for endringer, men
den ville dekket normal saksgang. Kanskje hadde det vært behov for kopiering
hvis det var andre typer endringer utenfor det normale løpet. Så før du bare tar
den enkle løsningen og kopierer alt hele tiden, tenk gjennom modellen din.

## PS. Semi-uforanderlige vedtak

Jeg nevnte at vedtakene ikke var helt uforanderlige. Det er nemlig slik at de var
lagd som FIFO-lister. Altså, det første "vedtaket" hadde en referanse framover
til endringen. Så hver gang det kom en endring, måtte det forrige vedtaket
oppdateres. Dette var også grunnen til at "tvangsmulkten" som var et vedtak,
ikke ble pekt på av "pålegget" som var roten til dette løpet med vedtak. Den
hadde en helt egen referanse i pålegget bare for tvangsmulktvedtak.
