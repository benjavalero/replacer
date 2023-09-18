# Technical Design

## Domain

A `WikipediaPage` is a page retrieved from Wikipedia, from any language or namespace. It contains the most important properties, in particular the text content. It is actually a snapshot, as the page content can still be modified later by any Wikipedia user. It is composed at least by the following properties:
  - `WikipediaLanguage`: the language of the Wikipedia where the page exists.
  - `PageKey`: to identify it uniquely, composed by the language and a numeric ID.
  - **Title** of the page. This one could also be used to identify a page, but it is easier to use the numeric ID as the page title might change.
  - `WikipediaNamespace`: the category of the page in Wikipedia: article, annex, user page, etc. Note that, although an article is a specific type of page, it is usually used as a synonym of page, but in this application we try not to do it.
  - **Content**: the current text of the page, also known as _wiki-text_.
  - **Last update**: the date and time of the last update of the page.
  - **Query timestamp**: the date and time when the page was queried and retrieved.
  - **Redirect**: if the page is considered a redirection page.

A Wikipedia page is usually divided in a collection of `WikipediaSection`. Each section on a page can be identified by its _index_. The name of the section is called the _anchor_. Note that Wikipedia allows retrieving and editing sections instead of the whole page, so in these cases the section will be added as a property of the page.

A `DumpFile` is huge XML file, generated monthly, containing all the current Wikipedia pages for a language. A `DumpPage` is similar to a Wikipedia page, but retrieved from a dump file, and therefore quite likely to be outdated depending on the dump date.

A `WikipediaUser` is a user registered in Wikipedia. Note that not all Wikipedia users are allowed to be a Replacer user. The username should be enough to identify a user in Wikipedia. However, as users may have different permissions depending on the Wikipedia language, we use a `UserId` to identify a user uniquely, composed by the language and the username. A Wikipedia user may belong to several `WikipediaUserGroup` which provide different permissions: `user`, `autoconfirmed`, `bot`, etc.

A `User` is a user of Replacer. It must be a Wikipedia user with the necessary permissions in Wikipedia in order to use the tool. In particular, after being authenticated in Wikipedia, a user has an `AccessToken` to perform the operations in Wikipedia in a non-anonymous way.

A `Replacement` is a potential issue to be checked and fixed (replaced). For instance, the word _aproximated_ is misspelled and therefore could be proposed to be replaced with _approximated_.
Note the importance of the _potential_ adjective, as an issue could be just a false positive. For instance, in Spanish the word _Paris_ could be misspelled if it corresponds to the French city (written correctly as _París_), but it would be correct if it refers to the mythological Trojan prince.

Each replacement belongs to a `ReplacementType`, which can be a `StandardType` if it is known or a `CustomType` if it has been customized by the user. Replacement types are categorized by `ReplacementKind`. Finally, a replacement type is composed by its kind and its subtype.

An `Immutable` is a section in the page contents to be left untouched, for instance a literal quote, so any replacement found within it must be ignored and not offered to the user to be reviewed.

A `Cosmetic` is a special type of replacement which can be applied automatically, concerning cosmetic modifications, visible or not, e.g. replacing `[[Asia|Asia]]` by `[[Asia]]`.

A replacement is identified by its type and by its position (**start**) in the page content, as the tool doesn't allow that overlapping of replacements. Instead of an _end_ position, a replacement contains the text intended to be replaced, along with a collection of suggestions. Each `Suggestion` contains the proposed new text after applying the replacement and optionally a description to explain the motivation of the fix.

A `Review` (or simply a **Page**) is a Wikipedia page (or section) containing one or more replacements to be reviewed. Then, a `ReviewedPage` is a page containing a collection of replacements reviewed by the user. Note that some or all these `ReviewedReplacement` might not be applied if the user has decided it that way, especially in case of doubt or false positive.

## Use cases

Main use cases are:

1. As a user, I want to request a random Wikipedia page containing potential issues to fix, in order to review them, discard the false positives and save the approved fixed into Wikipedia.
2. As a system, I want to find all the existing issues in Wikipedia pages, in order to find quickly a page for the previous use case.

For the first use case, the basic steps are:

1. Find in the database a page containing at least a replacement
2. Find in Wikipedia the last version of the page contents as the information in the database could be outdated
3. Parse the page contents and find all the replacements in the page
4. Display to the user the current page contents and all the found replacements
5. The user discards some replacements and accepts the suggestions for others
6. The replacements accepted by the user are applied to the page contents and uploaded to Wikipedia

For the second use case:

1. Find latest dump
2. Parse the dump and extract the pages. For each page:
    1. Parse the page to find the replacements
    2. Save the page replacements in the database
3. Save a summary of the process in the database

On an indexation, all dump pages are read but not all of them are taken into account or processed:

