:page/title Om å bygge sin egen lyssabel
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-03-19T09:00:00"
:blog-post/tags [:emacs]
:blog-post/description

Forleden dag ønsket jeg meg ny funksjonalitet i editoren min. Jeg fikk ikke
fullført CSS-klasser så greit som jeg ville -- ja, det var nesten på grensa til
kronglete. Heldigvis fantes det en løsning.

:blog-post/body

Forleden dag ønsket jeg meg ny funksjonalitet i editoren min. Jeg fikk ikke
fullført CSS-klasser så greit som jeg ville -- ja, det var nesten på grensa til
kronglete. La oss ta en rask titt.

Dette er "Hiccup":

```clj
[:div
  [:h1 "Hallo!"]
  [:p "Fint å se deg."]]
```

Som du ser så er det en HTML-representasjon med Clojure-datastrukturer. Veldig
praktisk, det rare navnet til tross.

Jeg kan slenge på et par CSS-klasser slik:

```clj
[:div {:class "foo bar"}
  [:h1 "Hallo!"]
  [:p "Fint å se deg."]]
```

Men det finnes også en mer hendig versjon:

```clj
[:div.foo.bar
  [:h1 "Hallo!"]
  [:p "Hyggelig å se deg."]]
```

Denne siste versjonen bruker jeg mye, men det er et problem: Sett fra språket
sin side så er `:div.foo.bar` bare et helt alminnelig keyword. Det er ingenting
spesielt med punktumene.

Editoren min kan fullføre keywords helt fint, men forstår ikke at `.foo` og
`.bar` burde fullføres hver for seg.

Kort fortalt må editoren min lære seg Hiccup.

## Lyssabelen

Så, hva gjør jeg nå? Setter jeg meg ned og skriver et langt leserbrev til
JetBrains? Krysser fingrene og håper det kommer i neste versjon av VS Code?

Niks!

Jeg bare fikser det sjøl.

Emacs er software i ordets rette forstand: myk og føyelig. Koden til Emacs
ligger ikke pakket ned og skjult -- tvert imot, den er godt dokumentert og
utvidbar. Alt av kode kan inspiseres og endres. Det er fascinerende. Når du
bruker Emacs og tilpasser den til dine egne behov over tid, så blir det litt som
å [bygge sin egen
lyssabel](https://www.infoq.com/presentations/Live-Programming/).

> "People talk about getting used to a new editor, but over time, it is
> precisely the opposite that should happen - the editor should get used to us."
>
> -- [Vivek Haldar](https://blog.vivekhaldar.com/post/31970017734/new-frontiers-in-text-editing)

## Plastisitet

Det er vanskelig å beskrive opplevelsen av å redigere sin egen editor mens man
bruker den selvsamme editoren. Hele greia endrer seg jo mens du holder på. For å
si det sånn, hvis du redefinerer `self-insert-command` så kan du bare glemme å
skrive noen flere tegn:

```elisp
(defun self-insert-command ()
  (interactive)
  (message "lol"))
```

Fra nå av står det bare "lol" i meldingsområdet når du prøver å skrive i
editoren.

Tenk deg det: Til og med kommandoen som setter inn tegnene du taster kan
modifiseres etter eget innfall.

Det er jo artig å kunne [brekke sine egne
verktøy](https://www.usenix.org/system/files/1311_05-08_mickens.pdf), men enda
artigere å lage nye.

## Å fullføre CSS-klasser

Det viser seg at Emacs har punkter for utvidelse stort sett overalt. Ett av dem
er `completion-at-point-functions`. Denne lista med funksjoner kalles når man
iverksetter autocompletion. ("at point" betyr "ved cursor-en")

Så da kan vi slenge sammen vår egen funksjon, og legge den til i denne lista.
For eksempel en funksjon som fullfører CSS-klasser i Hiccup.

Sånn her ble koden: (forklaring følger)

```elisp
(defun completion-at-point-for-hiccup-css-classes ()
  "Check if we are completing a hiccup keyword CSS class,
   and suggest all classes in the project."
  (when (and (eq major-mode 'clojure-mode) ;; 1
             (not (cider-in-string-p)) ;; 2
             (not (cider-in-comment-p)))
    (when-let ((s (thing-at-point 'symbol))) ;; 3
      (when (s-starts-with? ":" s) ;; 4
        (when-let ((last-dot-index (string-match-p "\\.[^\\.]+$" s))) ;; 5
          (let ((bounds (bounds-of-thing-at-point 'symbol)))
           (list
            (+ last-dot-index (car bounds)) ;; 6
            (cdr bounds) ;; 7
            (extract-css-class-names ;; 8
             (read-project-css-file-contents)))))))))
```

1. Sjekk at vi er i clojure-mode.
2. Sjekk at vi ikke er i en streng eller kommentarblokk.
3. Bruk `(thing-at-point 'symbol)` for å plukke ut symbolet der cursor-en er.
4. Sjekk om symbolet starter med `:`.
5. Finn posisjonen til siste punktum med `string-match-p` og en hårete regex.

Hvis alt dette slår til, så kan vi forsøke oss på en fullføring. Da løper vi
av gårde ut i prosjektet og finner alle klassedefinisjonene i css-filer.

Funksjonen vår svarer enten med `nil` (ingenting å fullføre) eller med en liste
av:

6. Start-posisjon for delen av teksten vi vil fullføre (fra punktumet og utover),
7. slutt-posisjon for det samme,
8. og en liste med forslag

Med funksjonen vår på plass, så kan vi koble den på:

```elisp
(add-to-list 'completion-at-point-functions
             #'completion-at-point-for-hiccup-css-classes)
```

Og med det kan jeg nå fullføre CSS-klasser i hiccup. Herlig!

## Avslutningsvis

Jeg forventer ikke at du skal sette deg ned og forstå alt som står i koden over,
men poenget er dette: Det tok mindre enn en dag fra jeg ønsket meg en ny
feature, til jeg hadde den. Et par timer på en flytur, faktisk, var alt som
skulle til.

Det sies at utviklere og smeder er de eneste som lager sine egne verktøy. Det er
et privilegium som flere utviklere burde benytte seg av.

Å kunne bygge ut editoren sin ved behov er på kort sikt en artig øvelse, men det
er først på lang sikt de store gevinstene kommer. Bare tenk hvor godt verktøyet
passer i hånda når du har gjort små forbedringer på det i årevis.
