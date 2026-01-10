# Knowledge Base — Part 2/3 (Continuation, Mandatory)

This file is a mandatory continuation of SKILL.md (Part 1/3).
Always read and apply Part 1 + Part 2 + Part 3 together as a single knowledge base.


# Knowledge Base Appendix
This file extends SKILL.md.
Start with SKILL.md first, then use this appendix for templates/examples/details.

#Consistent Handling of Likes and Friendships
REJECT: Inconsistently updating relational data like likes and friendships, or failing to enforce business rules (like preventing duplicate likes or self-friendship) after moving to persistent storage.
REQUIRE: Implement liking and friending operations in the service layer with clear rules and use the repository to persist changes:
- Adding a Like to a Film: In FilmService.addLike(filmId, userId), first retrieve the film and user to ensure both exist (the repositories will throw NotFoundException if not, which you catch or let propagate as a 404). 
Then add the userId to the film’s likes set in memory. If the set was a Set, it will automatically ignore duplicates – so a duplicate like won't change anything. 
After modifying, call filmStorage.update(film) to save the new like list to the database (which, under the hood, will insert a row in film_likes). Log an INFO-level message like “User X liked Film Y”. 
If the user was already in the set (duplicate like attempt), you might choose to log that as a DEBUG or INFO stating no change happened.
- Removing a Like from a Film: Similarly, retrieve the film (ensure it exists). Remove the userId from its likes set. If the set actually contained the user, 
then call filmStorage.update(film) to persist the removal (the repository will delete the corresponding row from film_likes). Log an INFO saying “User X removed like from Film Y”. 
If the user was not in the set (trying to remove a like that isn’t there), do not update (no change needed). 
Instead, log a WARN indicating an attempt to remove a non-existent like – as this is unusual and might indicate a logic issue or repeated request.
- Adding a Friend (Friend Request): In UserService.addFriend(userId, friendId), ensure both userId and friendId exist by retrieving those users. 
Verify that userId is not equal to friendId (prevent a user from adding themselves; if that’s attempted, throw a ValidationException). 
Then add friendId to the first user’s friends set. Use a Set so duplicates are automatically handled (no duplicate friend entries). 
Update the user in storage (userStorage.update(user)) to save the new friendship (the repository will insert into friendships table). 
If this friend addition now means both users have each other in their friends sets, you can interpret that as a confirmed friendship – optionally log something like “Friendship confirmed between User A and User B”. 
If only one side has the other (one-way friendship, perhaps representing a pending state), log that User A added User B as a friend. 
Importantly, do not automatically add A to B’s friend list in code – keep it one-directional unless your business logic explicitly wants auto-confirmation.
- Removing a Friend: In removeFriend(userId, friendId), retrieve both users to ensure validity. Remove friendId from userId’s friends set. 
If the friend was present, update user storage to persist the change (repository will delete that row from friendships). 
This effectively means userId no longer considers friendId a friend. Do not remove the inverse relationship (if B had A, B still has A until B also removes it). Log that User A removed User B from friends. 
If User B wasn’t actually in User A’s friend set, you might log a WARN or ignore it, as it was a no-op (similar reasoning to removing a like that isn’t there).
- Retrieving Friends List: Provide a method getFriends(userId) that fetches the user (to ensure they exist) then uses the friends IDs set to gather full User objects for each friend. 
This can be done by calling userStorage.getById for each ID, or by a custom repository query that gets all friends in one go. Return the list of User objects representing the friends.
- Retrieving Common Friends: Implement getCommonFriends(userId, otherId) in the service by fetching both users, then computing the intersection of their friends ID sets (e.g., commonIds = setA ? setB). 
Then retrieve those users (again possibly in a batch or loop) and return them as a list. This yields mutual friends between the two users.
Throughout these operations, handle exceptions from the storage layer (e.g., if a user or film isn’t found, propagate or translate to appropriate HTTP errors) and use logging to record abnormal attempts 
(like removing non-friends or liking already liked item) at WARN level, while normal actions at INFO.
REASONING: Likes and friendships represent relationships between entities and need careful handling to maintain data integrity. 
Doing this in the service layer (instead of directly manipulating database tables from multiple places) centralizes the logic. 
It ensures, for example, that you never add a friendship in one part of code without the duplicate-check or self-check that another part does – because there’s a single method responsible. 
Using sets to represent these relationships in memory provides an easy way to enforce uniqueness and simplify comparisons (duplicates naturally filtered, set intersection for common friends, etc.). 
Logging at the appropriate level aids in monitoring usage patterns and diagnosing issues (e.g., seeing a lot of WARN "remove like not present" might signal a UI bug causing redundant requests). 
The one-directional approach to friendships (only add one side) as described is a business decision – it simplifies things by not forcing mutual updates, 
but the logic is prepared to handle the moment when it becomes mutual (both sides added). Keeping that logic consistent is key to not ending up with divergent friend data. By following these guidelines, 
the friendship and like features remain reliable after moving to the database, preserving all the rules that existed (no duplicates, no self friendship, etc.) and ensuring that changes persist correctly.
EXCEPTION: The specifics of likes/friends logic can vary by application (some might auto-confirm friendships, others not), but the approach of doing checks in service and using repository updates is general. 
If the business rules change (say, making friendships mutual), this would be implemented by adjusting the service logic (e.g., adding the inverse friend and possibly a different table structure for pending requests), 
but still in one place.

