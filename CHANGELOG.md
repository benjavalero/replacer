# Versions

## 2.11.0 (??)
- New feature: edit text around replacement

## 2.10.5 (17 Apr 2021)
- New table for page titles

## 2.10.4 (16 Apr 2021)
- Fix: Load replacement list
- Update titles during indexation

## 2.10.3 (14 Apr 2021)
- Fix: Only sort replacement table after sort event
- Fix: Refresh and don't recreate table components in replacement list
- Specific table for custom replacements. This should improve custom replacement performance.

## 2.10.2 (11 Apr 2021)
- Fix in Galician dates

## 2.10.1 (11 Apr 2021)
- Fix: The language is needed to retrieve the user groups

## 2.10.0 (10 Apr 2021)
- Allow only autoconfirmed users
- Limit the number of editions per minute
- Improve edit summary to include the specific replacement applied
- Fix: custom replacement validation
- Fix: date finder in Galician

## 2.9.5 (1 Apr 2021)
- Fix: infinite loop on custom review when all provided results have no replacements

## 2.9.4 (26 Mar 2021)
- Fix: find file values in templates with trailing spaces
- More template params to ignore

## 2.9.3 (8 Mar 2021)
- Increment pagination size for Wikipedia searches to the maximum allowed in order to reduce the amount of calls
- Fix: find values in template params when surrounded by whitespaces
- Improve capture of template parameters to ignore

## 2.9.2 (28 Feb 2021)
- Fix: pagination when filtering in replacement list

## 2.9.1 (28 Feb 2021)
- Fix: load replacement list every time the list section is opened
- Fix: maintain replacement count cache in backend
- Move warnings to the bottom of the pages to make them less intrusive specially on mobile devices

## 2.9.0 (27 Feb 2021)
- Improve texts in dashboard, removing references to Spanish language and adding a warning.
- Improve suggestions for composed misspellings
- Improve technical documentation and organization of the code for easier maintenance
- Improve management of custom replacement results
- Explain better how to use correctly custom replacements

## 2.8.2 (29 Jan 2021)
- Improve finder for template immutables
- Fix encoding when redirecting to previous page after session login
- Limit custom replacement size
- Add rotate to pagination

## 2.8.1 (17 Jan 2021)
- Statistics page is again public (not admin)
- Fix option so the edited page is not added to watchlist

## 2.8.0 (11 Jan 2021)
- Upgrade backend and frontend dependencies
- Improve custom finder performance when calling Wikipedia search API
- Control exceptions on custom finder when text too long or too much results
- Call Check-Wikipedia when a cosmetic change is applied
- Fix cosmetic finder for links with the same link and title

## 2.7.14 (5 Jan 2021)
- Improve performance on template and link finders
- Improve warnings for wrong immutables
- Improve indexation performance by returning to the SAX handler

## 2.7.13 (1 Jan 2021)
- Escape characters in custom replacements

## 2.7.12 (26 Dec 2020)
- Improve performance of several immutable finders
- Support custom replacements with replacement in uppercase and suggestion in lowercase
- Simplify custom replacement form
- Redirect from custom replacement if the replacement is of a known type

## 2.7.11 (7 Dec 2020)
- Internal improvements in log traces

## 2.7.10 (30 Nov 2020)
- Improve performance of several finders reducing indexation times
- Improve queries to delete and update replacements when indexing
- Allow dots in custom replacements

## 2.7.9 (27 Nov 2020)
- Internal feature: add logging by aspects. Add warnings to some methods when taking too long. New appender to send the traces to a log cloud server. Simplify traces.
- Fix issue when finding custom replacements making the start too slow
- Internal feature: Enable warnings when found immutables are empty or too long.
- Add more ignorable templates

## 2.7.8 (9 Nov 2020)
- Improve indexation not to perform unnecessary database updates
- Allow dots, commas and numbers in composed misspellings (not in simple)
- Add more ignorable templates
- Fix another issue finding "ó" between words containing "ó"
- Improve responsive layout in dashboard for normal users

## 2.7.7 (6 Nov 2020)
- Fix issue finding "ó" between words containing "ó"

## 2.7.6 (5 Nov 2020)
- Revert link to open Wikipedia page section instead of edit
- New finder: "ó" between words (Spanish)
- Improve indexation performance by not updating unnecessary fields

