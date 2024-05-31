:page/title Transactions in Datomic: A Delightfully Convenient API
:page/locale :en
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-05-21T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic-en}
:blog-post/description

A database isn’t very useful if you can’t push data into it. After 7 blog posts
in the series, it’s time to take a look at Datomic transactions – and what the
Dead Kennedys have to do with it.

:blog-post/body

Data in Datomic isn’t stored in tables or documents but as independent RDF-like
tuples:

```clj
[entitet, attributt, verdi]
```

Here’s the example from the first blog post in the series which dealt with
exactly this:

```clj
[1234 :blog-post/id 14]
[1234 :blog-post/title "Smakebiter av Datomic: En eksplosjon av data"]
[1234 :blog-post/author 5678]
[1234 :blog-post/tags :datomic]
[1234 :blog-post/tags :clojure]
[5678 :person/id "magnars"]
[5678 :person/given-name "Magnar"]
[5678 :person/family-name "Sveen"]
```

Let’s say I want to add a tag to that blog post, something like this:

```clj
[1234 :blog-post/tags :eksplosjoner]
```

The transaction would look like this:

```clj
[[:db/add 1234 :blog-post/tags :eksplosjoner]]
```

Which I could send to the database this way:

```clj
(d/transact conn [[:db/add 1234 :blog-post/tags :eksplosjoner]])
```