#Normalize and Validate Input Data
REJECT: Saving or processing data inputs as-is without enforcing consistency or validity, which can lead to bad data in the database (e.g., duplicate entries in a list, missing required fields, 
or logically incorrect values like dates in the future when they shouldn’t be).
REQUIRE: Before persisting or further processing, normalize and validate all critical data in the service layer:
- Deduplicate and Clean Collections: If an entity contains a collection (e.g., a film’s set of genres), ensure that collection has no duplicates and contains only valid entries. 
For instance, if a Film comes in with a list of genres that might have duplicates, convert it to a Set to automatically drop duplicates. Also remove any genre entries that are null or have invalid IDs. 
It’s good to use a deterministic order (like a sorted order or insertion order) for the set to maintain consistency — for example, you might sort genre IDs and then store them in a LinkedHashSet to preserve that sorted order in the set.
- Default Null Collections to Empty: If a field like film.genres or user.friends is null (meaning no data provided), set it to an empty collection instead of leaving it null. 
This way, rest of the code can assume the collection is non-null and simply iterate or add to it without extra null checks.
- Validate Foreign Key References: Check that any foreign key IDs provided actually exist in their respective tables before proceeding. 
For example, if a client tries to create a film with mpa.id = 99, but you only have 5 MPA ratings (1-5), detect that and throw a ValidationException 
(“MPA rating 99 does not exist”) rather than attempt an insert that will fail or result in an inconsistent film. Similarly, for user-supplied friend IDs or liked film IDs, ensure those refer to actual entities.
- Enforce Business Rules: Validate all domain-specific rules. For instance, ensure a film’s release date is not before the first known film (e.g., not before 1895-12-28, if that’s a rule) – reject or correct it if it is. 
Check that a film’s duration is positive, a user’s email is not empty and matches a basic email regex, a user’s birthday is not in the future, etc. 
Use a combination of manual checks and Bean Validation (@NotNull, @Size, @Past, etc. on DTOs) to cover these. If any rule is violated, throw a clear exception 
(like ValidationException) with a message that can be translated to a 400 Bad Request response with details.
REASONING: Garbage in, garbage out – if you allow invalid or messy data into the system, it will eventually cause failures (maybe when querying or when making calculations). 
By normalizing data (like cleaning up collections) you ensure internal consistency – e.g., the same genre isn’t stored twice for a film, which could otherwise lead to duplicate entries in a join table or duplicate displays in a UI. 
By validating up front, you catch errors early and give immediate feedback, rather than letting bad data flow downstream into the database, 
where it might violate constraints or cause incorrect results (like a film with a ridiculously long description might break a UI later if not checked). 
It’s much easier to enforce rules in one place (the service) than to debug random failures later. Moreover, having these validations makes the system more robust against both user errors and potential bugs. 
Database constraints (like NOT NULL, FK constraints) are a last line of defense – ideally, 
your service logic should catch issues before they ever hit the DB so that the user gets a controlled error message and the DB doesn’t have to abort a transaction.
EXCEPTION: None – all input that comes from outside (API calls, file loads, etc.) should be considered untrusted and validated/normalized. 
The only slight exception is if you have another validation layer (like Spring’s @Valid on controllers) that catches some errors – but even then, the service might do deeper logical validation beyond simple field constraints.

#Performance and Optimization
##Avoid N+1 Query Patterns
REJECT: Querying the database in a loop for related data (the classic "N+1 queries" anti-pattern, where N is the number of items and you perform an additional query for each item).
REQUIRE: Fetch related data in bulk with set-based queries instead of iterative one-by-one retrievals. For example, if you need to load a list of films and their genres, do not:

List<Film> films = filmStorage.findAll();  
for (Film f : films) {  
    f.setGenres(genreStorage.findByFilmId(f.getId())); // N extra queries  
}
This causes one query to get films and N more queries (one per film) to get genres. Instead, do something like:
- Retrieve all films with one query (maybe joining the MPA table to get rating names).
- Get all film IDs from that result, and then query the film_genres table with a single SELECT that uses WHERE film_id IN (...) for all those IDs. This returns all genre links in one shot. 
Group the results by film_id in memory and assign genre sets to each Film.
- Similarly, if needed, do one query to get all likes for those film IDs in bulk.
This way, regardless of how many films, you’re using a fixed number of queries (e.g., 2 or 3) instead of proportional to the number of films. The same approach applies to users and their friends: fetch all users, 
then fetch all friendships for those user IDs in one query, rather than per user.
REASONING: The N+1 pattern can be a serious performance killer. If N is large (say 1000 films), N+1 queries means 1001 database calls. Each call has overhead in terms of network, parsing, and context switching. 
By batching into one query, you leverage the database’s set processing power and drastically cut down on round trips. The memory overhead of grouping results in the application is usually minimal compared to the cost of repeated DB calls. 
In addition, a single well-tuned query often can use indices and joins more efficiently than many small ones. Ultimately, avoiding N+1 ensures your application can scale – the database work grows with data complexity, 
not exponentially with the number of records.
EXCEPTION: If the dataset is extremely large and cannot be retrieved in one go due to memory, you might batch in chunks (e.g., 100 at a time). But this is still better than one at a time. 
The principle remains: prefer set operations over iterative queries.

