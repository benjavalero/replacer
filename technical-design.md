# Technical Design

## Domain

- A **Wikipedia page** is a page retrieved from Wikipedia, from any language or namespace. It contains the most important properties, in particular the text content. It is actually a snapshot, as the page content can still be modified later by any Wikipedia user.
- A **dump page** is similar to a Wikipedia page, but retrieved from a Wikipedia dump, and therefore quite likely to be outdated depending on the dump date. A **dump** is huge XML file, generated monthly, containing all the current Wikipedia pages for a language.
- A **Wikipedia user** is a user registered in Wikipedia. Note that not all Wikipedia users are allowed to be a Replacer user.
- A **user** is a Wikipedia user who also has the necessary permissions in Wikipedia in order to use the tool.


## Nomenclature and use cases

Main use cases:

1. As a user, I want to request a random Wikipedia page containing potential issues to fix, in order to review them, discard the false positives and save the approved fixed into Wikipedia.
2. As a system, I want to find all the existing issues in Wikipedia pages, in order to find quickly a page for the previous use case.

The following concepts are used:

- **Page**. A page in Wikipedia. It is composed at least by the following properties:
  - **Namespace**. The category of the page in Wikipedia: article, annex, user page, etc. Note that, although an article is a specific type of page, it is usually used as a synonym of page, but in this application we try not to do it.
  - **Language**. The language of the Wikipedia where the page exists.
  - **Title**. The title of the page which identifies it uniquely.
  - **ID**. The identifier of the page, a number for internal use that can also be used to identify it uniquely in a specific Wikipedia. Note that, used along with the language, it identifies a page completely in the tool.
  - **Content**. The current text of the page, also known as _wiki-text_.
  - **Last update**. The date and time of the last update of the page.
  - **Query timestamp**. The date and time when the page was queried and retrieved.
  - **Redirect**. If the page is considered a redirection page.
  - **Sections**. A Wikipedia page is usually divided in sections in different levels identified by its anchor (header title). Note that Wikipedia allows editing sections instead of the whole page.
- **Replacement**. A potential issue to be checked and fixed (replaced). For instance, the word _aproximated_ is misspelled and therefore could be proposed to be replaced with _approximated_.

  Note the importance of the _potential_ adjective, as an issue could be just a false positive. For instance, in Spanish the word _Paris_ could be misspelled if it corresponds to the French city (written correctly as _París_), but it would be correct if it refers to the mythological Trojan prince.

  A replacement is composed by:

  - **Text**. The text to be checked and fixed. It can be a word or an expression.
  - **Start**. A number corresponding to the position in the page contents where the text is found. Take into account that the first position is 0.
  - **Type**. The category of the replacement. It is composed by a parent **kind** (misspelling, date format, etc.) and a child **subtype**.
  - **Suggestions**. A list with at least one suggestion to replace the text. Each suggestion is composed by:
    - **Text**. The new text after the replacement.
    - **Comment**. An optional description to explain the motivation of the fix.
- **Immutable**. A section in the page contents to be left untouched, for instance a literal quote, so any replacement found within it must be ignored and not offered to the user for revision.
- **Cosmetic**. A special type of replacement which can be applied automatically, concerning cosmetic modifications, visible or not, e.g. replacing `[[Asia|Asia]]` by `[[Asia]]`.
- **PageReview**. A summary of a page (or page section) with replacements to be reviewed.

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
- Processable pages are to be processed if after reindexation the related replacements in database must be updated. Pages are not processed if dump page timestamp is before the last indexation in database.


## Code Conventions

The tool is composed by two independent modules, both in the same repository: the frontend (Angular), and the backend (Java with SpringBoot). Most of the tips below refer to the Java backend.

### Code Organization

The code is organized in different Maven submodules, whose dependencies follow the principles of Clean Architecture.

Note it is not worth to implement the Java Module System, as it implies some modifications in order to work with JUnit and Spring: new version of Surefire, package names cannot be repeated between modules and integration tests in a different package.

In general, services are created for each use case.

