:page/title What if the database didn't forget?
:page/locale :en
:blog-post/author {:person/id :person/christian}
:blog-post/published #time/ldt "2024-03-26T09:00:00"
:blog-post/tags [:datomic :clojure]
:blog-post/series {:series/id :datomic-en}
:blog-post/description

Our source code lives in Git. This allows us to track when any line was
introduced into the codebase, who last changed it, and why. Wouldn't it be nice
if we had the same level of control over the data in our production systems?
With Datomic, we do.

:open-graph/description

Traditional databases lose data every time you perform an `update` and `delete`.
What would the world look like if that weren't the case? Datomic has the answer.
