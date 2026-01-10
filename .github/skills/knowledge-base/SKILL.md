> IMPORTANT — Mandatory Knowledge Base (ALWAYS APPLY)
> These rules are global and apply to EVERY task without exception.
>
> This knowledge base is split into 3 files due to size limits. Treat them as ONE document.
> Before answering or proposing changes, you MUST read and follow ALL parts:
> - SKILL.md (Part 1/3)
> - SKILL_APPENDIX.md (Part 2/3 — continuation, mandatory)
> - SKILL_PART3.md (Part 3/3 — continuation, mandatory)
>
> Do not ignore Part 2 or Part 3. If any part is not available in context, request it / open it.

---
name: knowledge-base
description: General knowledge base. Use it as default context: before answering or proposing code changes, check this knowledge base (style, approach, templates, rules, commands).
---

# General knowledge Base

This skill provides shared context for working across any project.  
When answering questions, suggesting code changes, or outlining a plan, **first check this knowledge base**, then apply its guidance to the current project.

Below is the knowledge base content.

##Code Style and Formatting

#Always Use Braces for Blocks
REJECT: Omitting braces in control structures (e.g. writing an if or for without { }).
REQUIRE: Always include braces in if, else, for, while, and do constructs, even if the body is a single line or empty. For example:

if (condition) {
    doSomething();
}
and not:

if (condition) 
    doSomething();
REASONING: Including braces for all blocks improves readability and prevents errors when modifying code. If a new line is added to a single-line if without braces, it could unintentionally execute outside the intended block. 
Consistent use of braces makes the code’s structure clear and maintenance safer.
EXCEPTION: None – the team convention is to use braces universally (per style guides like Google Java Style).

#Use Clear and Descriptive Names
REJECT: Using single-letter or non-descriptive names for variables and methods (e.g. n, temp) except for very short-lived loop indices.
REQUIRE: Choose meaningful names that reflect the variable’s role or the method’s intent. For example, use names like taskList or userEmail instead of t or data. 
Method names should describe the action or calculation performed (e.g. calculateTotal() rather than doIt()). Class and field names should likewise be self-explanatory.
REASONING: Clear naming makes code self-documenting. When variables and methods describe their purpose, other developers can understand code logic without extra comments.
This reduces misinterpretation and errors. While loop counters like i or j in short loops are acceptable by convention, most contexts benefit from more explicit names.
EXCEPTION: Very limited-scope uses (such as i in a 3-line loop or mathematical formulas) can use terse names. In all other cases, prefer clarity over brevity.

#Maintain Consistent Formatting
REJECT: Using unconventional spacing or alignment just for visual appeal, such as manually lining up code in columns or mixing tabs and spaces inconsistently.
REQUIRE: Adhere to a consistent code formatting standard across the project. Use normal indentation (e.g. 4 spaces per level in Java), standard spacing around operators and keywords, and typical line breaks.
Do not align unrelated code elements in columns; instead, rely on the standard indentation rules. Ideally, use an automatic formatter or the project’s style settings to enforce consistency.
REASONING: Consistent formatting enhances readability and ensures that code appears uniform regardless of author. Artificial alignment often breaks as soon as code changes, making maintenance harder. 
By following common conventions (such as those in widely-used style guides), the codebase will be easier for any developer to read and modify.
EXCEPTION: None – formatting standards should be applied universally. Any deviation should be discussed and agreed upon by the team, not done ad-hoc.

#Automate Style Compliance (Checkstyle)
REJECT: Relying only on manual inspection or code reviews to catch style and formatting issues.
REQUIRE: Use automated style-checking tools (such as Checkstyle or integrated IDE linters) configured with the team’s conventions. 
Enable these in the development environment and CI pipeline to flag deviations (e.g. missing braces, wrong indentation, line length violations) immediately. 
This includes running tools like Checkstyle or SonarLint with a ruleset (for example, Google Java Style) that the project abides by.
REASONING: Automated tools provide instant feedback on stylistic mistakes, ensuring issues are caught early – often as code is written or before it’s merged. 
This reduces the burden on code reviewers to point out minor style problems and helps maintain a clean codebase. It also ensures consistency: every developer’s code is checked against the same standards, preventing subjective differences.
EXCEPTION: Minor formatting preferences not covered by the tool can be handled via team convention, but the majority of style rules should be enforced automatically to maintain uniformity.

#Isolate Formatting-Only Changes
REJECT: Mixing code formatting fixes or cosmetic refactoring with functional changes in the same commit or pull request.
REQUIRE: Make formatting changes in separate commits (or PRs) from functional code changes. For example, if you decide to reformat a class to meet style guidelines, commit that change by itself, without any logic changes included. 
Conversely, when implementing a feature or bug fix, avoid including unrelated reindentation or renaming in those commits.
REASONING: Separating formatting-only changes from functional changes makes code reviews and troubleshooting easier. 
Reviewers can skim a pure formatting commit quickly, since it should not alter behavior, and focus their attention on commits that change logic. 
In case a formatting change causes an unexpected issue, it can be reverted independently without rolling back bug fixes or features. This approach leads to a clearer project history and simpler merges.
EXCEPTION: Trivial whitespace or comment tweaks that clarify a single functional change can be included with that change. However, comprehensive reformatting (affecting many lines/files) should always be isolated.

