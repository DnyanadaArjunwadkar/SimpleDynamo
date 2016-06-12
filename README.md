# SimpleDynamo




This program performs following tasks:

1)works with basic operations, i.e., insert, query, delete, @, and *. This will test if everything is correctly replicated. There is no concurrency in operations and there is no failure either.
2)Testing concurrent ops(insert/query) with different keys
(The tester used independent (key, value) pairs inserted/queried concurrently on all the nodes.)
3)Testing concurrent ops with same keys
(The tester used the same set of (key, value) pairs inserted/queried concurrently on all the nodes.)
