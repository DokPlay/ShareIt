# Knowledge Base — Part 3/3 (Continuation, Mandatory)

This file is a mandatory continuation of SKILL.md (Part 1/3) and SKILL_APPENDIX.md (Part 2/3).
Always read and apply Part 1 + Part 2 + Part 3 together as a single knowledge base.


#Leverage Modern Java Features
REJECT: Sticking to outdated Java idioms and manual patterns when there are newer language features or library utilities that simplify code 
(for example, using verbose loops and collectors when List.of() or streams could do the job more clearly).
REQUIRE: Take advantage of language enhancements from recent Java versions in your codebase, as long as the project is on that Java version. Some examples:
- Use the Stream API and related methods for clarity when processing collections. For instance, 
if you need to transform a list, list.stream().map(x -> transform(x)).toList() (with Java 16+, toList() gives an unmodifiable List) is concise and clear. 
But also recognize when a simple loop is fine – the key is to make the code intention obvious.
- Use factory methods like List.of(...), Set.of(...) for creating small collections, rather than Arrays.asList or creating and then adding. 
These methods make the code more concise and the resulting collection is immutable by default, which is often desirable for constants or fixed sets.
- Use the diamond operator (<>) to avoid redundancy in generics, and var for local variables (Java 10+) where the type is obvious from context, to reduce clutter. 
For example, var movies = filmStorage.findAll(); is clear that movies is a List<Film> if that’s what findAll returns, and it avoids repeating the type on both sides. 
Don’t overuse var if it makes code less clear (like var x = someComplexExpression where x’s type isn’t evident).
- Utilize record classes (Java 16+) for simple data carriers, if appropriate, to avoid boilerplate of getters/setters.
- Use try-with-resources for any stream or file or DB resource handling, instead of old try-finally.
By using these features, the code becomes more succinct and often more performant (the JDK methods are highly optimized). Ensure all team members are aware of these features so the code style stays consistent.
REASONING: Java evolves to make developer life easier and code more expressive. Not using these features is like not using a tool that’s in your toolbox. For instance, prior to Java 8, 
mapping and filtering a list required several loops and temporary collections; now it can often be done in one chained statement. This reduces lines of code and potential bugs. Built-in methods 
(like Objects.requireNonNullElse or Collectors.toMap) have been thoroughly tested and handle corner cases, so using them can make your code more robust. 
Adopting modern practices also keeps the codebase welcoming to new hires familiar with contemporary Java; if someone sees code stuck in 1.6-era style, they might unintentionally mix paradigms or find it harder to follow. 
Performance-wise, newer methods sometimes have optimizations (for example, String.join or streams with laziness for large data). 
Overall, writing in a modern style tends to improve readability and maintainability, signaling that the project is keeping up-to-date.
EXCEPTION: Avoid using a new feature just for the sake of it if it doesn’t make the code clearer. 
For example, not every loop should be a stream – sometimes a simple loop with break/continue is more straightforward than a complex stream with filters and collectors. 
Also consider team familiarity; if a feature is very new (say, a preview feature or just released), ensure the team is comfortable with it and that your build environment supports it. 
But for well-established features (Java 8 through Java 17’s LTS features), there’s little reason not to use them.

#Preserve Insertion Order When Needed
REJECT: Using unordered data structures (like plain HashSet or HashMap) in scenarios where the order of elements matters for correctness or user experience.
REQUIRE: Choose collection implementations that guarantee an iteration order when your use case depends on order. 
For example, if you want to maintain the order in which friends were added or the order of genres as provided by the user, use a LinkedHashSet instead of a HashSet – this preserves insertion order. 
Or, if results need to be sorted, consider using a TreeSet/TreeMap (which keeps natural or specified order), or sort the collection explicitly and use an ordered list. 
Also ensure that if the source of data is a database query, you request the data in a sorted order (using ORDER BY) if the order is meaningful (databases by default might not guarantee order without explicit instruction). 
In summary, be mindful of order requirements: if a certain output or logic expects things in a particular order, design your data handling to respect that.
REASONING: Many subtle bugs and user interface quirks can arise from using unordered collections where order was implicitly expected. For instance, 
if you retrieve a set of friends and return it as a JSON array, a HashSet might list them in different orders on different calls, which could confuse clients or make testing harder. Using an ordered collection gives consistency. 
It also signals to other developers that order is important. For caches or maps where you want predictable iteration (like iterating in insertion order for LRU logic or just for deterministic output), LinkedHashMap is appropriate. 
Without these, the behavior might appear correct most of the time (since HashMap’s iteration order can be stable across runs for the same insertion sequence, up until a resize happens) and then suddenly shift, which is hard to debug. 
Explicitly preserving order avoids such surprises. Deterministic output is also crucial for repeatable tests – tests that assert a list of results will fail intermittently if the underlying collection is unordered. 
Thus, if order matters at all, use the right tool for it.
EXCEPTION: If order truly doesn’t matter (like a set of IDs purely for existence checks), then a HashSet is fine and likely more performant. The key is to identify when the problem domain has an ordering aspect 
(whether chronological, sorted by a comparator, etc.) and cater to that.

