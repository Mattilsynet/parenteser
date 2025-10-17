:page/title Reis i tid med Git-worktrees for å levere kontinuerlig
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-05-16T09:00:00"
:blog-post/tags [:testing :metodikk]
:blog-post/description

For å få til kontinuerlig leveranse i Team Servering, vil vi minimere herk knyttet til hver leveranse.
Det krever at vi tar tak i det som lugger når vi leverer.

Dagens tekst beskriver et steg som lugget, og hvordan git-worktree var del av løsningen.

:open-graph/description

Få til mer kontinuerlig leveranse ved å kjøre testene mot kliss lik kode du har sjekket inn i Git.

:blog-post/body

For å få til kontinuerlig leveranse i Team Servering, vil vi minimere herk knyttet til hver leveranse.
Det krever at vi tar tak i det som lugger når vi leverer.

Dagens tekst beskriver et steg som lugget, og hvordan [git-worktree] var del av løsningen.

## Hyppige leveranser for effektivt arbeid og fornøyde utviklere

På Team Servering har vi ikke noen avsjekk med andre før en utviklers kode går i produksjon.
Jeg gjør følgende når jeg skriver kode som skal ut i produksjon:

1. Skriver koden min, og kode for å sjekke om koden funker
2. Kjører testene
3. Committer og pusher
4. CI-serveren kjører testene på nytt
5. CI-serveren prodsetter ny kode hvis testene er grønne.

Christian går i dybden på hvorfor og hvordan vi gjør dette i [Hvordan levere kontinuerlig](/hvordan-levere-kontinuerlig/).

## Trøbbel i paradis når ikke alle endringer er sjekket inn

Men prosessen over er ikke vanntett!
Jeg lager krøll når jeg gjør følgende:

1. Testene går gjennom på min maskin! 🟢
2. Jeg committer og pusher, men glemmer å legge til en fil.
5. Testene på CI er røde! 🔴

Hvis jeg har skikkelig uflaks, gjør jeg dette rett før jeg går for dagen.
Resten av folka trekker ned endringene mine, og nå får ikke de gjort jobben sin heller!

Au!
Hva gikk galt her?

## Diagnose: jeg og CI testet forskjellig kode!

Først testet jeg koden jeg hadde på disk.
Så testet CI-serveren koden per siste commit.
Men koden jeg hadde på disk var forskjellig fra koden per siste commit.
Jeg og CI-serveren testet forskjellig kode!

## Tiltak: kjør testene lokalt slik CI ser koden

Dette problemet kunne vært taklet på forskjellige måter.
Jeg fulgte to prinsipper:

1. Feedback er bedre med kortere forsinkelse
2. Tilby utvikleren mer informasjon heller enn å innføre begrensninger

Resultatet av tiltaket er en ny Makefile-kommando: `make test-latest-commit`.

Vi kan nå kjøre testene slik CI vil se koden (i stedet for å kjøre testene slik vi ser koden akkurat nå) før vi pusher.
Problem løst, wohoo!
🕺

## Hvordan bygger du din egen `make test-latest-commit`?

`make test-latest-commit` gjør følgende steg:

1. Identifiser siste commit med `git rev-parse HEAD`
2. Lag en midlertidig katalog
3. Sjekk ut siste commit til katalogen med `git worktree add`
4. Kjør testene i ny utsjekket katalog, og spar på returkoden
5. Fjern worktree med `git worktree remove`
6. Returner returkoden

Dette kan du gjøre i de fleste språk/kjøretidsmiljøer.
For å holde testkjøringene så raske som mulig, er det fint å bruke et språk som starter raskt.
Bash, Javascript, Python og Babashka er gode kandidater.

Takk til [Kevin] som tipset meg om [git-worktree] for mange år siden.
En helt super Git-kommando som skinner sterkere når du må jobbe med (for) mange ting samtidig.

[Kevin]: https://kevin.stravers.net/
[git-worktree]: https://git-scm.com/docs/git-worktree

## Appendix: gi meg koden!!!

Vår løsning er skrevet i Clojure-dialekten Babashka, som er godt egnet for scripting.
Babashka starter raskt, og vi har gode biblioteker for å jobbe med filsystemet ([babashka/fs]) og prosesser ([babashka/process]).

[babashka/fs]: https://github.com/babashka/fs
[babashka/process]: https://github.com/babashka/process

Siden jeg skriver, kan presentere koden i akkurat den rekkefølgen jeg ønsker.
Ha!

Først kommer testene.
Jeg liker tester som forklarer koden.

Vi starter med en wrapper for `git rev-parse`.
`rev-parse`-funksjonen vår tar inn mappen rev-parse skal kjøres i, og Git-revisjonen som skal "parses".