#Use Batch Operations for Bulk Writes
REJECT: Performing repetitive insert/update operations one by one in a loop (each causing a separate database transaction or round trip).
REQUIRE: Utilize batch capabilities of your database access layer for inserting or updating multiple records. For example, if you need to insert 50 genre entries for a film (maybe an extreme case), 
instead of calling jdbcTemplate.update(...) 50 times, prepare all 50 sets of parameters and call jdbcTemplate.batchUpdate(sql, batchParams). 
This sends all inserts in one or a few requests to the DB. In SQL, you might use a single INSERT ... VALUES (...), (...), ... statement for multiple rows or database-specific bulk syntax if available. 
Similarly, for updates or deletes, group operations when possible (or use WHERE IN (...) to delete multiple). In the context of our application: 
when updating a film’s genres, delete all old genre links with one DELETE ... WHERE film_id = ?, and insert new ones with one batch call for all genre IDs. For adding multiple likes or multiple friends, 
consider batch inserting those relationships as well.
REASONING: Batching significantly reduces overhead. The cost of setting up a database connection and transaction can dwarf the cost of the actual data change if done record-by-record. 
By batching, you amortize that cost over many operations. The DB can often execute a batch of inserts more efficiently than many single inserts (it can optimize I/O and logging). 
From the application’s perspective, batch operations also simplify error handling – you handle one big result instead of many small ones, and either all succeed or you can handle partial failures in aggregate. 
This leads to faster writes, especially in scenarios like saving a film with 10 genres or a user with 100 friends; those become 1-2 DB operations instead of 10 or 100.
EXCEPTION: In rare cases, if each operation must be atomic and independent (e.g., each insert triggers complex logic or you need per-insert error handling), batching might be less straightforward. 
But for our use cases (likes, genres, friends), it’s straightforward. 
Also, extremely large batches (thousands of records at once) might need to be chunked to avoid hitting query size limits or transaction log issues – but that still beats single-row operations.

#Optimize Aggregation Queries
REJECT: Calculating summary values (counts, averages, etc.) in inefficient ways, such as computing them in application code with multiple queries or using suboptimal database queries that recalc aggregates repeatedly.
REQUIRE: Let the database handle aggregation in a single, optimized query whenever possible. For example, to get the "most popular films" (films sorted by number of likes), use an SQL query that joins the films with a subquery or aggregate:

SELECT f.*, m.name as mpa_name,  
       COALESCE(fl.likes_count, 0) AS likes_count  
FROM films f  
JOIN mpa_ratings m ON f.mpa_id = m.id  
LEFT JOIN (  
   SELECT film_id, COUNT(*) AS likes_count  
   FROM film_likes  
   GROUP BY film_id  
) fl ON f.id = fl.film_id  
ORDER BY likes_count DESC, f.id ASC  
LIMIT ?;  
This query computes the count of likes per film just once (in the subquery fl) and returns the top N films in order. The application simply executes this query to get the results; it doesn’t need to manually count or sort anything. 
Likewise, for any similar problems (like top friends, etc.), aim to use DB-side computation. Ensure proper indexes (e.g., an index on film_likes.film_id) to make counting efficient. Use COALESCE or similar to treat null counts as 0.
REASONING: The database is optimized for set-based operations and aggregations. It can count and sort using indexes and native algorithms that are often much faster than pulling all data into the app and then processing it in Java. 
By pushing the work to the DB, you also transfer less data (e.g., you only get the top 10 results, not every film’s like list). 
The query above, for instance, will likely use an index on film_likes to get counts quickly and sort with the DB engine’s highly-tuned sorting. 
The alternative (fetch all films, fetch all likes, combine and sort in Java) would be far more network-heavy and slower. 
Additionally, writing it as one query ensures consistency (all data is calculated at the same snapshot of the database, within one transaction or query context).
EXCEPTION: If the logic cannot easily be done in SQL (very complex or requiring custom code), you might not fully push it to the DB. But even then, try to minimize data movement 
(maybe use a stored procedure or a partial aggregation in SQL). The general principle is to capitalize on DB strengths for heavy data lifting.

#Eliminate Redundant Processing
REJECT: Wasting effort on processing data that isn’t there or repeating work on identical inputs unnecessarily.
REQUIRE: Add simple guards and use data structures to avoid redundant operations:
- Skip empty operations: If you’re about to run a loop or DB operation on a collection, check if the collection is empty (or the result set is empty) first. 
For example, if a film has no likes, don’t attempt to delete from film_likes or do an insert batch of zero elements – simply skip that step. Similarly, if your code is about to batch-update an empty list of genres, short-circuit it. 
This saves a needless call or loop iteration.
- Remove duplicate inputs: When preparing to fetch or update by IDs, use a Set to eliminate duplicates in the list of IDs. 
For instance, if somehow the application ended up with duplicate user IDs in a friends list due to input, convert to a Set so you only process each user once. 
If you’re constructing an SQL IN ( ... ) clause or batch parameters, ensure each unique key is only included one time.
- No-op short circuits: Implement checks like if (newValue.equals(oldValue)) return; in setters or update routines where appropriate to avoid performing an expensive operation when nothing actually changed. 
For example, if a user tries to "add friend" with someone who’s already in their friend list, detect that and do nothing (aside from maybe logging) instead of going through DB update logic that will effectively change nothing.
REASONING: These patterns improve efficiency with minimal effort. 
Skipping work when there is none to do (empty lists) is a basic but important optimization – it reduces noise 
(no empty transactions or log messages for “updated 0 rows”) and very slightly improves performance (skipping a function call or DB round-trip). 
Deduplicating inputs ensures you don’t accidentally do redundant computations, which not only wastes time but could also lead to incorrect double-counting if not careful. 
The overhead of these checks is usually trivial (an isEmpty() call, a Set conversion) compared to the operations they guard. 
Moreover, it clarifies intent: seeing an early return on no data clearly communicates that case is handled intentionally. 
This leads to cleaner logs and more predictable performance, especially in edge cases or when dealing with user-provided data that might be quirky.
EXCEPTION: Be careful that skipping operations doesn’t hide a bug (e.g., if a list shouldn’t be empty at a certain point, and it is, you might want to know that rather than silently skip – but that’s more a logic assertion). 
Generally, these checks are straightforward improvements.

