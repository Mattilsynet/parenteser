:page/title Some animals are more equal than others
:page/locale :en
:blog-post/published #time/ldt "2023-11-21T09:00:00"
:blog-post/author {:person/id :person/christian}
:blog-post/series {:series/id :full-text-search}
:blog-post/tags [:sok :javascript]
:open-graph/image /images/bokstavkjeks.jpg
:blog-post/description

Last time we looked at how full-text search works, we considered all the symbols
in the index as equally important. However, that's not always the case in
practice -- for example, matches for whole words are better than matches for
fragments of words. This can be addressed with boosting, which is today's topic.

:open-graph/description

How boosting the terms in an index -- both during indexing and searching -- can
give us more relevant search results.
