# Replacer

**Replacer** is an online tool whose purpose is to provide a straightforward interface in order to help fixing the most common ortography or style errors in Wikipedia.

The tool has been created on the need of reviewing manually certain corrections which are difficult to automatize due to the existence of false positivies.

It's available at https://replacer.toolforge.org

The tool is optimized to be used with mobiles, tablets and bigger screens. It's possible that it doesn't work completely in old browsers, as IE.

This README is based on the tool page in Spanish Wikipedia: https://es.wikipedia.org/wiki/Usuario:Benjavalero/Replacer

Technical details can be found in https://github.com/benjavalero/replacer/blob/master/technical-design.md

## Users

The first time is needed to log in with an existing user which is registered in Wikipedia.

All editions in Replacer are performed in the name of this user and therefore count as his/her contributions.

The description of the editions, available in the history of the pages, includes a reference to the use of the tool.

It's recommended not to perform a high amount of editions in little time in order to make easier the task for the Wikipedia users in charge of following the most recent changes. If you own a _bot_ user, please use it instead of the standard one.

## Languages

Once logged in, in the upper menu (at right) the user name is displayed along with the language associated to the Wikipedia to work with, by default the Wikipedia in Spanish (_es_).

Currently there exists also the option of working with the Wikipedia in Galizian (_gl_).

Don't mistake this language with the one used in the common texts of the application: buttons, menus, etc. In this moment the application is not internationalized yet and the interface, whatever the chosen Wikipedia to work with, will be displayed in Spanish.


## Edit Pages

The tool contains several sections which help to find pages with replacements:

- Random Page. It opens the editor with a random page and all its replacements.
- Replacement List. It shows a list with all the replacement types and the approximated amount of pages having replacements of these types. Each replacement type contains a link to open the editor with a random page and the occurrences of the chosen replacement.
- Custom Replacement. It allows to introduce a replacement not existing in the tool. It opens the editor with a random page and the occurrences of the chosen replacement. The purpose of this section is to perform replacements with few occurrences. For common replacements it's recommended to included in the lists managed by the tool.

Once Replacer finds a page to be reviewed, in the top is displayed the title of the page.

By the title is displayed a link which allows to edit the page in Wikipedia by the traditional way. If all the replacements to be reviewed are contained in the same section, then the link will open the edition of that specific section.

Below, the potential replacements are displayed independently. Each replacement shows first a snippet of the text surrounding it, highlighting the replacement in red. Beneath the text, the different replacement options are listed, including the option for the original text which is selected by default.

After reviewing all the replacements, there are two buttons:

- The button **Save changes** will be active if there is any replacement to be performed. In this case there is also a badge with the amount of replacements. After clicking this button, the selected replacements will be applied immediately in Wikipedia.
- The button **Save without changes** will always be active, even if there are replacements selected. On clicking this button, no modification will be performed in Wikipedia, and Replacer will mark the page internally as reviewed.

After clicking any of the two buttons, Repalcer automatically will find the next page to be reviewed.

A page edited or reviewed without changes will not be offered again to be reviewed until the page has new relevant editions.

If none of the offered options is the right one, it's recommended to edit the page manually by using the link by the title, and save the page without changes.

Notice that ortography or style errores are usually signs of vandalism or a bad translation of the page or section. In this case, again it's recommended to edit the page manually and apply the action more convenient: revert the vandalism, rewrite the page or section, or add a warning template, like `Copyedit`.

## Replacement Types

The replacement types are grouped in following categories:

- **Ortography**. Potentially misspelled words.
  - They are extracted from the general list in https://es.wikipedia.org/wiki/Wikipedia:Corrector_ortogr%C3%A1fico/Listado. The list is refreshed every hour.
  - Each subtype matches with an item of the list
  - Terms with non-alphabetic characteres (dots, spaces or digits) are not taken into account. Terms included in the false positive list (detailed further) are ignored too.

- **Composed**. Expressions, with more than one word, potentially mispelled, or terms not fitting in the Ortography category.
  - Most subtypes are extracted from the list in https://es.wikipedia.org/wiki/Usuario:Benjavalero/ComposedMisspellings. The list is refreshed every hour.
  - Subtypes **ó between numbers** and **ó between words** for unnecessary uses of conjunction "ó" containing a diacritic (only in Spanish).

- **Dates**. Dates with wrong format. Only in Spanish.
  - **Month in uppercase**. Dates with the month in uppercase: `2 de Septiembre de 2019`
  - **Day with zero**. Dates with the day starting with zero: `02 de septiembre de 2019`
  - **Year with dot**. Dates with the year containing a dot: `2 de septiembre de 2.019`
  - **Incomplete Date**. Dates with missing prepositions: `2 de septiembre 2019`
  - It's recommended the cultivated use of «septiembre» instead of «setiembre». Nevertheless this replacement is only offered along with another fix in the same fix. For instance the date «2 de setiembre de 2019» is not offered to be reviewed.

## False positives

There are certain parts of the content of page where most false positives usually appear. With the risk of letting some replacement without fixing, the tool ignores all the replacements contained in these parts:

- Some complete XML tags, e.g. for source code: `<math>LaTeX</math>`
- Some complete templates, e.g. for quotes: `{{Cita|Texto}}`
- Template names: `{{Album|...}}`
- Template parameters: `{{...| pais = España | ...}}`
- Values of some template parameters: `{{... | índice = españa | ...}}`
- Texts in cursive and bold: `''online''`
- Quoted texts: `«In Paris»`, `"In Paris"`, `“In Paris”`
- URLs: `http://www.monografias.com`
- XML tags: `<span style="color:salmon;">`
- HTML comments: `<!-- españa -->`
- Categories: `[[Categoría:Jennifer Lopez]]`
- Suffixed links: `[[brasil]]eño`
- Aliased links (the link is ignored, not the alias): `[[Taiwan|Taiwán]]`
- Inter-wiki links: `[[pt:Title]]`
- Filenames: `[[File:españa.png|España]]`
- False positives extracted from the list in: https://es.wikipedia.org/wiki/Usuario:Benjavalero/FalsePositives. The list is refreshed every hour.
- Some proper nouns which can also be common nouns. If they are preceded or followed by a word with uppercase then they are ignored. For instance, the tool ignores "Julio" in "Julio Verne", or "Domingo" in "Plácido Domingo".
- Words in uppercase wich are correct according to the punctuation rules, in particular after the following punctuation symbols: `! # * | = .`

## Cosmetic Changes

When the tool applies changes in a Wikipedia page, it takes profit to perform some cosmetic changes which usually have no effect in the page visualisation but in the internal maintenance of the wikitext:

- Simplification of links with the same link and alias, e.g. `[[Coronavirus|coronavirus]] ⇒ [[coronavirus]]`
- Fix file spaces in lowercase, e.g. `[[archivo:x.jpg]] ⇒ [[Archivo:x.jpg]]`

If approppriate, this changes are communicated to the wikiproject _Check Wikipedia_ to update its counters.

## Indexation

Replacer indexes weekly all pages in Wikipedia trying to find new replacements, as the content of the pages may change and so the lists containing the potential errors. The indexation uses the monthly dumps in https://dumps.wikimedia.org/backup-index.html. However, not all pages are taken into account:

- Only namespaces _Article_ and _Annex_ are indexed. For instance user or discussion pages are ignored.
- Redirection pages are ignored.
- The tool alse ignores pages containing certain templates, for instance those tagged to be deleted or containing many issues.