#Pre-Size Collections for Efficiency
REJECT: Using default initial capacities for collections in scenarios where you know you will be adding a large number of elements, causing repeated resizing or rehashing.
REQUIRE: When you have an estimate of collection size, initialize the collection with a capacity at least that large. For example, if you are about to collect 1000 items into a list, do new ArrayList<>(1000) to allocate space upfront. 
If you’re putting a known number of entries into a HashMap or HashSet, use new HashMap<>(expectedSize) (remember that HashMap capacity should account for the load factor; using expectedSize directly is fine as it will be adjusted internally). 
In our context, if you query 100 films and then plan to put their IDs in a set or results in a list, pre-size those collections to 100. When batching inserts, if you know you have N inserts to do, initialize the parameter list with size N.
REASONING: Pre-sizing avoids the incremental reallocation that occurs as collections grow. 
An ArrayList starts at a small default size and doubles when exceeded – if you know you need space for 1000, it’s more efficient to allocate once rather than have it resize multiple times as you add elements. 
Each resizing involves allocating a new array and copying elements. Similarly, a HashSet or HashMap’s backing array will rehash as it expands; giving it a good initial capacity prevents or reduces that. 
While in many cases the performance gain is minor, for tight loops or large data sets it can be noticeable. It also conveys intent: a reader of the code sees that you expect about N items, which documents the scale of operations. 
This is a micro-optimization, but when used judiciously (especially in code that runs often or processes big collections) it contributes to overall efficiency.
EXCEPTION: If you truly don’t know how many elements to expect, or the number can be huge (and might exceed memory if you allocate that big), you have to use defaults. 
Also, pre-sizing with an extremely large number could waste memory if you overestimate drastically. 
Use realistic estimates or bounds (e.g., if a query limit is 100, you can always pre-size to 100, even if sometimes you only get 3 results – the wasted space is trivial in that case).

#API and Security
##Never Expose Entities in API Contracts
REJECT: Using internal persistence entities (e.g., JPA @Entity objects) directly as inputs or outputs in your REST API, for example, accepting an User entity in an @RequestBody or returning a Film entity directly as JSON.
REQUIRE: Define separate Data Transfer Object (DTO) classes for your API requests and responses. These should contain only the fields needed by the client. 
Convert between these DTOs and your internal entities in the service or controller layer. For instance, have a FilmDto for output that might include id, name, 
description, releaseDate, duration, mpaRating, genreIds (whatever is needed by clients) rather than the entire Film entity with all its relationships. 
Likewise, for creating or updating, have an input DTO that includes just the necessary fields (and perhaps IDs referencing other entities). 
The service layer will map this to the real Film object, fetch actual Genre objects for given IDs, etc. 
This way, JPA/Hibernate-specific annotations and lazy-loading proxies never leak into the controller, and you control exactly what gets serialized.
REASONING: Exposing entity objects can tightly couple your API to your database schema and implementation details. 
It can also be a security hazard: for instance, if your User entity has a isAdmin flag or a passwordHash field, returning the whole entity would inadvertently expose those to the client. 
Or accepting an entity as input might allow a malicious user to set fields they shouldn’t (mass assignment vulnerability), like setting their own isAdmin=true in a JSON payload. 
Using DTOs gives a clear separation – you expose only what you want. It also frees you to change internal implementations without breaking API contracts (e.g., renaming a field in the database entity can be isolated to the mapping, 
while the API DTO remains the same). In addition, frameworks like Jackson can inadvertently trigger lazy-loading of relationships when serializing an entity, causing N+1 queries and performance issues, 
which DTOs avoid since you populate them deliberately.
EXCEPTION: In quick prototypes or very simple applications, teams sometimes skip DTOs for speed, but it’s a risky shortcut. In an internal or early-stage project, if you do this, you should treat it as technical debt to fix soon. 
For any production or shared API, never expose or accept entities directly.

#Standardize Error Handling in APIs
REJECT: Allowing default error responses (like the raw Spring Boot error HTML or stack traces) to be returned to API clients, or otherwise returning inconsistent error formats from different endpoints.
REQUIRE: Implement a global exception handler (using @ControllerAdvice in Spring, for example) to intercept thrown exceptions and convert them into uniform JSON error responses. Define a structured error format, for instance:

{ 
  "timestamp": "2026-01-10T05:10:45Z", 
  "status": 400, 
  "error": "Bad Request", 
  "message": "Validation failed: email must not be empty", 
  "path": "/users" 
}
This is just an example; the structure can vary (some include an error code or a list of field errors for validation). The key is that every error response from your API follows the same schema. 
In your exception handler, catch common exceptions like MethodArgumentNotValidException (for validation errors), custom exceptions like NotFoundException, or others like IllegalArgumentException, 
and map them to an appropriate HTTP status code and message. For instance, NotFoundException -> 404 Not Found with a message, ValidationException -> 400 Bad Request with details. 
Additionally, ensure that internal details (stack traces, exception class names) are not sent to the client; log those on the server side instead. 
The client should get a user-friendly (or at least client-consumable) error object without internal implementation specifics.
REASONING: A standardized error response format makes it easier for API consumers to handle errors. They can always expect certain fields to be present (status, message, etc.) and parse them. 
It also avoids leaking sensitive information – default error pages or stack traces can reveal internal class names or even source code lines that attackers could exploit, and are generally not useful to clients. 
By handling exceptions in one place, you ensure consistency (all 404s look the same, all validation errors are structured similarly, etc.) and you can enforce any logging or alerting when certain errors occur. 
This improves both security and maintainability of the API.
EXCEPTION: Minor differences might exist between environments (e.g., in dev you might include more debug info), but the format should remain mostly consistent. 
Also, for truly unexpected exceptions that you didn’t specifically handle, you should have a catch-all in the global handler that returns a generic 500 error response in the same format 
with maybe a generic message like "An unexpected error occurred") while logging the details internally.

