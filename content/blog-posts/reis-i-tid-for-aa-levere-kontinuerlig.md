:page/title Reis i tid med Git-worktrees for å levere kontinuerlig
:blog-post/author {:person/id :person/teodor}
:blog-post/published #time/ldt "2025-05-16T09:00:00"
:blog-post/tags [:testing :metodikk]
:blog-post/description

I Team Mat satser vi på å levere hyppig, med minimalt herk knyttet til hver leveranse.
Denne teksten tar for seg en kilde til herk som dukket opp, og hvordan git-worktree gjorde at vi fikk løst kilden til herket.

:open-graph/description

Om de første to ukene i ny jobb som utvikler med parprogrammering og kontinuerlig leveranse.

:blog-post/body

I Team Mat satser vi på å levere hyppig, med minimalt herk knyttet til hver leveranse.
Denne teksten tar for seg en kilde til herk som dukket opp, og hvordan [git-worktree] gjorde at vi fikk løst kilden til herket.

## Hyppige leveranser for effektivt arbeid og fornøyde utviklere

På Team Mat har vi ikke noen avsjekk med andre før en utviklers kode går i produksjon.
Jeg gjør følgende når jeg jobber på kode som skal ut:

1. Skriver koden min, og kode for å sjekke om koden funker
2. Kjører tester
3. Commit og push
4. CI-server kjører testene på nytt
5. CI-server prodsetter ny kode hvis testene er grønne.

Christian går i dybden på hvorfor og hvordan vi gjør dette i [Hvordan levere kontinuerlig](/hvordan-levere-kontinuerlig/).

[Hvordan levere kontinuerlig?]:

## Trøbbel i paradis når ikke alle endringer er sjekket inn

Prosessen over har imidlertid en kilde til feil!
Følgende kan skje:

1. Jeg jobber med to ting, og ikke én.
2. Jeg sjekker inn arbeidet på den ene tingen.
3. Jeg kjører testene, og får 🟢
4. Jeg comitter og pusher.
5. Testene på CI er røde! 🔴

Hvis jeg har skikkelig uflaks, har jeg gjort dette rett før jeg går for dagen.
Resten av folka trekker ned endringene mine, og nå får ikke de gjort jobben sin heller.

Au!
Hva gikk galt her?

## Diagnose: jeg og CI testet forskjellig kode!

Lokalt kjørte jeg testene på all kode jeg hadde på disk.
Noe var sjekket inn, noe var ikke sjekket inn.
CI kjente derimot kun til siste commit.

## Tiltak: kjøre lokale tester slik CI ser koden

Dette problemet kunne vært taklet på forskjellige måter.
Jeg fulgte to prinsipper:

1. Feedback er bedre når den har kortere forsinkelse
2. Tilby utvikleren mer informasjon, så kan utvikleren hente informasjonen som trengs.

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

[git-worktree]: https://git-scm.com/docs/git-worktree

Dette kan du gjøre i de fleste språk (runtimes).
Språk som starter raskt er foretrukket, fordi da slipper du en ekstra kilde til venting.
Bash, Javascript, Python og Babashka er gode kandidater.

Takk til [Kevin] som tipset meg om [git-worktree] for mange år siden.
En helt super Git-kommando som skinner sterkere når du må jobbe med (for) mange ting samtidig.

[Kevin]: https://kevin.stravers.net/

## Appendix: bare gi meg koden!!!

Vår løsning er skrevet i Clojure-dialekten Babashka, som er godt egnet for denne typen oppgaver.
Babashka starter raskt, og vi har gode biblioteker for å jobbe med filsystemet ([babashka/fs]) og prosesser ([babashka/process]).

[babashka/fs]: https://github.com/babashka/fs
[babashka/process]: https://github.com/babashka/process

Siden jeg skriver, kan jeg velge å presentere koden i rekkefølgen jeg vil selv.
Ha!

Først kommer testene.
Jeg liker å kunne forklare kode på denne måten.

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

- `worktree add` tar mappen med repoet, mappe for nytt wokrtree og en Git ref.
- `worktree remove` tar mappen med repoet, og mappen med worktreet.

`worktree-add` og `worktree-remove` testes sammen fordi `remove` må rydde opp etter `add`.

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
[Sindre] sa en gang til meg at å kopiere kode ofte var dumt, men å kopiere testene kunne være veldig lurt.
Kopier gjerne testene over for å implementere selv!

[Sindre]: https://sindrejohansen.no/

... men jeg lovte koden, du skal få koden.
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
