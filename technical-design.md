# Technical Design

## Nomenclature and use cases

Main use cases:

1. As a user, I want to request a random Wikipedia page containing potential issues to fix, in order to review them, discard the false positives and save the approved fixed into Wikipedia.
2. As a system, I want to find all the existing issues in Wikipedia pages, in order to find quickly a page for the previous use case.

The following concepts are used:

- **Page**. A page in Wikipedia. It is composed at least by the following properties:
  - **Namespace**. The category of the page in Wikipedia: article, annex, user page, etc. Note that, although an article is a specific type of page, it is usually used as a synonym of page, but in this application we try not to do it.
  - **Language**. The language of the Wikipedia where the page exists.
  - **Title**. The title of the page which identifies it uniquely.
  - **ID**. The identifier of the page, a number for internal use that can also be used to identify it uniquely in a specific Wikipedia.
  - **Contents**. The current text contents of the page, also known as _wiki-text_.
  - **Timestamp**. The date and time of the last update of the page.
- **Dump**. A huge XML file, generated monthly, containing all the current Wikipedia pages for a language.
- **Replacement**. A potential issue to be checked and fixed (replaced). For instance, the word _aproximated_ is misspelled and therefore could be proposed to be replaced with _approximated_.

  Note the importance of the _potential_ adjective, as an issue could be just a false positive. For instance, in Spanish the word _Paris_ could be misspelled if it corresponds to the French city (written correctly as _París_), but it would be correct if it refers to the mythological Trojan prince.

  A replacement is composed by:

  - **Text**. The text to be checked and fixed. It can be a word or an expression.
  - **Start**. A number corresponding to the position in the page contents where the text is found. Take into account that the first position is 0.
  - **Type**. The category of the replacement: misspelling, date format, etc. It may include several **subtypes**.
  - **Suggestions**. A list with at least one suggestion to replace the text. Each suggestion is composed by:
    - **Text**. The new text after the replacement.
    - **Comment**. An optional description to explain the motivation of the fix.
- **Immutable**. A section in the page contents to be left untouched, for instance a literal quote, so any replacement found within it must be ignored and not offered to the user for revision. It is composed by:
  - **Start**. The start position of the section in the page contents
  - **End**. The end position of the section in the page contents
  - **Text**. Optionally, the text in the section, especially for debugging purposes, i.e. the text between the start and end position of the section.
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

The tool is composed by two independent modules, both in the same repository: the frontend ( Angular), and the backend (Java with SpringBoot). Most of the tips below refer to the Java backend.

### Code Organization

Files are usually suffixed: service, controller, entity, etc.

For the moment the size of the project is not so big that it is worth to divide the backend in several submodules.
Besides, the backend is quite coupled to Spring Boot framework, making more difficult to use Java 9 modules.
Instead, packages are meant to be as independent as possible, organizing the code by feature and following the principles of Clean Architecture.

In backend, we have the following packages:

- `config`. Spring configuration classes with beans used in several packages.

- `dump`. Helper classes to parse a dump, extract the pages, the replacements in these page, and finally add the fount replacements into the database. `DumpFinder` finds the latest dump available for a given project. `DumpManager` checks periodically the latest available dump, and indexes it, by running the job implementing `DumpJob`. Note that in the past there were two implementations: one in Spring Batch and another with SAX. Currently there is only the last one which give better performance results. When indexing we have the typical steps:
  - Read: the dump is read and parsed in `DumpHandler`, extracting the pages into `DumpPage`.
  - Process: `DumpPageProcessor` transforms each dump page into a list of replacements to be saved in the database. `ReplacementCache` helps to retrieve the database replacements in chunks in order to compare them to the ones obtained in the processing.
  - Write: `DumpWriter` inserts or updates in the database the resulting replacements from the processor.

- `finder`. The core functionality of the tool is to find potential replacements in a text. This package offers several helper classes to find these and other similar useful matches in a text. Note that these match searches will be performed millions of times when indexing a whole dump, therefore the performance is critical.

  - `common`. Several interfaces. `Finder` will be implemented to find specific matches: URLs, templates, misspellings, etc. `FinderService` will be implemente to find all matches of a common type: replacements, custom replacements, cosmetics and immutables.

    Some of the finders use a list of properties which are maintained in text files (or Wikipedia pages) which need to be parsed first. These finders retrieve the properties from a manager class extending `ParseFileManager`. All of these also implement the Observable pattern. The managers reload the properties periodically, and the observer finders are notified in case of changes. The first load is done at the start of the application and it takes few seconds, so there is a little chance that, when using the application just started, some of them is not loaded yet. Just in case we check this possibility and return no results if so.

  - `util`. Several finders are based on regular expressions. We use `RegexMatchFinder` to iterate the match results of a regex. This tool uses also a _text-based_ implementation of regular expressions in `AutomatonMatchFinder`, which builds an automaton from the regex and gives performance improvements of 1 to 2 orders of magnitude for simple expressions. However, it doesn't include advanced features implying backtracking. Finally, most finders are implemented _by hand_ with `LinearMatchFinder`. This makes the implementations quite more complex but the performance improvement is worth it.

  - `replacement`. Replacement finders: misspellings, composed misspellings, dates, etc. Note that some of these finders may only work for Spanish language.

  - `immutable`. Immutable finders: URLs, template parameters, HTML comments, etc.

  - `cosmetic`. Cosmetic finders: links with same title and alias, file templates in lowercase, etc. These finders are used after a user reviews a page, therefore the performance is not so important as when finding replacements and immutables.

  - `benchmark`. Performance tests for different approaches in order to find the best performant finders.

- `page`. Helper classes to review a page, i.e. find all the related replacements to be processed by the frontend.

- `replacement`. Helper classes to retrieve replacements from database, and cache proxies for some heavy operations. Currently the database has a **huge** table, represented with `ReplacementEntity`, containing all the replacements found for all Wikipedia pages, along with the review status. For performance reasons we use a direct JDBC approach in `ReplacementDao`.

  When a new page is indexed, or there have been any modifications, the replacements in the database must be updated. `ReplacementIndexService` offers a method, receiving the list of the new page replacements, which adds the new replacements and marks as obsolete the ones not found in the new list.

- `wikipedia`. Helper class `WikipediaService` to authenticate in Wikipedia to perform requests against the [Wikipedia API](https://www.mediawiki.org/wiki/API:Main_page).

  The authentication is performed by the Oauth 1.0a protocol, the authentication is implemented in the backend, and the frontend only contains the last access token. Note there are different OAuth tokens to develop locally (see `replacer.wikipedia.api.*` properties). The Production ones, once logged in, redirect to https://replacer.toolforge.org, while the Development ones redirect to http://localhost:8080.


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