#Code Structure and Organization
##Exclude Generated or Temporary Files from Version Control
REJECT: Committing compiled binaries, build artifacts, or local environment files into the source repository (e.g. .class files, target/ or out/ directories, IDE config files like .idea/).
REQUIRE: Configure version control ignore rules to omit any files that are generated by the build or specific to an individual’s environment. 
Only source code, configuration, and necessary assets should be tracked. For example, add entries to .gitignore for build output directories, dependency caches, and personal settings. 
Ensure that another developer can clone the repository and generate these artifacts (classes, jars, etc.) via the build process, rather than retrieving them from version control.
REASONING: The repository should contain the single source of truth needed to build and run the project, not the results of a build or user-specific settings. 
Including generated files causes repository bloat and merge conflicts (since binaries change with every build). It can also inadvertently expose machine-specific paths or secrets. 
By keeping the repo clean, you guarantee reproducibility: everyone builds from the same sources to get the outputs.
EXCEPTION: In rare cases, certain generated files that cannot be produced on the fly (for example, a specific codegen output or an SQL schema snapshot required for reference) might be versioned. 
Such exceptions should be well-documented. Generally, if it can be regenerated or is user-specific, it does not belong in source control.

#Eliminate Duplicate Code
REJECT: Copy-pasting code or writing the same logic in multiple places in the codebase.
REQUIRE: Refactor and unify repeated code into single methods or utility classes. If you notice similar code blocks in different classes or modules, extract them into a common helper. 
For instance, if several test classes or controllers perform identical JSON parsing or HTTP request setup, implement that once (e.g. a HttpTestUtil.sendRequest() method) and reuse it everywhere. 
Likewise, if two pieces of code compute the same value or follow the same algorithm, consolidate that logic so it lives in one place.
REASONING: Duplicate code is a maintenance burden and a bug risk. When requirements change or a bug is found, having one authoritative implementation means you fix it in one place. 
If code is duplicated, it's easy to miss one copy, leading to inconsistent behavior. 
Unifying duplicates reduces the code size and complexity, and makes the system easier to reason about (every piece of functionality has a single implementation).
EXCEPTION: Occasionally, what looks like duplicate code may need to remain separate due to different context or performance reasons. Such cases are uncommon; even then, try to factor out any truly identical subsets of code into shared helpers. 
In general, strive for zero duplication: Don't Repeat Yourself (DRY) principle.

#Consolidate Small Utility Functions
REJECT: Defining tiny helper functions (parsing or simple checks) repeatedly in multiple classes or packages.
REQUIRE: Create common utility classes for frequently used small operations. 
For example, if many controllers or services need to parse an identifier from a string or check if an ID represents a "new" entity, implement a single utility (e.g. an IdUtil or HttpUtil class with methods like parseId(String) and isNewId(id)).
Use that utility method everywhere instead of writing the same parsing or check logic each time. This also applies to any repeated validation or conversion logic – centralize it.
REASONING: Even simple code, when duplicated, carries the risk of diverging behavior and increases cognitive load (a developer must recognize multiple implementations as essentially the same thing). 
A shared utility ensures consistency – if the parsing logic needs to change, it’s changed once. 
It also makes the code more readable; a well-named method (like isNewId()) communicates intent better than a comparison scattered throughout the code.
EXCEPTION: None. If a utility method truly only makes sense in one class, it isn’t a "common" utility. But if you find yourself writing similar helper logic in more than one place, that’s a signal to consolidate it.

#Handle Similar Operations Uniformly
REJECT: Implementing the same kind of operation in different ways for different cases, leading to redundant patterns.
REQUIRE: Use a consistent approach for operations that are conceptually the same. 
If multiple parts of the code need to perform a similar task (e.g., calculating a maximum ID among various collections, formatting output strings, etc.), find a single generalized solution. 
For example, if originally tasks, epics, and subtasks each had a separate loop to find the max ID, refactor to use one method that checks all, or use a stream with a max operation that works for any list. 
Strive to write one routine that can handle all similar cases, or at least follow the same pattern in each place.
REASONING: Writing uniform code for similar operations improves both maintainability and performance. It reduces the total code to test and understand. 
Moreover, if an operation needs to be optimized or fixed, having one implementation means a one-time change. 
A consistent pattern also helps new developers – once they understand one occurrence, they understand the others. Divergent implementations might accidentally evolve differently and introduce inconsistent behavior over time.
EXCEPTION: If two operations appear similar but have essential differences (different edge cases or requirements), a single abstraction might be too cumbersome. 
In such cases, implement them separately but still try to apply common patterns and naming. Ensure each variant clearly documents why it differs.