#Enforce Security Boundaries Between Gateway and Services
REJECT: Duplicating authentication logic in microservices or performing authorization in the wrong layer, such as a gateway re-checking business rules or a service re-validating JWT signatures on every request.
REQUIRE: In a microservice architecture with an API gateway (or auth service) in front, clearly separate concerns:
- Authentication (verifying who the user is) should happen at the gateway or a dedicated auth service. For example, the gateway should validate the JWT token’s signature and expiration. 
Downstream services can trust that if a request comes through with certain headers or a security context, the user has been authenticated. They should not each individually call the identity provider to verify the token again. 
Use a token relay or context propagation mechanism instead of re-authenticating.
- Authorization (what the user is allowed to do) should happen within each service according to its domain rules. 
For instance, if a token says the user has role "USER", the service should check if that role is sufficient for the action (maybe admin role is needed to delete something, etc.). 
Also, services enforce data-level permissions (user X can only modify their own data, etc.). The gateway typically should not attempt to enforce those kinds of rules since it doesn’t have the full context.
- Input Validation should be done in the service that knows the business context. A gateway might do basic schema validation on requests, but it won’t know deeper invariants. 
Each service validates its own inputs (e.g., a Film service validates film data, a User service validates user data) using @Valid annotations or manual checks, and returns errors as appropriate.
Essentially, let the gateway handle cross-cutting concerns like auth token validation, request routing, maybe high-level rate limiting. Let the services handle domain-specific logic and checks. 
Ensure that any user context info (like user ID or roles) is passed from gateway to service (commonly via JWT claims or headers). 
Also, avoid making each microservice call out to the auth server on each request (token introspection) if not needed; validate once at the gateway, then trust the token’s claims in internal calls 
(this can be done securely by signing tokens and using a shared public key, etc.).
REASONING: This separation follows the principle of single responsibility and least privilege. The gateway is the gatekeeper – it should stop any request that isn’t properly authenticated. 
This offloads that work from services and centralizes authentication (which is easier to update and audit in one place). 
Meanwhile, services are the experts in their own data and rules – they are best positioned to enforce who can do what within that domain (like “only a user with 
ADMIN role can add new genres” – the GenreService would check role before proceeding). If the gateway tried to do that, it would need a lot of duplicated knowledge of each service’s rules and likely become a bottleneck of complexity. 
If services tried to re-authenticate every call, it would add overhead and potential inconsistency (if one service interprets token differently than another). 
By trusting tokens from the gateway, you improve performance and maintain a clear trust boundary. Each piece does what it’s best at: gateway for auth, service for business logic. 
This also means if you have multiple different client types or multiple gateways, each can handle auth in its realm, but the core services remain consistent.
EXCEPTION: In some setups, internal services might need to perform a token introspection (say, if there is no gateway in a particular integration). 
But ideally, internal calls are secured by network policies or mutual TLS, etc., and the tokens can be trusted as-issued. 
If zero trust is in place, each service could validate signature of the JWT using a public key – that’s okay (that’s not re-calling the auth server, it’s just cryptographic verification). 
Just avoid each service independently calling the auth server unless absolutely necessary (e.g., to get user profile info – even that could be cached or included in token claims).

#Testing
##Prefer Slice Testing for Individual Layers
REJECT: Using heavy, full-application context tests (@SpringBootTest) for every scenario, including those that only need a small portion of the application, resulting in slow test suites.
REQUIRE: Use Spring’s slice test annotations to test components in isolation with a lighter context. For example:
- Use @WebMvcTest for controllers: this brings up the web layer (controllers, filters, etc.) with MockMVC, without starting the entire application or hitting the database. 
You can mock service beans and test HTTP request/response behavior, validation, and error handling in the controller.
- Use @DataJpaTest or @JdbcTest (depending on whether JPA or plain JDBC Template is used) for repository tests: this will configure an in-memory database and the Spring Data/JDBC components, without the web layer or full business logic. 
It usually also rolls back after each test to keep the DB clean.
- Use plain JUnit tests (no Spring context) for simple service methods or utility classes where possible, to run them fastest. You can instantiate the service with a mock repository manually if needed rather than loading Spring.
Reserve @SpringBootTest (or full integration tests) for scenarios where you need everything together – for example, ensuring that the whole application wiring works, 
or an operation that traverses multiple layers and perhaps external integrations. Those should be fewer in number.
REASONING: Slice tests run much faster because they only load a fraction of the application. This leads to a faster feedback cycle for developers and allows having a large number of tests without the suite becoming unmanageable in time. 
It also makes it easier to pinpoint issues: if a web test fails, you know the problem is likely in the controller or its immediate configuration, rather than somewhere deep in the service. 
By contrast, a full context test could fail for a variety of reasons. Additionally, isolating layers means you can use mocks to simulate boundaries (e.g., simulate service exceptions to see how the controller handles them, 
or simulate repository behavior in a service test). This yields more thorough testing of edge cases (like service throwing NotFoundException -> does controller return 404 properly). 
In summary, using the right tool for the test improves both speed and coverage.
EXCEPTION: Sometimes writing a slice test is not straightforward (for example, testing security configuration might require a full context, or some beans aren’t easily sliced). 
In those cases, a limited number of full integration tests is fine. The guiding principle is to avoid doing everything with the heaviest approach when a lighter one will do.