The _ports_ (interfaces for the Wikipedia, OAuth and database adapters) are included in the same package of the adapter but in the `domain` submodule.

Classes are usually suffixed: `Service`, `Controller`, `Repository`, etc.

DTO objects are used to communicate the different layers. The suffixes `Request` and `Response` are preferred to `Dto` for the objects used to communicate the controllers with the view.

In backend, we have the following modules:

- `replacer-wikipedia`. Implementations (adapters) to access external resources: Wikipedia (pages, users and authentication), and Check-Wikipedia, to send notifications when some types of cosmetics are applied.

And the following packages:

- `common.domain`. Domain entities.

- `config`. Spring configuration classes, some of them with beans used in several packages.

- `dump`. Helper classes to parse a dump, extract the pages, the replacements in these page, and finally add the fount replacements into the database. `DumpFinder` finds the latest dump available for a given project. `DumpManager` checks periodically the latest available dump, and indexes it, by running the job implementing `DumpJob`. Note that in the past there were two implementations: one in Spring Batch and another with SAX. Currently, there is only the last one which give better performance results. When indexing we have the typical steps:
  - Read: the dump is read and parsed in `DumpHandler`, extracting the pages into `DumpPage`.
  - Process: `DumpPageProcessor` transforms each dump page into a list of replacements to be saved in the database. `ReplacementCache` helps to retrieve the database replacements in chunks in order to compare them to the ones obtained in the processing.
  - Write: `DumpWriter` inserts or updates in the database the resulting replacements from the processor.

- `finder`. The core functionality of the tool is to find potential replacements in a text. This package offers several helper classes to find these and other similar useful matches in a text. Note that these match searches will be performed millions of times when indexing a whole dump, therefore the performance is critical.

  - `common`. Several interfaces. `Finder` will be implemented to find specific matches: URLs, templates, misspellings, etc. `FinderService` will be implemente to find all matches of a common type: replacements, custom replacements, cosmetics and immutables.

    Some finders use a list of properties which are maintained in text files (or Wikipedia pages) which need to be parsed first. These finders retrieve the properties from a manager class extending `ParseFileManager`. All of these also implement the Observable pattern. The managers reload the properties periodically, and the observer finders are notified in case of changes. The first load is done at the start of the application, and it takes few seconds, so there is a little chance that, when using the application just started, some of them is not loaded yet. Just in case we check this possibility and return no results if so.

  - `util`. Several finders are based on regular expressions. We use `RegexMatchFinder` to iterate the match results of a regex. This tool uses also a _text-based_ implementation of regular expressions in `AutomatonMatchFinder`, which builds an automaton from the regex and gives performance improvements of 1 to 2 orders of magnitude for simple expressions. However, it doesn't include advanced features implying backtracking. Finally, most finders are implemented _by hand_ with `LinearMatchFinder`. This makes the implementations quite more complex but the performance improvement is worth it. *Note*: for very simple finders it is not very clear which approach is better (see _simple benchmark_) so it is recommended to implement a new finder with different approaches and test which one has better performance. In any case, sometimes it will be worth to lose some performance for the sake of maintainability.

  - `replacement`. Replacement finders: misspellings, composed misspellings, dates, etc. Note that some of these finders may only work for Spanish language.

  - `immutable`. Immutable finders: URLs, template parameters, HTML comments, etc.

  - `cosmetic`. Cosmetic finders: links with same title and alias, file templates in lowercase, etc. These finders are used after a user reviews a page, therefore the performance is not so important as when finding replacements and immutables.

  - `benchmark`. Performance tests for different approaches in order to find the best performant finders.

- `page`. Helper classes to review a page, i.e. find all the related replacements to be processed by the frontend.

- `replacement`. Helper classes to retrieve replacements from database, and cache proxies for some heavy operations. Currently the database has a **huge** table, represented with `ReplacementEntity`, containing all the replacements found for all Wikipedia pages, along with the review status. For performance reasons we use a direct JDBC approach in `ReplacementDao`.

  When a new page is indexed, or there have been any modifications, the replacements in the database must be updated. `ReplacementIndexService` offers a method, receiving the list of the new page replacements, which adds the new replacements and marks as obsolete the ones not found in the new list.