- Pages are processable (and therefore _read_) if their namespace is one of the supported ones: articles and annexes.
- Processable pages are to be processed if after re-indexation the related replacements in database must be updated. Pages are not processed if dump page timestamp is before the last indexation in database.


## Code Conventions

The tool is composed by two independent modules, both in the same repository: the frontend (Angular), and the backend (Java with SpringBoot). Most of the tips below refer to the Java backend.

### Code Organization

The code is organized in different Maven submodules, whose dependencies follow the principles of Clean Architecture.

Note it is not worth to implement the Java Module System, as it implies some modifications in order to work with JUnit and Spring: new version of Surefire, package names cannot be repeated between modules and integration tests in a different package.

In general, services are created for each use case. In general, it is not worth to create an interface for each service as there is usually only one implementation.

_Ports_ (interfaces for the Wikipedia, OAuth and database adapters) are included in the same package of the adapter but in the `domain` submodule.

Classes are usually suffixed: `Service`, `Controller`, `Repository`, etc.

DTO objects are used to communicate the different layers. The suffix `Dto` (also `Request` and `Response`) is usually used for the objects communicating the controllers with the view.

In backend, we have the following submodules:

- `replacer-app`. SpringBoot configuration and application.
- `replacer-core`. Use cases.
- `replacer-domain`. Domain objects and interfaces shared between modules.
- `replacer-finder`. Adapter with the functionality of finding replacements in pages. It has been implemented as an adapter instead of in the core to improve the decoupling.
- `replacer-repository`. Adapter with a JDBC implementation of the persistence.
- `replacer-web`. Adapter with the REST services.
- `replacer-wikipedia`. Adapters to access external resources: Wikipedia (pages, users and authentication), and Check-Wikipedia.

And the following packages:
- Configuration classes and property files are preferred to be in the base package.
- `checkwikipedia`. To send notifications to Check-Wikipedia when some types of cosmetics are applied, and they are also supported in Check-Wikipedia, so we can help this project to keep their figures up-to-date.
- `common`. Domain entities shared between packages, along with exceptions and utility classes used on all the tool.
- `common.domain`. Domain entities.
- `dump`. Helper classes to parse a dump, extract the dump pages, find the replacements in these pages, and finally add the found replacements into the database. `DumpFinder` finds the latest dump available for a given project. `DumpManager` checks periodically the latest available dump, and indexes it.
- `finder`. A `ReplacementFinder` is a helper class to find all the replacements in a page of a specific replacement type. `ReplacementFinderService` is a helper class to find all the replacements in a page. Note that these are particular implementations of `Finder` and `FinderService`. Other implementations find all the immutables and cosmetics in a page. Note that not all the properties of a page are needed in order to find its replacements, so these finders receive a `FinderPage` with only the needed properties, being `FinderPage` an interface implemented in particular by `WikipediaPage` and `DumpPage`.
  - `benchmark`. Performance tests for different approaches in order to find the best performant finders. Note that these finders will be performed millions of times when indexing a whole dump, so the performance is critical.
  - `cosmetic`. Cosmetic finders: links with same title and alias, file templates in lowercase, etc. These finders are used after a user reviews a page, therefore the performance is not so important as when finding replacements and immutables.
  - `immutable`. Immutable finders: URLs, template parameters, HTML comments, etc.
  - `listing`. Some finders use a list of properties which are maintained in text files (or Wikipedia pages) which need to be parsed first. These finders retrieve the properties from a manager class extending `ParseFileManager`. All of these also implement the Observable pattern. The managers reload the properties periodically, and the observer finders are notified in case of changes. The first load is done at the start of the application, and it takes few seconds, so there is a little chance that, when using the application just started, some of them are not loaded yet. Just in case we check this possibility and return no results if so.
  - `replacement`. Replacement finders: misspellings, composed misspellings, dates, etc. Note that some of these finders may only work for Spanish language.
  - `util`. Several finders are based on regular expressions. We use `RegexMatchFinder` to iterate the match results of a regex. This tool uses also a _text-based_ implementation of regular expressions in `AutomatonMatchFinder`, which builds an automaton from the regex and gives performance improvements of 1 to 2 orders of magnitude for simple expressions. However, it doesn't include advanced features implying backtracking. Finally, most finders are implemented _by hand_ with `LinearMatchFinder`. This makes the implementations quite more complex but the performance improvement is worth it. *Note*: for very simple finders it is not very clear which approach is better (see _simple benchmark_) so it is recommended to implement a new finder with different approaches and test which one has better performance. In any case, sometimes it will be worth to lose some performance for the sake of maintainability.