#Use Realistic Databases for Integration Tests
REJECT: Using an in-memory database (H2 in mem mode, etc.) for tests that involve complex SQL, especially if the production database is different (e.g., PostgreSQL or MySQL), which can mask SQL compatibility issues or performance differences.
REQUIRE: For any integration test that touches the database layer in a way that depends on DB-specific behavior (such as custom queries, JSON columns, timestamp handling, case sensitivity, etc.), 
run those tests against the same type of database as production. This is often achieved with Testcontainers: e.g., using a PostgreSQL Docker container that starts up for the test suite and is initialized with the schema. 
Spring Boot can be configured (profile "test") to use the container’s JDBC URL. Alternatively, have a dedicated test database instance. 
The idea is to test your repositories and data access logic in an environment as close as possible to real. H2 is fine for basic CRUD sanity tests, but if you rely on, say, LIKE queries 
(which may behave differently in H2 vs Postgres) or use sequences, JSON columns, etc., those should be verified on the real thing. 
Also, if you tune queries for performance, the execution plan on H2 is irrelevant – you want to know how Postgres/MySQL does it.
REASONING: While H2 is a convenient default for tests (fast, in-memory), it isn’t 100% compatible with other databases. 
For example, H2 might allow certain syntax that PostgreSQL rejects, or handle string comparisons in a case-sensitive way when MySQL wouldn’t. 
Relying solely on H2 can give false confidence – your tests pass, but in production the application might fail or behave incorrectly. By using the actual database engine in tests, you catch these discrepancies early. 
Testcontainers has made it practical to run, say, a PostgreSQL instance during tests with fairly low overhead. 
Though it’s slower than H2, you wouldn’t do it for every test, just the ones that matter (perhaps in a separate integration test phase). 
This approach improves reliability of the test suite as a true validator of production behavior. It also allows you to test DB-specific features (like a PostGIS spatial query, or a MariaDB fulltext index) that H2 simply can’t emulate.
EXCEPTION: If the project is using purely standard SQL and very basic queries, H2 (or an H2 compatibility mode for another DB) might suffice for all tests. But this is uncommon in larger projects. 
Generally, it’s safest to assume differences will crop up and to include at least some tests with the real database.

#Thoroughly Test the Persistence Layer
REJECT: Assuming that just because the unit tests for services or the old in-memory implementation passed, the new database-backed implementation will work correctly without dedicated testing.
REQUIRE: Develop comprehensive integration tests for all CRUD operations and key queries in the persistence layer once a database is introduced. For example:
- Film Storage Tests: Create a film via filmStorage.create(film) and then retrieve it with getById to ensure it was saved correctly (check that the returned film’s fields match, that it got an ID, that associated objects like 
MPA have their names, and that genres are present without duplication). Update a film (change some fields, add/remove genres and likes) via filmStorage.update and then verify getById reflects those changes (correct genre list, 
updated like count or set, etc.). Test edge cases like updating a non-existent film (should throw NotFoundException). Test the custom query for popular films: insert multiple films and likes, 
then call findMostPopular(n) and verify it returns films in the correct order (the one with most likes first, ties broken consistently) and with correct limiting.
- User Storage Tests: Similar approach: create a user and verify retrieval. Update the user’s data and friends list, then verify retrieval and friend relationships. 
Ensure that adding a friend (one-sided) is persisted (maybe by checking the friendships table via another call or indirectly via the service common friends logic). 
Delete a user and verify they are gone (and possibly that cascading removed their friendships or likes).
- Genre and MPA Tests: Ensure that findAllGenres returns all the genres that were pre-populated (and getById works for each valid ID, and throws for an invalid ID). This confirms your reference data is correctly read from the DB.
- Cross-Checks: If the service layer has logic combining data, you can test via the service or controller as well (for example, using MockMvc to simulate an HTTP call that creates a film and then fetches it via API to see end-to-end). 
But primary focus is on the repository methods doing what they promise.
Use the same schema and initial data in tests as in dev (for example, run the schema.sql and data.sql on the test database at startup, which Spring can do if you set spring.sql.init.mode=always in test profile). 
This way, your tests operate with a realistic starting point (e.g., known genres and ratings are already there).
REASONING: When migrating from an in-memory implementation to a database, many things can go wrong: SQL mistakes, transaction issues, constraint violations, or logic that was slightly different. 
Only by testing the actual persistence layer can you be confident that, for example, “deleting a film removes its likes” or “updating a user’s friends correctly overwrites the old list in the DB” as expected. 
These tests act as a safety net for the new code. They also serve as documentation of how the persistence layer is supposed to behave. 
Running them gives immediate feedback if a future change breaks the persistence logic (like a refactor of a query). 
Essentially, you want the same confidence in your DB-backed repositories as you had in your in-memory ones, and that comes from exercising them thoroughly in tests. 
Integration tests here complement unit tests: they test the real interaction with the DB, including the effects of constraints, the correctness of SQL, and the mappings. 
Without them, you might only discover issues during runtime with real data, which is far more costly.
EXCEPTION: None. Any significant change to how data is stored/retrieved (like introducing a DB) warrants a full round of tests. 
Even if some duplicative tests exist at the service level, it’s worth testing the repositories directly for precise control and observation of database state.