#Avoid the N+1 Query Problem
##(This reiterates some points from performance but as a general principle)
REJECT: Designing data access or service logic that triggers a cascade of queries proportional to the number of items being processed 
(for example, loading a collection of objects and then in a loop loading each object’s details with separate queries).
REQUIRE: Plan data loading strategies that fetch all needed data with as few queries as possible. Use joins or batch selects to retrieve related data. In an ORM context (like Hibernate), 
be cautious of lazy loading in a loop – either use eager fetching appropriately or batch-fetch associations. In explicit JDBC, as described earlier, use IN clauses or bulk operations. 
The goal is that the number of database calls remains roughly constant (or grows sub-linearly) even as the amount of data grows. Always consider: “If I have 1000 items, will this approach run 1000 queries?” If yes, 
rethink it to use a different approach (like one query that brings all needed data).
REASONING: (Consolidating prior reasoning) N+1 queries can cripple application performance under load. What’s fast for 5 items becomes disastrously slow for 5000. 
By avoiding this pattern, you ensure that your code can handle larger data sets gracefully. It also simplifies consistency – all data is loaded in one shot, reducing the window where data could change mid-loading in between queries. 
While sometimes an N+1 might slip in innocuously, getting into the habit of searching for and eliminating them (via analysis or performance tests) is a crucial part of writing efficient enterprise applications. 
This guideline overlaps with the specific performance item but is here to emphasize the conceptual best practice: always be mindful of how your code scales with data size.
EXCEPTION: If N is guaranteed to be extremely small or bounded (e.g., loading details for at most 2-3 items, and it’s hard to combine those queries), a minor N+1 might be acceptable for simplicity. 
But those cases are rare and should be commented if they exist (“this will only ever loop 3 times, acceptable tradeoff”).

#Use Batch Operations for Bulk Updates/Inserts
##(Also overlaps with performance, reaffirming best practice)
REJECT: Looping over a list of items and performing individual insert/update operations for each item (leading to many calls).
REQUIRE: Whenever you have bulk data to insert or update, use the batch features of your database or data access framework. 
This could mean using JDBC batch updates, or if using JPA/Hibernate, using batch mode or clear the persistence context periodically to avoid memory issues when batch processing. 
Ensure that batch sizes are tuned if necessary (e.g., if updating tens of thousands of rows, commit in chunks of, say, 1000 to avoid a huge transaction). 
The code example earlier illustrated using jdbcTemplate.batchUpdate with a batch of parameters for inserts – follow that pattern for similar needs. 
For updates, sometimes a single SQL can update multiple rows if they’re identical operation (like UPDATE table SET status='DONE' WHERE id IN (...)).
REASONING: The reasoning is essentially what was stated: reduce DB round trips, improve performance. Including it as a guideline here generalizes it beyond our specific context to any situation of bulk data processing. 
It’s a common best practice in batch processing or any time a user might submit, say, a list of items to create. By doing it server-side in one go, you also ensure atomicity (if needed) and easier error handling 
(either all succeed or all fail in one transaction, depending on requirements, but at least you know how many succeeded in one call).
EXCEPTION: For small batches (a handful of items), the complexity of batching may not be needed – but it rarely hurts either. 
Developer effort vs. benefit should be weighed when batch size is truly minimal, but typically the framework makes batching easy enough to always do it.
(By consolidating overlapping items in performance and general, we've ensured that all points are covered at least once in a structured way.)

#Build and Versioning
##Target Latest LTS Java Version
REJECT: Starting new projects on outdated Java versions without a compelling need, or neglecting to upgrade existing projects to newer Java LTS versions over time.
REQUIRE: Use the current Long-Term Support (LTS) version of Java for all new development (for example, Java 17 or Java 21 or Java 25, whichever is the latest LTS at the time of writing). 
For existing projects, plan upgrades to move from older versions (like Java 8 or 11) to the latest LTS. Incorporate this into your build configuration by setting the 
Java compiler/source/target version to that LTS. Also ensure your dependencies and build tools (Maven/Gradle plugins, etc.) are compatible. When generating project scaffolding or documentation, 
explicitly mention the Java version requirement. If you’re writing a tool or script (like a project generator) that detects an older Java version, have it prompt or guide the user to upgrade to the modern LTS if possible.
REASONING: Each Java LTS release brings significant improvements – for example, Java 17 has performance enhancements (a more efficient garbage collector, etc.) and language features 
(like text blocks, improved switch, records) that can boost productivity and performance. Sticking to an old version means missing out on these and can also lead to support and security issues 
(once a Java version is out of free support, you won’t get updates unless you have a paid plan). Moreover, the ecosystem moves forward: libraries start to require newer Java versions for their latest releases. 
Keeping projects on modern Java ensures better compatibility with new libraries and tools. The cost of upgrading is usually far outweighed by the benefits in the long run, and doing it periodically 
(at LTS intervals) prevents the gap from becoming so large that an upgrade is a huge effort. Essentially, treat Java version like a dependency that needs maintenance – don’t let it become obsolete.
EXCEPTION: If you have a hard requirement from the environment or legacy systems that forces a specific older Java (for instance, some enterprise container that only runs on Java 8), you may delay an upgrade. 
But even then, you should plan how to eventually remove that constraint. For any greenfield (new) project, there’s almost no reason to not use the latest LTS. 
Always communicate to stakeholders the importance of staying up-to-date with the platform.