#Implementation and Design
##Prefer Polymorphism Over Type Checks
REJECT: Using instanceof checks or type-based switches to differentiate behavior between subclasses or related types.
REQUIRE: Utilize object-oriented principles by giving each class or subclass responsibility for its own behavior. 
For example, if Task, Epic, and Subtask need to be saved to CSV in different ways, have each class implement a toCsv() method (perhaps via a common interface or abstract class). 
Then simply call task.toCsv() polymorphically, instead of writing a big method that checks the type of task and then processes accordingly. 
This applies to any scenario where behavior varies by type: embed the variation in the types themselves (via overriding methods) rather than external conditional logic.
REASONING: Polymorphism makes code more extensible and cleaner. It adheres to the Open/Closed Principle – new types can be added without modifying existing logic, just by providing new implementations. 
It also eliminates error-prone casting and type checks scattered through code. The code that uses the objects doesn’t need to know about the specific subclasses, reducing coupling. 
Overall, the design becomes easier to maintain and extend, as each class knows how to do its part.
EXCEPTION: When working with types you cannot modify (external library classes) or when the behavior absolutely doesn’t belong in the type, you might use type checks. 
But in the context of your own class hierarchy, repeated instanceof or type-based switch statements are usually a sign that functionality should be moved into the objects themselves.

#Preserve Identifiers on Data Import
REJECT: Loading or restoring data through code paths that unintentionally generate new IDs for existing records (e.g., using the normal "create" method which assigns a new ID when you are actually rehydrating an object from storage).
REQUIRE: When reloading entities from a file or database (for example, on application startup or import), use methods that allow you to retain their original identifiers. 
This might mean bypassing the usual ID generator. For instance, add the reconstructed objects directly into the in-memory store or use a special import initializer that sets the ID explicitly without incrementing the sequence. 
The goal is that after restoration, each task/user/entity keeps the same ID it had when saved.
REASONING: Many parts of the system (relationships, references, order) rely on IDs being consistent. 
If all IDs change upon every load, you cannot reliably, say, restore links between an Epic and its subtasks, because the subtask IDs will no longer match what the Epic has recorded. 
Preserving IDs ensures that references remain valid and that the system’s state after loading is exactly as it was when it was saved. 
It avoids the confusion and errors that would arise if objects essentially became new entities on each load.
EXCEPTION: If there’s a conflict (e.g., importing data into an environment where those IDs are already taken), a more complex merge or remapping strategy is needed. 
Barring such scenarios, a straightforward data restore should never alter identity fields.

#Optimize Aggregate Calculations
REJECT: Making multiple passes over data to compute related aggregate values separately.
REQUIRE: Compute related aggregates in one iteration when possible. 
For example, when updating an Epic based on its subtasks: instead of looping once to calculate overall status, again to sum duration, and yet again for start/end times, combine these into a single loop through the subtasks. 
During that one traversal, determine the Epic’s status (e.g. if any subtask is IN_PROGRESS), accumulate the total duration, and track the earliest start and latest end times. After the loop, set all aggregated fields.
REASONING: A single pass approach is both clearer and more efficient. 
It reduces the time complexity (one O(n) instead of several O(n) operations) and keeps the aggregation logic localized in one place. 
This not only improves performance (especially if the list of subtasks is large) but also ensures consistency (all calculations are based on the same snapshot of data). 
The code becomes easier to maintain because there's only one loop to update if the logic changes, rather than multiple sections that must stay in sync.
EXCEPTION: If certain aggregates truly require different iterations (maybe one needs sorted data, etc.), you might not combine them. 
But even in those cases, carefully consider if sorting once and doing all calculations together is possible. Generally, avoid repetitive looping for related computations unless absolutely necessary.

#Restrict Access to Internal Methods
REJECT: Defining internal helper methods as public in your classes, especially if those methods manipulate an object’s internal state that should be guarded.
REQUIRE: Limit the visibility of methods that are intended only for internal use. If a method exists solely to help a manager or service class perform calculations or state updates, make it package-private or private. 
For example, an Epic class might have methods to update its calculated fields (calculateDuration(), etc.); these should not be part of the public API of Epic if external classes should never call them. 
Instead, the task manager can call package-private methods (if in the same package) or perform the calculation logic itself and then set fields via a controlled interface. 
The public methods of a class should represent meaningful operations that any client might legitimately invoke, nothing more.
REASONING: Keeping implementation details hidden maintains encapsulation. 
Public methods are promises to external code – once you expose a method, other parts of the codebase (or external consumers) might start depending on it. 
If those methods are not meant to be used externally, this opens up the possibility of misuse (e.g., someone calling an internal update out of sequence) and makes future refactoring harder 
(because removing or changing a public method can break other code). By restricting access, you can change internal workings freely and keep object state transitions under control.
EXCEPTION: If certain internal methods must be exposed (for example, to facilitate unit testing in absence of better hooks), they should be clearly documented as internal-use-only. 
A better approach, however, is usually to test via the public interface or use package-private access for tests, rather than compromising the class’s API design.