#General Best Practices
##Program to Interfaces, Not Implementations
REJECT: Declaring variables, method parameters, or return types with concrete classes when an interface type would suffice (e.g., using ArrayList or HashMap in declarations instead of List or Map).
REQUIRE: Use the most general applicable type (usually an interface or abstract class) for declarations. 
For example, if a method returns a collection of movies, declare it to return List<Film> rather than ArrayList<Film> – the caller doesn’t need to know the specific implementation, only that it’s a list. 
Likewise, accept List<String> as a parameter if that’s all you need, rather than insisting on a specific implementation like LinkedList. 
Internally, you can instantiate a specific class (e.g., new ArrayList<>()), but do not expose that in your method signatures or variables unless you have a good reason. 
This principle also extends to other areas: use Path or File (from java.nio or io) rather than FileInputStream in APIs, or use an interface type for service references 
(like UserService interface) rather than a concrete class type, to allow swapping implementations.
REASONING: Programming to interfaces makes your code more flexible and easier to refactor. If later you want to change the implementation (say from ArrayList to LinkedList or some custom list), 
you can do so without breaking code that uses the variable or method – since they only knew about the interface List. It also communicates intent: 
for instance, returning a List signals "I offer list semantics" without committing to how it’s backed. It can also aid testing, because you can use different implementations (or mocks) that adhere to the same interface. 
Overall, it reduces coupling; code depends on the contract (interface) rather than the concrete details. This practice is a core part of good API design in Java.
EXCEPTION: If a specific implementation has a behavior the caller must rely on, you may need to expose it. For example, if order matters and you specifically use a LinkedHashSet to preserve insertion order, 
returning just a Set might be misleading because not all Set implementations preserve order. In such cases, document it clearly (or use a more specific interface like SortedSet if applicable). 
But as a general rule, stick to interfaces or abstract base classes for types in signatures.

#Avoid Magic Numbers and Duplicated Constants
REJECT: Scattering literal values throughout the code (numbers, strings, etc.) that have inherent meaning, leading to potential inconsistencies and unclear purpose.
REQUIRE: Define constants for values that are used in multiple places or carry special significance. For example, if the default page size or list limit is 10 in several queries, 
define a constant DEFAULT_PAGE_SIZE = 10 in a common location and use it everywhere instead of the raw number 10. If a specific SQL snippet is reused (like a base SELECT clause for films that joins the MPA name), 
consider making it a constant or at least a single method that provides it, rather than duplicating the string in each repository method. Similarly, status codes, error messages, 
or regex patterns should typically be defined once. Use public static final in a class (or enum for a set of related constants) to hold these values. 
This also applies to things like date formats (have one constant for a date format pattern) or file paths.
REASONING: Repeating literals can lead to errors and inconsistencies. If a business rule changes (say the default page size should be 20 now), you might update it in one place and forget another, causing unpredictable behavior. 
A constant guarantees a single source of truth. It also gives a name to the value, making code more readable – seeing DEFAULT_PAGE_SIZE is clearer than seeing 10 and wondering what it represents. 
For strings like SQL queries, having a centralized constant can simplify changes (e.g., if the table name changes, you update one string). 
Moreover, it reduces the chance of typos – if you copy a long string literal in five places, one might accidentally have a typo; a constant avoids that.
EXCEPTION: Truly obvious literals can be left inline (like for(int i=0; i<10; i++) where 10 is just part of a trivial loop, or checking if (statusCode == 404) in a test context). 
But if the number 404 appears across the app as an error code, better to have a named constant. Use judgment: the more a literal’s meaning might change or the more places it appears, the more it needs a constant. 
Small, self-contained uses can sometimes be okay.

#Do Not Reassign Method Parameters
REJECT: Modifying the value of a method’s input parameter inside the method, thereby using the parameter variable as a general-purpose local variable.
REQUIRE: Treat method parameters as read-only inputs. If you need a different variable to perform calculations or transformations, declare a new local variable. 
For example, if a parameter count might be null or zero and you want to default it, do:

int effectiveLimit = (count <= 0) ? DEFAULT_PAGE_SIZE : count;  
and then use effectiveLimit for the rest of the method. Do not do:

if (count <= 0) {  
    count = DEFAULT_PAGE_SIZE;  
}  
// then use count  
Similarly, if you need to trim a string parameter, don’t param = param.trim(), instead do String trimmed = param.trim(). This way, the original param remains unchanged.
REASONING: Reassigning parameters can confuse the reader and lead to mistakes. 
The parameter, by definition, represents what was passed in – if you change it, by the end of the method it doesn’t hold the original value, which can be misleading during debugging or reading code. 
It’s clearer to introduce a new variable name for a modified value. 
Also, some static analysis or style tools flag parameter reassignments because they consider it a bad practice (it can hide bugs where you think you’re using the input but accidentally altered it). 
By keeping parameters immutable within the method, you make the code’s intentions clearer: parameters are inputs, locals are working variables. 
This aligns with functional thinking as well – treat inputs as values that shouldn’t be changed.
EXCEPTION: In some cases (especially with legacy code), you might see parameter reassignment to reuse a single variable instead of creating a new one for memory reasons, but modern Java compilers handle locals efficiently, 
and clarity trumps micro-optimizations. One potential soft exception: in constructors or setters, sometimes people use the same name with this.param = param; and maybe tweak the input before assignment – but even there, 
it’s often clearer to use a differently named input or a temp variable for processing.

