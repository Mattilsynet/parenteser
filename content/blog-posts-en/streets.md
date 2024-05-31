:page/title Write code instead of SQL
:page/locale :en
:blog-post/author {:person/id :person/magnar}
:blog-post/published #time/ldt "2024-04-02T15:45:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic}
:blog-post/description

Datomic moves queries to the client. This means that you don't need to send a
text string to another server, but can instead dig into the data where you are.
Let's take a look at a practical example.