- `user`. Features related to user management. In particular, aspects to restrict permissions to certain operations, and authentication with OAuth 1.0a against Wikipedia.
- `wikipedia`. Repository to perform operations on Wikipedia pages and users. Currently, these operations are implemented with requests against the [Wikipedia API](https://www.mediawiki.org/wiki/API:Main_page). The common behaviour for these API requests has been refactored to a helper class in the inner `wikipedia.api` package.
- `check-wikipedia`. Service to notify to Check-Wikipedia about fixes performed by Replacer, which are also supported in Check-Wikipedia, so we can help this project to keep their figures up-to-date.


### Formatting

Code in TypeScript and Java is formatted with Prettier.

In case of Java code, this is automatically done when compiling by a Maven plugin.

In case of TypeScript code, Prettier configuration is automatically found by VS Code, and formatting can be done easilly from the IDE.

### Annotations

The backend uses some of the JetBrains (IntelliJ) annotations: `@TestOnly`, `@VisibleForTesting` and `@RegExp`, but this IDE is not mandatory to build or maintain the tool.

On the other hand, all packages are annotated with Spring annotation `@NonNullApi` which forces all method parameters and return values to be non-null. When some parameter or return value is nullable, it will be annotated explicitly.

### Immutability

All Domain Objects should be immutable, and all data structures which may be accessed by several threads should be thread-safe. To achieve object immutability, we annotate them with `@Value` and optionally with `@Builder` for simplicity in case they contain lots of fields. All of this can also be applied to Output DTOs.

On the other hand, Input DTOs are annotated with `@Data` and `@NoArgsConstructor` so they can be mapped by Jackson library.

In case a DTO is used for input and output, we will annotate it with a mix of the annotations above.

### Validation

All Domain Objects should always be valid. The simplest validation is about the nullable and non-nullable fields (most of them). Non-nullable fields will be annotated with `org.springframework.lang.NonNull` (we could use the Lombok one but we prefer this one for consistency). Lombok will find this annotation and generate null-checks which will throw an `IllegalArgumentException`. Nullable fields can be then annotated with `org.springframework.lang.Nullable`.

To perform more complicated validations, like data integrity, we will generate a constructor for all fields (which will be eventually called with `@Value` and `@Builder` annotations) which will throw an `IllegalArgumentException` if validation fails.

Again, all of this can also be applied to Output DTOs.

_Note_: if an `IllegalArgumentException` is eventually thrown in a REST Controller it will result in an HTTP status "400 - Bad Request".

On the other hand, simple validations on Input DTOs will be annotated with the standard Java `javax.validation.NotNull`, `@Size`, etc. Request bodies (and nested DTOs) will be annotated with `@Valid`. Complex validations will be avoided in these cases, as they will be performed in the domain objects.

### Persistence

Currently, Replacer uses its own classic SQL database hosted in Wikipedia servers. As performance is critical, especially when indexing, finally a classic JDBC approach is taken instead of using an ORM like JPA.

As the database structure is quite simple, there is one repository per table, although these tables are of course linked.

## Logging

Replacer uses Logback logging, the default in Spring Boot. To initialize the loggers, we use the Lombok annotation `@Slf4j` in each class.

A custom `logback-spring.xml` exists to simplify the log pattern, and include a special appender to Logz.io for Production.

The default logging level is DEBUG, using INFO for calls in controllers and WARNING for suspicious replacements or immutables.

Finally, we use the annotation `@Loggable` provided by dependency `com.github.rozidan.logger-spring-boot`. It _wraps_ the annotated methods by aspects logging the start and the end of the method, displaying the elapsed time, warning about too long time, parameters, etc. Note that Spring provides a limited AspectJ solution: as it works proxying the classes, Spring adds the functionality in runtime and therefore it can only be applied in public methods and in calls from different classes.

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
