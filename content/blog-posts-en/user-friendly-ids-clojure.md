:page/title User-Friendly IDs in Clojure
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-04-30T09:00:01"
:blog-post/tags [:clojure :ux]
:blog-post/description

A concrete implementation of the neat IDs I outlined in Data Modeling is UX
Design.

:open-graph/description

A Clojure algorithm for generating nice user-friendly ids.

:blog-post/body

In [Data Modeling is UX Design](/en/data-modeling-ux/) I outlined an algorithm
for generating user-friendly ids, but how can it actually be done in practice?
Here's one possible solution in Clojure.

The algorithm requires maintaining a counter, typically in a database. I'll skip
this detail here, as it depends on the database you're using. But each time you
generate an ID, you need to increment the counter by one in a transaction and
retrieve the new value, let's call it `n`:

```clj
(def n 1024)
```

Next, we need an alphabet and the number of characters in it:

```clj
(def alphabet "23456789acefghjkqrsuwxz")
(def num-chars (count alphabet))
```

To translate our number (`1`) into an ID in our alphabet, we need to convert
from the decimal system to our handcrafted base-23 system. Brace yourself for a
mouthful:

```clj
(->> n
     (iterate #(/ % num-chars))
     (map long)
     (take-while pos?)
     (map #(mod % num-chars)))
```

This is a kind of factorization. `iterate` returns an infinite sequence that
looks like this:

```clj
(def n 1024)

(n
 (/ n 23)
 (/ (/ n 23) 23)
 (/ (/ (/ n 23) 23) 23)
 (/ (/ (/ (/ n 23) 23) 23) 23)
 ,,,)
```

`(map long)` rounds these off to integers:

```clj
(1024
 44
 1
 0
 0
 ,,,)
```

`(take-while pos?)` takes from this sequence as long as the number is greater
than 0:

```clj
(1024
 44
 1)
```

Finally, we use modulus (`mod`) against the length of the alphabet to find out
how many of each power there are, `(map #(mod % num-chars))`, which gives us:

```clj
(12
 21
 1)
```

This is, as mentioned, a "kind of factorization" (the factors appear in reverse
order), because it can be read as "12 ones, 21 tens, and 1 hundred", or
more precisely with our alphabet:

```clj
12 * 23^0 ;; "Ones"
21 * 23^1 ;; "Tens"
 1 * 23^2 ;; "Hundreds"
```

The order of the factors are irrelevant as long as all ids are generated the
same way.

We can now look up the factors in our new alphabet and assemble it into a
string:

```clj
(def alphabet "23456789acefghjkqrsuwxz")
(def num-chars (count alphabet))

(defn encode-id [n]
  (->> n
       (iterate #(/ % num-chars))
       (map long)
       (take-while pos?)
       (map #(mod % num-chars))
       (map #(nth alphabet %)) ;; Look up
       (apply str)))           ;; Make a string
```

This gives us the ID `"gx3"`. If we want all IDs to have a certain number of
characters, like I suggested, we have a couple of choices. We opted for the
simplest solution, which is to start our counter at the lowest number that
yields `x` characters, i.e., `(Math/pow 23 (dec x))`, which with `x` equal to
`6` gives the ID `"222223"`. This also highlights the reversal of the factors,
as the "one" (i.e., `3`) is at the end, prefixed by a bunch of "zeros" (i.e.,
`2`).

## A Bit More Fun with IDs

The IDs we get now look a lot like serial numbers -- which they are. To reduce
this effect, we cheekily shuffled our alphabet, which can also produce some fun
letter combinations. We ran `shuffl` and adjusted the result a bit by hand,
ending up with this alphabet:

```clj
(def alphabet "awsg6x9h34j572uqr8feckz")
```

We were also happy with IDs of 3 or more characters, resulting in IDs like
these:

```clj
(encode-id 529) ;;=> "aaw"
(encode-id 530) ;;=> "waw"
(encode-id 531) ;;=> "saw"
```

It doesn't get any more fun than you make it, right?