## 2.7.5 (4 Nov 2020)
- Link to edit Wikipedia page section instead of link to open in read mode
- New finder: "ó" between numbers (Spanish)
- Fix uppercase composed misspellings not found when case-insensitive
- Fix issues on dump indexation affecting performance

## 2.7.4 (30 Oct 2020)
- Improve date finders to fix years with dots and complete missing prepositions
- Improve indexation memory footprint by replacing JPA repository by DAO

## 2.7.3 (19 Oct 2020)
- Revert the last date finders as they are given wrong descriptions and suggestions
- Improve indexation performance and estimation accuracy

## 2.7.2 (14 Oct 2020)
- Improve date finders to detect incomplete dates with missing prepositions

## 2.7.1 (12 Oct 2020)
- New feature: mark as reviewed all pages containing a specific replacement type.

## 2.7.0 (5 Oct 2020)
- New feature: list of pages containing a specific replacement type especially to be used by bots.

## 2.6.6 (24 Sep 2020)
- Improve capture of file names
- Improve capture of aliased links to annexes
- Improve breadcrumb to display pending articles count
- Add cosmetic finder for file spaces in lowercase

## 2.6.5 (20 Jun 2020)
- Fix issue on estimation of pending articles to review

## 2.6.4 (18 Jun 2020)
- Show the estimated amount of pending articles to review for the current replacement type
- Improve list of templates whose presence makes a page not processable
- Improve immutable finders containing tabs
- Add option to close the session

## 2.6.3 (30 May 2020)
- Add more cases to detect false positives

## 2.6.2 (18 May 2020)
- Fix issue not loading more than 100 result for custom replacements
- Add drop-down to select language

## 2.6.1. (16 May 2020)
- Adapt to new ToolForge URL: https://replacer.toolforge.org

## 2.6.0 (14 May 2020)
- Backend adaptation to use several Wikipedias. For the moment Spanish (es) and Galician (gl).
- Fix finder of file names containing dots in the name
- Internal improvements for configuration of finders
- Upgrade to SpringBoot 2.2.7 and Angular 9.1.6.

## 2.5.2 (25 April 2020)
- Fix finder of file names in template parameter values

## 2.5.1 (25 April 2020)
- Add more complete tags to ignore
- Improve template parameter finder to ignore file values

## 2.5.0 (16 April 2020)
- Save replacement context to check of only the position has changed
- Optimization of most immutable finders
- Improve immutable finders to find more false positives
- Review misspellings with more than 20K matches to find potential false positives

## 2.4.0 (31 de marzo de 2020)
- Corrección crítica de configuración que escribía mensajes en consola en Producción
- T238972: Adaptación al nuevo esquema de los dumps
- T239866: Adaptación a la nueva compresión de los dumps
- Adaptación a cambios en la API de Wikipedia
- Mejora de rendimiento en algunos buscadores de falsos positivos
- Adaptación de la indexación a Spring Batch
- Actualización de dependencias: SpringBoot 2.2.5 y Angular 9.1
- Refactorización en el backend
- Inicio de documentación técnica (en inglés)
- Inicio de preparación para usarse en otros proyectos e idiomas

## 2.3.7 (30 de septiembre de 2019)
- Corregir bucle infinito al buscar reemplazos personalizados
- Corregir permisos para mostrar ciertas secciones 

## 2.3.6 (26 de septiembre de 2019)
- Ignorar plantillas de traducción
- Enlace a la página de la herramienta en la Wikipedia en el pie 
y en el resumen de cada edición 

## 2.3.5 (24 de septiembre de 2019)
- Mejoras en la captura de nombres de ficheros
- Ignorar artículos con las plantillas «Destruir» o «CopyEdit» 

## 2.3.4 (24 de septiembre de 2019)
- Ignorar plantilla «TA»
- Mejoras en el proceso de indexación
- Mejoras en la captura de enlaces interlingüísticos

## 2.3.3 (20 de septiembre de 2019)
- Fechas: corregir también «De» antes del año

## 2.3.2 (19 de septiembre de 2019)
- Fechas: tener en cuenta «De» en mayúsculas
- Mostrar listas de reemplazos separadas por tipos

## 2.3.1 (18 de septiembre de 2019)
- Reemplazos de ortografía con más de una palabra
- Mejoras en la captura de plantillas completas
- Ignorar los enlaces interlingüísticos
- Ignorar todas las plantillas de citas
- Ignorar dominios web

