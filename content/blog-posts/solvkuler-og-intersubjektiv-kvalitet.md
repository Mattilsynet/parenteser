:page/title Sølvkuler i bruk
:blog-post/authors [{:person/id :person/teodor} {:person/id :person/magnar}]
:blog-post/published #time/ldt "2024-11-18T09:00:00"
:blog-post/description

:open-graph/description

:blog-post/body

<div style="background-color: #052024; color: #f8fdfe; padding: 1em">
<p>
Jeg tror at god kode finnes.
Jeg antok at andre utviklere tenkte det samme.
Er det ikke derfor vi liker utvikling, at vi deler en søken etter bedre kode?
Jeg ble mildt sjokkert da jeg fikk tilbake svar om at "det bare er smak".
<p>
<p>
Å påstå at objektivt god kode ikke finnes slår meg som ufattelig pessemistisk.
Da kan vi aldri komme lenger!
Det eneste vi gjør er å bevege oss fra min smak, til din smak, til noen andres smak.
</p>
<p>
Da kollapser søkenen etter bedre kode til politisk pratmakeri.
"Bedre" finnes ikke, det eneste som står igjen er hvem av du og jeg som skal få sin vilje til å skje.
</p>
</div>

<br>

<div style="color: #052024; background-color: rgba(0,0,0,0.05); padding: 1em;">
<p>
Det minner meg om Eliyahu Goldratt i boka <a href="https://www.amazon.com/Beyond-Goal-Eliyahu-Goldratt-Constraints/dp/1596590238">Beyond the
Goal</a>.
Han har veldig lite til overs for uttrykket "There are no silver bullets".
Uttrykket forteller oss at det ikke finnes enkle løsninger, at alle forbedringer
vi kan strebe etter er inkrementelle. Det er ment å være realistisk. Og det er
bra, det. Vi skal være realistiske — alt har sine trade-offs — men det betyr
ikke at alt er like bra. Det finnes faktisk bedre måter. Selv peker Goldratt
på sin <a href="https://en.wikipedia.org/wiki/Theory_of_constraints">Theory of
Constraints</a> som en "stor samling med sølvkuler".
</p>
<p>
Etter at jeg hadde lest <a
href="https://en.wikipedia.org/wiki/The_Pragmatic_Programmer">The Pragmatic
Programmer</a> så ble jeg inspirert til å lære meg ett programmeringsspråk i
året. Det gjorde jeg hvert år, til jeg møtte Clojure. Det var ikke en
endestasjon for min læring, men det var en endestasjon for min søken etter et
verktøy som virkelig passet med hodet mitt. Det er ingen overdrivelse å si at
det data-drevne, funksjonelle språket med alle parentesene var min sølvkule.
</p>
</div>

<br>

<!--
Steg 3, første forsøk.
Jeg ble ikke helt fornøyd: for abstrakt, for svevende, tror vi mister folk.
Jeg prøver på nytt under.
<div style="background-color: #052024; color: #f8fdfe; padding: 1em">
<p>
Men er Clojure <em>objektivt</em> bedre enn andre programmeringsspråk?
Det er et standpunkt jeg ikke ønsker å ta!
<p>
<ul>
<li>Det finnes typer programmering jeg ikke kjenner gode Clojure-løsninger.
Grafikkprogrammering og tallknusing er to eksempler.</li>
<li>Selv om jeg ikke hadde kjent <em>noen</em> eksempler der Clojure ikke hadde
vært best, hadde ikke det vært nok til å si at Clojure er <em>objektivt</em>
bedre enn andre alternativer!</li>
</ul>
<p>
Kan vi komme videre ved å spørre “i hvilken kontekst?” og “for hvem?”?
Kanskje sølvkuler kun finnes for en kontekst.
Hvis vi angriper denne konteksten med denne sølvkulen, forsvinner nærmest problemet.
</p>
<p>
</p>
</div>
-->