#Keep Business Logic Out of Controllers
REJECT: Performing significant business logic or validations in the controller (HTTP layer) rather than in the service or manager layer.
REQUIRE: The controller layer should mainly handle HTTP request/response mapping and leave the core logic to the underlying service. 
For instance, when updating an entity, the controller should not itself check if the entity exists or if the update is allowed – instead, call the service’s update method and handle exceptions it throws 
(like NotFoundException if the entity isn’t there). This means existence checks, consistency rules, and any conditional updates are implemented in the service/manager. 
The controller catches expected exceptions and translates them to HTTP status codes or messages, but it does not duplicate the validation logic.
REASONING: Separating concerns leads to cleaner, more maintainable code. 
Controllers become lightweight and similar to each other (simply delegating and mapping results to HTTP), and all the important logic is concentrated in one place. 
This way, whether the service is called from a REST API, a unit test, or a future batch process, the same rules apply. 
It prevents inconsistencies where one code path validates something and another doesn’t. 
Additionally, by throwing exceptions for error cases in the service, you centralize error handling – the controller just converts those to standardized error responses, improving consistency of API output.
EXCEPTION: Basic input parsing and validation (such as checking for malformed data or required fields) can happen in controllers or via validation annotations – these are more about translating bad requests to a proper response. 
But anything that involves business rules or data existence (for example, "a user cannot like a film twice") should reside in the service layer.

#Use Long for ID Counters
REJECT: Using a 32-bit Integer type for ID generators or counters that could potentially exceed the integer range over time, or using non-atomic counters in multi-threaded contexts.
REQUIRE: Define ID counters as long (64-bit) to allow a practically unlimited range for auto-generated IDs. This ensures the application won’t overflow the ID space even if it runs for years or handles a very large number of entities. 
Additionally, if IDs are generated or incremented in a concurrent environment, use thread-safe constructs like AtomicLong for the counter, or synchronize the increment operation, to avoid race conditions. 
This guidance applies to any sequence generator (primary keys, request counters, etc.).
REASONING: While Integer.MAX_VALUE (around 2.1 billion) seems high, systems can reach this if they run long enough or have heavy usage, leading to an overflow bug that can be catastrophic (duplicate IDs, negative values, etc.). 
Using a long is a simple way to future-proof the ID system. Likewise, in multi-threaded apps, using an AtomicLong or equivalent prevents subtle bugs where two threads might get the same ID. 
It’s a low-cost change that increases the robustness and scalability of the system.
EXCEPTION: For quick prototypes or scenarios with an absolute guarantee (backed by checks) that IDs will remain small, an int can be used. However, switching to long has almost no downside in modern Java, 
so it is generally advised for any non-trivial project to avoid unnecessary limits.

#Avoid Logging Sensitive Data
REJECT: Logging entire objects or detailed data structures directly, especially if they may contain sensitive or irrelevant information (for example, printing a full user object with passwords or personal data to the logs).
REQUIRE: Be selective in what you log. Log messages should convey what happened and key identifiers, but not dump full internal state. 
In practice, this means logging only necessary fields (such as an entity’s ID and perhaps name) rather than the whole object. 
For instance, in a controller handling a user update, log “User [id] updated” instead of logging the entire User object JSON. Review all log statements to ensure no private data (addresses, tokens, etc.) is included. 
This is important both in development/test projects and in production code.
REASONING: Logs often persist and are accessible to developers or operators; if they contain sensitive information, that can become a security and privacy risk. 
Moreover, overly verbose logs can clutter log files and make it hard to find relevant information. By whitelisting only necessary details in logs, you protect user data and keep logs focused and useful. 
It also reduces the chance of accidentally violating privacy regulations by storing personal data in plain text logs.
EXCEPTION: In a debugging scenario, you might temporarily increase log verbosity or log a full object to diagnose an issue, but such statements should be removed or turned off before committing code. 
In general, always err on the side of caution and assume logs will be seen by others – log accordingly.