## 2.3.0 (13 de septiembre de 2019)
- Nuevo tipo de reemplazos: fechas sin día y con el mes en mayúscula.
- Optimización: se reduce el tamaño interno de los datos para la tabla de reemplazos
- Mejora en la captura de nombres de ficheros
- Se añaden títulos (en la barra del navegador) a todas las secciones
- Se reindexa siempre un artículo del dump si su fecha coincide con la última indexación,
para indexar los nuevos tipos de reemplazos. 

## 2.2.1 (7 de septiembre de 2019)
- Corregir casos puntuales al leer las secciones de un artículo
- Optimización: al buscar un artículo para revisar, recibir todos los datos
y así ahorrar una llamada al backend.

## 2.2.0 (5 de septiembre de 2019)
- Guardar última indexación en la base de datos
- Editar solo la sección del artículo con los reemplazos para reducir y
optimizar los datos entre el navegador y el servidor, a costa de aumentar un
poco el tráfico entre el servidor y la Wikipedia.
- Mejoras en la captura de nombres de ficheros sin el prefijo indicador
- Mejoras cosméticas al guardar: simplificar hiperenlaces redundantes.
- Mejoras en la captura de etiquetas cerradas
- Reversión: se dejan de capturar las líneas que comienzan por un espacio
en blanco (texto preformateado) pues hay mucho contenido de plantillas que
cumple esta regla y que no nos interesa ignorar.

## 2.1.2 (20 de agosto de 2019)
- Mejoras en la búsqueda de reemplazos personalizados con mayúsculas
- Resaltado del botón de Guardar cambios
- Mejoras en el proceso de indexación

## 2.1.1 (17 de agosto de 2019)
- Mejoras en la visualización de las sugerencias de reemplazos en móviles
- Mejoras de rendimiento: no se comprueba el estado de la indexación si no
se está en esa pantalla.

## 2.1.0 (8 de agosto de 2019)
- Corrección en la captura de plantillas completas, como las citas.
- Cambios en la búsqueda de artículos con reemplazos. Se recupera la funcionalidad de que la búsqueda devuelva un
artículo con reemplazos, gestionando los que no tenga en segundo plano, disminuyendo por tanto la cantidad de mensajes
en pantalla.

## 2.0.6 (4 de agosto de 2019)
- Corrección en la ordenación de la tabla de reemplazos
- Mejoras en la indexación para evitar reemplazos duplicados

## 2.0.5 (28 de julio de 2019)
- Mejoras de rendimiento al buscar etiquetas para ignorar
- Ignorar texto rodeado por subrayados o barras
- Ignorar la etiqueta `nowiki`
- Ignorar la plantilla `cita libro`
- Ignorar líneas comenzando por espacio (texto preformateado)
- Correcciones en el paginado del listado de reemplazos
- Mejora en la indexación para ignorar los artículos ya indexados sin reemplazos encontrados

## 2.0.4 (10 de julio de 2019)
- Mejora en la indexación para ignorar los artículos ya indexados sin reemplazos encontrados
- Corrección de un error al recargar las pantallas

## 2.0.3 (8 de julio de 2019)
- Corrige captura de fechas que comienzan por 0
- Muestra el texto original siempre como la primera sugerencia
- Corrige un bucle infinito en los reemplazos personalizados cuando todos están revisados
- Corrige la plantilla "Caja de cita" que no se estaba ignorando

## 2.0.2 (7 de julio de 2019)
- Corrige indexación truncada
- Pasa a mayúsculas los reemplazos personalizados si procede
- Otras correcciones menores

## 2.0.1 (5 de julio de 2019)
- Corrección de enlaces a algunas secciones

## 2.0.0 (4 de julio de 2019)
- Frontend independiente implementado en Angular aunque se sigue desplegando junto con el backend
- Proceso de autenticación separado para facilitar el desarrollo
- Implementación propia para llamar al API de Wikipedia sin depender de librerías externas
- Optimización de los buscadores de errores ortográficos y excepciones
- Se ofrecen varias alternativas de reemplazos (si las hay)
- Al editar un tipo de reemplazo en concreto solo se muestra ese
- Buscador de reemplazos para fechas con meses en mayúscula
- Posibilidad de buscar reemplazos personalizados 