#Clean Up Dead Code and Simplify Logic
REJECT: Keeping commented-out code, unused variables, or outdated methods in the codebase “just in case”, or writing overly complex routines when a simpler approach exists.
REQUIRE: Regularly remove or refactor code that is no longer needed. If a piece of code is disabled or commented out because it’s not needed now, delete it (source control history can retrieve it if necessary). 
Unused variables, imports, or methods should be eliminated to avoid confusion. Additionally, after implementing new features or fixes, review if any old workaround code can be removed or simplified. 
Strive to keep the codebase lean: every line of code should have a purpose. Also simplify logic where possible: for instance, if you find a method doing something in 10 steps that could be done via a standard 
library call or a clearer algorithm, refactor it. Use descriptive methods or comments to explain non-obvious logic, but prefer to write logic that is obvious when possible.
REASONING: Dead code can be misleading or even dangerous. Other developers might not realize a piece of code is not used and attempt to modify it or rely on it, wasting time. 
It also bloats the codebase and makes reading harder (“is this function actually called anywhere? what happens if I change it?”). Removing it reduces cognitive load – you only focus on what matters. 
Keeping code simple and intention-revealing reduces bugs. Complex, convoluted logic is harder to test and maintain. By cleaning up, you also often find opportunities for improvements and ensure that there aren’t multiple 
ways of doing the same thing lingering in the project. A tidy codebase with straightforward logic is much easier to work with, especially for new team members. Finally, unnecessary code can even have performance costs 
(if it’s executed or even loaded) and security implications (unused endpoints could expose something if accidentally enabled). It’s best to remove what’s not needed.
EXCEPTION: The only time to keep seemingly unused code is if it’s going to be used in the very near future and its presence is intentional (even then, better to feature-toggle it or keep it in a branch until ready). 
If you do leave it, it must be clearly marked with why it’s there (e.g., “TODO: enable this when feature X is launched”). But generally, prefer to remove and retrieve from Git when needed.

#Refactor and Reuse Query Logic
REJECT: Writing similar boilerplate code for handling query results or constructing queries in multiple places, leading to slight variations and potential inconsistencies.
REQUIRE: Abstract common database access patterns into helper methods or reusable components. For example, if several repository methods need to convert a ResultSet into a Map of IDs to objects, write a private method or use a 
ResultSetExtractor that does this mapping, and call it from each query instead of duplicating the loop each time. Or if building an IN clause for a variable list of IDs is done in multiple classes, 
consider a small utility to generate the ?, ?, ? placeholder string given a count, to avoid each class doing its own loop with off-by-one errors. Essentially, identify repeated SQL or JDBC patterns and consolidate them. 
Another instance: if multiple repositories use the same base SELECT (e.g., selecting all film fields plus MPA name), define that SELECT string once (in a constant or method) and use it everywhere, 
so you don’t accidentally select different columns in different places.
REASONING: Repetition in query logic can lead to errors and divergence. If each developer writes their own version of a friends extractor from a ResultSet, one might forget to close resources, another might use a different data structure. 
By providing a standard method, you ensure uniform behavior and reduce the chance of mistakes. It also shortens code, making repository methods more concise and focused on what query to run rather than how to process it 
(since the "how" is handled by the common helper). If the schema changes (say you add a new column that should be included in all film queries), having a single place to change the base query or mapper makes 
life easier and guarantees consistency. Essentially, treat repetitive data access tasks just like any other duplicated code – DRY them up. This improves maintainability and clarity of the code.
EXCEPTION: If only two places share a logic and abstracting it would add indirection without much benefit, it might be okay to leave as is. Use good judgment on when a piece of logic is general enough to be pulled out. 
Err on the side of reuse when the logic is non-trivial or likely to be needed elsewhere.

#Prefer Standard SQL and Portable Constructs
REJECT: Using vendor-specific SQL features or non-standard syntax when standard SQL can achieve the same result, thereby locking the code to a particular database unnecessarily.
REQUIRE: Write queries and database interactions using ANSI-standard SQL as much as possible. 
For example, instead of using an H2-specific MERGE INTO ... or MySQL’s REPLACE INTO, consider using a standard INSERT followed by an ON CONFLICT/ON DUPLICATE KEY 
(if portability between Postgres and MySQL is needed, you'd handle that carefully, or better yet handle logic in application or use UPSERT only where you know your target DBs support it similarly). 
Avoid non-standard functions or syntax unless you know you are targetting a single DB engine and that feature is crucial. If you do use a DB-specific feature (like Postgres’s JSONB functions or MySQL’s FULLTEXT search), 
isolate those queries in the repository layer and document them clearly, possibly with fallback or alternative implementations for other DBs if needed. 
The general approach is: if an ordinary join, subquery, or standard function (SUM, COUNT, etc.) can do it, prefer that over fancy extensions.
REASONING: Sticking to standard SQL makes your application more adaptable to different environments. For example, during testing you might use H2, but production is 
PostgreSQL – if you kept to common SQL, the tests will run more reliably and any switch or addition of a new environment is simpler. If one day the company decides to move to a different database, 
the fewer proprietary SQL bits you have, the easier that migration will be. Even within a single DB, standard constructs are usually well-optimized and understood by developers, whereas esoteric features might have pitfalls. 
Additionally, new team members are more likely to be familiar with standard SQL; a complex vendor-specific query might be harder to maintain. There are times when a proprietary feature gives a big benefit 
(performance or simplicity) – those should be chosen deliberately and sparingly, with full awareness of the trade-off. In summary, prefer portability and simplicity unless a strong case is made.
EXCEPTION: When a database-specific feature is essential for performance or functionality and you are confident the application will only run on that database (or you provide different implementations per database), 
it’s acceptable. In such cases, encapsulate the use of that feature so it’s easy to locate and change if needed. Also, ensure tests cover it as much as possible (perhaps using the same DB engine in tests via Testcontainers, 
per previous guideline).