- `page`. Services to count, index, find/review and save pages.
- `replacement`. Repositories to retrieve and count indexed replacements from database, and cache proxies for some heavy operations. Currently, the database has a huge table, represented with `ReplacementEntity`, containing all the replacements found for all Wikipedia pages, along with the review status.
- `user`. Features related to user management. In particular, aspects to restrict permissions to certain operations, and authentication with OAuth 1.0a against Wikipedia.
- `wikipedia`. Repositories to perform operations on Wikipedia pages and users, implemented with requests against the [Wikipedia API](https://www.mediawiki.org/wiki/API:Main_page). The common behaviour for these API requests has been refactored to a helper class in the inner `wikipedia.api` package.

### Formatting

Code in TypeScript and Java is formatted with Prettier.

In case of Java code, this is automatically done when compiling by a Maven plugin.

In case of TypeScript code, Prettier configuration is automatically found by VS Code, and formatting can be done easily from the IDE.

### Annotations

The backend uses some of the JetBrains (IntelliJ) annotations: `@TestOnly`, `@VisibleForTesting` and `@RegExp`, but this IDE is not mandatory to build or maintain the tool.

On the other hand, all packages are annotated with Spring annotation `@NonNullApi` which forces all method parameters and return values to be non-null. When some parameter or return value is nullable, it will be annotated explicitly.

### Immutability

All Domain Objects should be immutable, and all data structures which may be accessed by several threads should be thread-safe. To achieve object immutability, we annotate them with `@Value` and optionally with `@Builder` for simplicity in case they contain lots of fields. All of this can also be applied to Output DTOs.

On the other hand, Input DTOs are annotated with `@Data` and `@NoArgsConstructor` so they can be mapped by Jackson library.

In case a DTO is used for input and output, we will annotate it with a mix of the annotations above.

### Validation

All Domain Objects should always be valid. The simplest validation is about the nullable and non-nullable fields (most of them). Non-nullable fields will be annotated with `org.springframework.lang.NonNull` (we could use the Lombok annotation, but the Spring one is preferred for consistency). Lombok will find this annotation and generate null-checks which will throw an `IllegalArgumentException`. Nullable fields can be then annotated with `org.springframework.lang.Nullable`.

To perform more complicated validations, like data integrity, we will generate a constructor for all fields (which will be eventually called with `@Value` and `@Builder` annotations), throwing an `IllegalArgumentException` if validation fails.

Again, all of this can also be applied to Output DTOs.

_Note_: if an `IllegalArgumentException` is eventually thrown in a REST Controller it will result in an HTTP status "400 - Bad Request".

On the other hand, simple validations on Input DTOs will be annotated with the standard Java `javax.validation.NotNull`, `@Size`, etc. Request bodies (and nested DTOs) will be annotated with `@Valid`. Complex validations will be avoided in these cases, as they will be performed in the domain objects.

### Persistence

Currently, Replacer uses its own classic SQL database hosted in Wikipedia servers. As performance is critical, especially when indexing, finally a classic JDBC approach is taken instead of using an ORM like JPA.

As the database structure is quite simple, there is one repository per table, although these tables are of course linked.

## Logging

Replacer uses Logback logging, the default in Spring Boot. To initialize the loggers, we use the Lombok annotation `@Slf4j` in each class.

A custom `logback-spring.xml` exists to simplify the log pattern, and include a special appender to Logz.io for Production.

The language and the user for each call are logged as MDC.
Most objects are printed in the logs as JSON.
Immutable warnings are logged with the marker `IMMUTABLE`.

## Benchmarks

For some critical finders, we can run benchmarks to find the best approach.
Take into account that, when dump indexing, each finder might be run almost 2M times (once per indexed page), so a difference of microseconds might turn into minutes in the overall time.

Anyway take into account that the time finding the replacements might not represent all the time performing an indexation, as there are other parts like XML parsing and DB operations.

Each benchmark runs the finders against a sample of 50 pages. Note that the resulting times depend strongly on the machine where the benchmarks have been run. Empirical tests show that the average time to find the replacements in an average page in different machines is about 1.5 to 3.5 ms.

Benchmarks in Replacer are test classes extending `BaseFinderBenchmark` and having `BenchmarkTest` as a name suffix to be ignored.

For each finder (usually different approaches for the same purpose), the test method `testBenchmark` runs the finder several times (100 times by default) to warm up, and then it runs again the finder several times (1000 times by default) and prints the average time per iteration (in ns).
This is done for each page in the sample. This results into a text file with 50 lines per finder.

The class also contains a main method which reads the previous text file a generates a boxplot which allows comparing the times of the different approaches, along with a text file containing the classic five-number summary with the most important percentiles. These statistics help us not only to compare the different approaches in general, but also how the finders behave with simple or complex pages which in theory result into lower or higher times.

_Note_: after some attempts to run benchmarks with JMH library, we have found similar results. For our purposes, as the benchmarks _by hand_ already exist, JMH benchmarks don't provide any advantage, so they have eventually been discarded from the code.