#Use Appropriate Log Levels
REJECT: Logging all events at the same level (for example, everything as DEBUG or everything as INFO), or misusing levels (like logging an error as debug, or vice versa).
REQUIRE: Calibrate log levels to the importance of the message:
- Use DEBUG for detailed diagnostic information that’s mainly useful during development or troubleshooting (e.g. method entry/exit, detailed state dumps).
- Use INFO for high-level events in the normal flow of the application, such as startup messages, major business actions (e.g. “Order 123 created”), or successful completion of significant tasks.
- Use WARN for abnormal or unexpected events that are not critical but might indicate a potential issue. 
For example, if an operation could not find a record to delete (a user tries to remove a like that doesn’t exist), log a warning – it’s not a failure, but it is out of the ordinary.
- Use ERROR (or FATAL, if available) for serious problems where something has gone wrong that should be investigated, such as exceptions that will halt processing of a request or corrupt data scenarios.
REASONING: Proper log level usage makes it possible to filter and understand logs in production. 
Important issues won’t be lost in a sea of debug messages, and verbose output can be turned off in production to save space and I/O. 
When an unexpected situation occurs (like an invariant violation or a missing expected data condition), a WARN draws attention to it so it can be analyzed. 
On the other hand, routine operations stay at INFO for audit trail purposes, and deep internals remain at DEBUG to be enabled when needed. This stratification leads to logs that are both useful and manageable.
EXCEPTION: Teams may have slight variations in level definitions, but the general hierarchy (ERROR > WARN > INFO > DEBUG) and intent should remain consistent. 
Avoid using levels out of order (e.g., don't use ERROR for purely informational messages to force them visible – that should be solved by log configuration, not misleveling).

#Defensive Programming and Immutability
##Avoid Exposing Mutable Internal State
REJECT: Returning references to a class’s internal mutable objects, or storing external mutable references directly without copying.
REQUIRE: Implement defensive copying to protect mutable state: if a method needs to provide data from an internal List or Map, return a new ArrayList or an unmodifiable view rather than the original collection. 
Similarly, when accepting a collection or mutable object via a setter or constructor, copy its contents into the internal field instead of keeping the original reference. For example, in a constructor:

this.items = new ArrayList<>(itemsArg);
not

this.items = itemsArg;
This ensures the class’s internal items can’t be modified from outside. In getters, you might do:

return Collections.unmodifiableList(items);
to return a read-only view.
REASONING: In Java, objects are referenced, so if you expose a reference to a mutable object (like a list or a date), external code can change your class’s internal state without any checks, violating encapsulation. 
Defensive copying prevents clients of your class from corrupting or depending on its internal representation. It also signals that modifications should go through proper methods of the class. 
Without this, subtle bugs can occur, where something seemingly unrelated modifies an object held by another class. Defensive programming closes off that risk.
EXCEPTION: If an object is known to be immutable (e.g., instances of java.time.LocalDate or an already unmodifiable collection) or explicitly meant to be a shared mutable structure (rare in well-designed systems), 
you can safely pass references. But these are special cases – the default assumption should be to copy or protect mutable data.

#Prefer Immutability
REJECT: Overuse of mutable classes (especially mutable date/time classes like java.util.Date or Calendar), and not marking variables as final when they should not change.
REQUIRE: Embrace immutability wherever practical. Use the modern Java Date/Time API (java.time classes) instead of legacy Date/Calendar – the newer classes (e.g. Instant, LocalDate) are immutable and thread-safe. 
Design your data models to be immutable if possible: for example, value objects that have only final fields and no setters (any modification creates a new object). 
At the very least, declare fields and local variables final if they are not meant to be reassigned. This makes it clear they remain constant after initialization.
REASONING: Immutability greatly simplifies reasoning about code, because you know an object won’t change out from under you. It eliminates whole classes of bugs related to unintended side effects. 
Immutable objects are inherently thread-safe, since their state cannot change after construction. Marking variables final communicates intent and helps the compiler catch unintended reassignments. 
Using immutable library classes (like java.time or collections from Guava) further ensures safety and can improve performance by avoiding defensive copies.
EXCEPTION: Some objects (particularly entities managed by frameworks like JPA) need to be mutable for the framework to do its work. In such cases, confine mutability as much as possible (e.g., only allow changes within a controlled scope). 
Still use final for fields that are truly invariant (like IDs once set). Also, collections that need to change can be encapsulated behind methods rather than exposed. 
Immutability is a spectrum – apply it as thoroughly as you can without breaking necessary functionality.

#Database Persistence and Data Management
##Database Dependencies and Configuration
REJECT: Starting to use a database in the application without adding the proper drivers or forgetting to configure the connection properties, leading to runtime errors or using default (in-memory) settings unintentionally.
REQUIRE: Include all necessary database dependencies and configuration when introducing persistence. For example, add the Spring JDBC or JPA library and the H2 database driver to your project (pom.xml or build file). 
Then, in application.properties (or YAML), set up the connection URL, driver class, username, and password for H2. A typical configuration for a file-based H2 database might be:

spring.datasource.url=jdbc:h2:file:./db/filmorate  
spring.datasource.driverClassName=org.h2.Driver  
spring.datasource.username=sa  
spring.datasource.password=  
Additionally, enable automatic initialization of the schema and data if using SQL scripts: e.g., spring.sql.init.mode=always so that on startup Spring will run schema.sql to create tables and data.sql to insert reference data. 
Verify that these scripts are placed in the classpath (usually src/main/resources).
REASONING: Proper configuration is critical for the application to successfully use the database. Without the driver dependency, the app cannot establish JDBC connections.
 Without the correct URL or initialization settings, the database might not be created or seeded as expected. 
 By explicitly configuring these, you ensure that the application’s persistent storage is ready at runtime and consistent across different environments (dev machines, CI, etc.). 
 It also prevents subtle issues like using an ephemeral in-memory DB when you intended a file or server, which could lead to data loss on restart.
EXCEPTION: None – every environment should have an explicit database configuration. 
The only variation might be different URLs or credentials for test and production, but those should be managed via profiles or environment-specific config, not left to defaults.

#Proper Database Schema Definition
REJECT: Defining an incomplete or inconsistent database schema, or neglecting to enforce relationships at the database level (for instance, missing foreign keys and constraints).
REQUIRE: Design and maintain a comprehensive schema for your database that mirrors the application’s data model. This means creating tables for each major entity (e.g., films, users) with clearly typed columns for each field. 
Include primary keys on each table (usually an id column) and define foreign key constraints for references (for example, a films.mpa_id foreign key to an mpa_ratings table, or a film_genres.film_id FK to films.id). 
Use junction tables (like film_genres(film_id, genre_id) or friendships(user_id, friend_id)) for many-to-many relations, and enforce keys on those as well. 
Add indexes where appropriate (e.g., index foreign key columns or columns used in lookups). If using SQL migration tools (Flyway/Liquibase), each schema change should be captured in a migration script so all environments stay in sync. 
Also, pre-populate static reference tables via scripts: for example, insert all genre types and MPA ratings into genres and mpa_ratings tables in an initial data script, so that these references exist before they’re needed by code.
REASONING: A well-defined schema ensures data integrity at the lowest level. 
Foreign keys prevent orphaned records (e.g., a like referring to a film that doesn’t exist), and cascades can automatically clean up related records (like deleting film likes when a film is deleted). 
Constraints (such as NOT NULL on required fields) catch errors that might slip by application logic. 
Preloading reference data means the application can rely on those values being present (for example, the list of genres to be always available from the DB). 
Essentially, the database becomes a guardian of the data’s consistency, catching mistakes that could otherwise cause runtime exceptions or corrupt state in the app.
EXCEPTION: During early development, you might start with a simpler schema (or even none, if using an ORM to auto-generate) but as soon as the model is clear, formalize it. 
No production or shared testing environment should run with an ad-hoc or implicit schema. Also, if an ORM is used, ensure its generated schema aligns with these rules (and if not, use DDL tweaks or migrations to enforce needed constraints).

#Introduce Domain Objects for Reference Data (Ratings & Genres)
REJECT: Using primitive values or ad-hoc structures to represent key domain concepts like movie ratings or genres (for example, just using an integer or string for a rating code throughout the codebase).
REQUIRE: Model each significant domain concept as its own class or entity. In the context of a film application, create an Mpa class to represent the movie rating (MPA rating) with fields such as id and name (e.g., id=1, name="G"). 
Similarly, define a Genre class for film genres with its own id and name. 
Then update the Film class to use these: instead of storing just a rating ID, a Film should have a field of type Mpa (meaning it holds an MPA object, which includes the name and id). 
Likewise, give Film a collection of Genre (e.g., Set<Genre> genres). 
Ensure that these fields are properly managed: for instance, a film’s genres set should be initialized to an empty set (to avoid null issues) and when setting it, copy the provided set 
(defensive copy) or use an immutable set to guard against external modification. Also use validation annotations or logic to guarantee that films always have a valid rating 
(non-null MPA) and perhaps limit genre list length, etc., according to business rules.
REASONING: By promoting ratings and genres to first-class objects, the code becomes more expressive and less error-prone. You can attach behavior or validation to these classes in the future 
(for example, a Genre could have a method to localize its name, or an Mpa might include an age limit). It also prevents passing around magic numbers or strings – instead of film.setRating(1) which is not self-explanatory, 
you do film.setMpa(new Mpa(1, "G")) or fetch an MPA object from storage. This layering also aligns with how data is stored in the database (with separate tables for ratings and genres), making it easier to map results. 
Overall, strong typing of domain concepts leads to clearer code and easier maintenance.
EXCEPTION: None. Even if initially you only have an ID for something like a rating, creating a class for it sets the stage for future expansion (like adding a description field, etc.). 
It’s a good practice to represent real-world entities in the model, even if they start with just an ID and name.

#Expose Reference Data via Controllers
REJECT: Failing to provide API endpoints for fundamental reference data, or embedding reference data logic awkwardly in unrelated endpoints.
REQUIRE: Create dedicated controllers to serve reference data entities such as ratings (MPA) and genres. 
For example, implement an MpaController mapped to /mpa that has endpoints to get all ratings and to get a rating by ID. Similarly, a GenreController for /genres. 
Each controller should call its corresponding service (e.g., MpaService, GenreService) to retrieve data from the database. Keep these controllers read-only (since these reference values are usually static or managed elsewhere). 
Also, include basic logging and error handling: log requests at an appropriate level (perhaps INFO for a single retrieval, DEBUG for listing all) and throw a NotFoundException (or respond with 404) if a requested ID doesn’t exist. 
Essentially, these controllers present a simple REST API for clients to obtain lists of genres or ratings, which can be useful for populating UI dropdowns, etc.
REASONING: Having explicit reference data endpoints makes the API more complete and self-describing. 
Clients of the API (front-ends or third parties) don’t have to hard-code the list of genres or ratings; they can fetch from the API, ensuring they use the same source of truth as the main application. 
It also encourages proper layering: rather than the Film controller doing double-duty by serving genre data, a separate controller cleanly separates that concern. 
Logging and standardized error responses in these controllers follow the same best practices as the rest of the API, resulting in consistent behavior.
EXCEPTION: If the application is not exposing an API (e.g., in a monolithic UI application), separate controllers might not be necessary – but the separation of logic is still beneficial. 
In an internal context, one might still have separate service methods to get genre lists, etc., to avoid mingling that with core business operations.

#Use Service Layer for Reference Data
REJECT: Accessing the database or repository for reference entities (genres, ratings, etc.) directly from controllers or other parts of the code without an intermediate service.
REQUIRE: Even for simple CRUD operations on reference tables, introduce a service layer. For example, an MpaService that provides methods like getAllMpa() and getMpaById(id), and a GenreService with getAllGenres() and getGenreById(id). 
These services should depend on repository interfaces (e.g., MpaStorage, GenreStorage) that hide the specifics of data access (JDBC, JPA, etc.). 
The service methods might be straightforward pass-through in the beginning (just calling the storage and returning the result), but having them allows you to add caching, combine results, 
or implement business rules later without changing the controllers. It also means the controllers don’t need to know anything about how data is retrieved.
REASONING: Introducing a service layer, even when it seems “over-engineered” for static data, pays off in consistency and future-proofing. 
All controllers in the application talk to services, not directly to DAOs, which makes the architecture uniform. If you later decide that genres should be cached in memory, you can do that inside GenreService transparently. 
Or if you add validation (maybe disallowing deletion of a genre if it’s in use), the service is the place to implement it. 
It’s much easier to maintain a clean separation: controllers handle HTTP, services handle business logic, repositories handle persistence. 
This also makes testing easier (you can unit test services without web, and potentially swap a mock storage for testing service logic).
EXCEPTION: If some reference data truly has no logic (pure read-only lookup), one might argue the controller could call the repository directly. 
However, for consistency and potential needs, it’s still recommended to have a thin service. The overhead is minimal (especially with dependency injection doing wiring), and it keeps your structure consistent.

#Use JDBC for Persistent Repositories
REJECT: Continuing to use in-memory data access objects (DAOs) or otherwise not persisting data after deciding to integrate a database.
REQUIRE: Replace any in-memory storage implementations with JDBC-based repositories that perform actual database operations. For example:
- Implement a FilmDbStorage class (annotated with @Repository) that implements the FilmStorage interface. Mark it @Primary if an in-memory version still exists, so Spring will inject the DB version by default. 
Use Spring’s JdbcTemplate for executing SQL queries and updates. Define row mappers to convert ResultSet rows into Film objects (e.g., map columns to fields, build the associated Mpa object from its ID and name).
- In FilmDbStorage.create(film): use a SimpleJdbcInsert or an INSERT SQL to add the film to the films table (populating fields like name, description, release_date, duration, mpa_id). 
Retrieve the generated primary key (film ID) and set it on the Film object. Then handle related tables: if the film has genres, insert records into a join table (film_genres) for each genre ID; if the film has likes (user IDs who liked it), 
insert those into film_likes. Clean up old relations first if this method is also used for updates. Finally, fetch the film back (or assemble it) to return a fully populated object (including the MPA name and genre set).
- In FilmDbStorage.update(film): perform an UPDATE films SET ... WHERE id=?. If no rows are affected (meaning the film wasn’t found), throw a NotFoundException. 
If update succeeds, synchronize related data: delete existing entries in film_genres and film_likes for this film (to remove old genres/likes), then insert the current genres and likes from the film object (similarly to create). 
This ensures the database reflects the film’s latest state. Return the updated film (querying it again or updating fields accordingly).
- Implement similar patterns for UserDbStorage (for User data). On create, insert into users table and get the new user’s ID. 
If the User object had any friends listed (which typically it wouldn’t on creation), insert those into a friendships table. On update, do an UPDATE users and throw NotFoundException if the user doesn’t exist. 
Then update friendships: remove all existing friendship rows for that user and insert rows for each ID in the user.getFriends() set (this set represents the user’s current friends). 
This effectively resets friendships to match the provided set. Fetch or construct a User with friends set for return.
- Provide simple implementations for reference data: GenreDbStorage and MpaDbStorage that use straightforward SELECT * FROM genres or SELECT * FROM mpa_ratings queries to fetch all, and parameterized queries to fetch by ID. 
These should throw NotFoundException if an ID isn’t found, to allow service/controller to translate that to a 404.
After implementing, ensure these new repositories are wired in by Spring (e.g., via component scanning) and that your service layer is using them instead of the old in-memory collections.
REASONING: Switching to persistent storage requires the application to handle a lot of new concerns – these repository classes isolate all that logic. Each repository deals with converting between the relational schema and the object model. 
By doing this in one place, the rest of the app (services/controllers) doesn’t need to know about SQL details. 
Marking them as @Primary ensures that, for example, wherever FilmStorage is needed, the JDBC one is injected, seamlessly replacing the in-memory one without changing usage. 
The repository methods also enforce consistency (via exceptions and complete data retrieval). For example, always throwing NotFoundException for missing data means the service layer can uniformly handle "not found" cases. 
These implementations also let you optimize – such as using a single query to fetch popular films with like counts – and still present a clean interface upward. 
In summary, the JDBC repositories are a foundational part of moving to real persistence, and they must be thorough and reliable in mirroring the in-memory behavior using SQL.
EXCEPTION: None – once a decision is made to persist data, all create/update/delete operations should go through these repositories. 
In-memory implementations can remain only for testing or prototyping, but the main app logic should be using the persistent ones.

#Update Service Layer for Persistence
REJECT: Leaving service logic unchanged (or partially updated) after migrating from in-memory to database storage, leading to mismatch in behavior or missing validations (which could cause database errors).
REQUIRE: Refine the service layer to accommodate the database-backed repositories and any new constraints:
- Remove any now-obsolete mechanisms (like in-memory maps or manual ID counters) from services – those are now handled by the database and repository. 
For example, FilmService should no longer generate an ID; it should call filmStorage.create(film) and trust it to return a film with an ID.
- Add validations that become important with a database. For instance, before saving a film, ensure the film has a valid Mpa rating set (not null and an ID that likely exists in mpa_ratings table). 
If not, throw a ValidationException or similar. This prevents the repository from trying to insert an invalid foreign key and failing; it’s better to catch it in the service with a clear message.
- Continue to enforce business rules in the service: e.g., check that a film’s release date isn’t too early (before 1895), or that a user’s email is not empty and follows proper format. 
These checks remain similar as before, but now are extra important to prevent bad data from reaching the DB (which might have constraints that would reject it).
- Logging: maintain or improve logging around these operations. For example, after a successful creation via the repository, log an INFO message like “Created Film [id=123, name=FooMovie]”. 
On update, log the film’s id and any key changes. On deletion, log which entity was deleted. This is essential for debugging and auditing behavior in production.
- Ensure that exception handling aligns with persistence: the repository might throw NotFoundException if something is missing, so service methods should let that propagate 
(or wrap it if needed) so the global exception handler can produce a 404. Similarly, if the repository or DB layer might throw an exception for a duplicate key or constraint violation, 
the service might catch that and throw a more domain-specific exception (like ValidationException for a duplicate email, for instance).
In summary, audit each service method (create/update/delete for each entity, plus special operations like adding friends or likes) to ensure it is doing: input validation, calling the repository, 
handling the repository’s output or exceptions, and logging appropriately.
REASONING: The service layer is the business logic coordinator – when the underlying storage changes, the services must adapt to ensure the business rules still hold and that the system behaves correctly. 
For example, in-memory you might not have cared about a missing foreign key because everything was in one structure, but with a DB, a missing foreign key (like a genre ID that doesn’t exist) will cause an error. 
It’s better for the service to catch such issues and respond gracefully rather than letting a SQL exception bubble up. Also, the performance characteristics differ: 
what was a simple collection lookup might now be a database call, so perhaps services might choose to batch some operations or reorder checks (though usually the repository is optimized to handle that). 
Proper logging at the service layer becomes even more important, as data is now persistent – you'll want a record of key actions for monitoring. 
In short, services must be reviewed and adjusted to work seamlessly with the new persistence layer and ensure no logic was accidentally left behind or duplicated.
EXCEPTION: None. All service methods should be reviewed when switching to a database. It’s not acceptable to assume everything will just work the same – some will, 
but many will need tweaks. The end goal is that the higher layers (controllers, etc.) cannot tell that the storage changed except for perhaps performance improvements or stricter error handling.