<!-- Steg 3, andre forsøk!
OPPLEVD KVALITET.
Tror dette avsnittet både kan og bør trimmes litt ned, men er forholdsvis fornøyd med kjernen.
-->
<div style="background-color: #052024; color: #f8fdfe; padding: 1em">
<p>Når én person har funnet en sølvkule, hvordan går vi videre?
En sølvkule er opplevd som en god løsning til et problem, men <em>for hvem</em>?
</p>
<p>Vi som jobber med programmering sier ofte at vi jobber med <em>systemer</em>.
Men hvordan setter vi systemgrensene?
Er koden vår systemet?

Rich Hickey sier at koden er en for snever systemdefinisjon i <a style="color: #cdff92"
href="https://www.youtube.com/watch?v=ROor6_NGIWU">The Language of the
System</a>: Rich vil ha med databasen og køer.

Jessica Kerr argumenterer for en bredere systemdefinisjon i
<a style="color: #cdff92" href="https://jessitron.com/2018/04/15/the-origins-of-opera-and-the-future-of-programming/">
The Origins of Opera and the Future of Programming</a>: hun vil ha med seg <em>folk</em>.
Hvem kjenner systemet?
Hvem kjenner prinsippene som systemet er bygget etter?
Hvem kjenner bruken av systemet?
</p>
<p>🤔</p>
<p>Kanskje det å finne sølvkuler ikke er hele jobben?
Si at jeg har funnet en sølvkule, og kanskje til og med fått inn en fulltreffer på en vampyr.
Er jobben ferdig der?
Eller bør jeg ta med meg vennene mine på skytebanen og jobbe litt mer med hvordan vi skyter disse sølvkulene?
Hva tenker du, Magnar, hva bør jeg gjøre hvis jeg finner en sølvkule?</p>
</div>

<br>

<div style="color: #052024; background-color: rgba(0,0,0,0.05); padding: 1em;">
<p>
Her er jeg ikke så preskriptiv, kjenner jeg. For å sitere moren min: "<em>Bør</em> er en
byrde". Men jeg kan si hva <em>jeg</em> gjorde: Jeg satte meg ned og begynte å lage ting,
og koste meg glugg i hjel. Det var jo en opplevelse, å finne et språk som var så
godt alignet med mitt eget hode. Jeg lærte meg så mye jeg kunne, med stor begeistring.
</p>
<p>
Jeg startet med å bygge ny motor til hobbyprosjektet mitt — <a
href="https://www.adventur.no">Adventur Delux</a> — og tok språket med på jobb når
jeg hadde litt mer erfaring å by på.
</p>
<p>
Du kan si at jeg brukte sølvkula mi til å levere resultater.
</p>
<p>
Det er klart, jeg hadde jo også lyst til å dele oppdagelsen med andre. Jeg tok
med meg videoene til Rich og viste til de andre utviklere i lunsjen på jobben.
Jeg husker godt opplevelsen av å vise <a
href="https://www.youtube.com/watch?v=SxdOUGdseq4">Simple Made Easy</a> til en
forsamling veldig erfarne utviklere. Det var langt fra alle som ble like
begeistret som meg. Men det er greit det. Jeg fant et par andre som ville være
med, og det var nok.
</p>
<p>
Nå over et tiår senere, så endte jeg opp med to sidestilte tilnærminger:
</p>
<ul>
<li><strong>Tenk på meg sjæl:</strong> Christian og jeg søkte på jobb her hos
Mattilsynet under forutsetning av at vi fikk jobbe sammen med de verktøyene vi
er effektive med og har glede av.</li>
<li><strong>Tenk på andre:</strong> Jeg har ikke lyst til å prakke ting på folk,
men jeg prøvd å vise frem hvorfor språket er kult med <a href="https://www.zombieclj.no">videoserier</a>
og <a href="/foredrag/">foredrag</a>.
</ul>
<p>
Så jeg dodget unna spørsmålet om hva man <em>bør</em> gjøre, og svarte heller
hva jeg gjorde selv. Det har jo fungert fint for meg, i det minste. Har du selv
noen tanker om saken, Teodor?
</p>
</div>
