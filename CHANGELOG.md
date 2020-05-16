# Versions

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