```clojure
;; bb/test/matnyttig/timemachine_test.clj
(ns matnyttig.timemachine-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing]]
            [matnyttig.timemachine :as timemachine]))

(deftest rev-parse
  (testing "short commit SHAs expand into long ones"
    (is (= (timemachine/rev-parse "." "4d252aef")
           "4d252aef804f31022042126fd29f3cc41f3d126d")))

  (testing "refs like HEAD and branch names are supported"
    (is (= (count (timemachine/rev-parse "." "4d252aef"))
           (count (timemachine/rev-parse "." "HEAD"))
           (count (timemachine/rev-parse "." "main")))))
  )
```

Fy, søren, jeg liker å forklare kode med tester!

Vi trenger wrappere for `git worktree add` og `git worktree remove`.

- `worktree add` tar repo-katalogen, ny katalog for nytt worktree og en Git-ref.
- `worktree remove` tar repo-katalogen og worktree-katalogen.

`worktree-add` og `worktree-remove` testes sammen fordi `remove` rydder opp etter `add`.

```clojure
(deftest worktree-add-remove
  (let [tempdir (fs/create-temp-dir)
        sha "4d252aef804f31022042126fd29f3cc41f3d126d"
        repo-dir "."
        worktree-dir (str (fs/file tempdir sha))
        worktree-list-dir #(when (fs/exists? worktree-dir)
                             (fs/list-dir worktree-dir))]
    (testing "At first, there are no files in the worktree folder"
      (is (empty? (worktree-list-dir))))

    (testing "After worktree-add, we can find our README in the worktree folder"
      (timemachine/worktree-add repo-dir worktree-dir sha)
      (try
        (is (contains? (set (map fs/file-name (worktree-list-dir)))
                       "README.md"))
        (finally
          (timemachine/worktree-remove repo-dir worktree-dir))))

    (testing "After worktree-remove, the folder is empty."
      (is (empty? (worktree-list-dir))))))
```

Til slutt kommer kjernen: `matnyttig.timemachine/do-at`.
Kjør en funksjon på et valgfritt punkt i tid, der "punkt i tid" er en Git-ref.

```clojure
(deftest do-at
  (testing "We can run functions in the past"
    (is (= (timemachine/do-at "HEAD" (constantly ::result))
           ::result)))

  (testing "bb.edn existed at this point in time"
    (is (contains? (timemachine/do-at "4d252aef804f31022042126fd29f3cc41f3d126d"
                     (fn [dir]
                       (->> (fs/list-dir dir)
                            (map fs/file-name)
                            (into (sorted-set)))))
                   "bb.edn")))
  )
```

Nå har du testene!

[Sindre] sa en gang til meg at selv om kopiering av kode kunne være dumt, kan kopiering av tester være veldig lurt.
Kopier gjerne testene over for å implementere selv!

[Sindre]: https://sindrejohansen.no/

… men jeg lovte å dele koden koden, og du skal få koden.
Tidsmaskin-navnerommet ser slik ut:

```clojure
;; bb/src/matnyttig/timemachine.clj
(ns matnyttig.timemachine
  "Kjør Clojure-funksjoner og shell-kommandoer slik et Git-repo var på et tidspunkt (Git-revisjon)"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(defn rev-parse [dir git-revision]
  (-> (p/shell {:out :string :dir dir}
               "git rev-parse" git-revision)
      :out str/trim))

(defn worktree-add [dir path commit-ish & [extra-process-opts]]
  (-> (p/process (merge {:dir dir} extra-process-opts)
                 "git worktree add" path commit-ish)
      p/check))

(defn worktree-remove [dir worktree & [extra-process-opts]]
  (-> (p/process (merge {:dir dir} extra-process-opts)
                 "git worktree remove" worktree)
      p/check))

(defn ^{:indent 1} do-at
  "Pass handle-fn a dir argument where dir is the Git repo checked out at given
  Git revision

  git-revision: eg HEAD or 91fa7c32 or a branch name
  handle-fn: function of directory where files have been checked out."
  [git-revision handle-fn]
  (let [tempdir (fs/create-temp-dir)
        repo-dir "."
        sha (rev-parse repo-dir git-revision)
        worktree-dir (str (fs/file tempdir sha))]
    (worktree-add repo-dir worktree-dir sha)
    (try
      (handle-fn worktree-dir)
      (finally
        (worktree-remove repo-dir worktree-dir)))))


;; Example usage from Babashka task:
;;
;;   bb timemachine HEAD -- ls
;;   bb timemachine HEAD -- pwd
(defn ^{:indent 1} main
  [revision _ & shell-command-args]
  (do-at revision
    (fn [dir] (apply p/shell {:dir dir} shell-command-args))))
```

`bb.edn` lar oss kjøre `matnyttig.timemachine/main` som en Babashka-task:

```clojure
;; bb.edn
{:paths ["bb/src" "bb/test"]
 :tasks
 {:requires ([matnyttig.timemachine :as timemachine])
  timemachine (apply timemachine/main *command-line-args*)}}
```

... og en Makefile-task "samler" alle tasks vi har.
Hvis du har alle shell-kommandoer og sånn i `bb.edn`, klarer du deg kanskje uten `Makefile`.

```Makefile
# Makefile
test-latest-commit:
    bb timemachine HEAD -- make test
```
