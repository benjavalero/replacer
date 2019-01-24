# Detalles técnicos

## Nomenclatura

- *Article*: artículo o página de la Wikipedia.
- *Namespace*: determina el tipo de página: artículo, anexo, usuario, etc.
- *Replacement*: potencial reemplazo, p. ej. el término «habia» que es candidato a reemplazarse con «había».
- *IgnoredReplacement*: porción de un artículo que no se tiene en cuenta para buscar los reemplazos, p. ej. las frases entrecomilladas.
- *Dump*: ficheros generados mensualmente con toda la información en la Wikipedia. El que usa esta herramienta es un XML enorme (~13 GB, ~3 GB comprimidos) con todos los artículos de la Wikipedia.


## Búsqueda de reemplazos

Para cada artículo, buscamos posibles reemplazos que validará el usuario. Actualmente, los únicos reemplazos disponibles son potenciales faltas de ortografía.

### Errores ortográficos

Las posibles faltas de ortografía se extraen del artículo «Wikipedia:Corrector_ortográfico/Listado».

Hay muchísimos casos de falsos positivos. El sistema intenta minimizar éstos ignorando las faltas de ortografía que se encuentran en partes del texto que se consideran excepciones: frases entrecomilladas o en cursiva, nombres de ficheros, citas, etc. (v. [Excepciones](exceptions.md))

### Optimizaciones

Hay expresiones regulares para encontrar reemplazos o excepciones que pueden llegar a consumir mucho tiempo o recursos para textos muy largos.
Esto no es muy importante al analizar un artículo en concreto pero sí cuando se analiza toda la Wikipedia.

He realizado un pequeño estudio sobre el tamaño de los artículos de la Wikipedia. Hay aproximadamente 1,5 millones que son artículos y anexos.
* Mínimo: 18 bytes
* Primer cuartil: 2 kB
* Mediana: 3 kB
* Tercer cuartil: 6 kB
* Máximo: 771 kB (artículo «Literatura victoriana»)

Nótese que solo el 1 % de los artículos tiene más de 55 kB y solo el 1 ‰ tiene más de 150 kB.

Por tanto, a la hora de optimizar las expresiones regulares, he tenido en cuenta tres tipos de artículos:
* Artículos medianos de unos 3 kB (50 % del total): «Aquifoliaceae»
* Artículos largos de unos 50 kB (1 % del total): «América del Norte»
* Artículo extremo de 771 kB: «Literatura victoriana»

También he tenido en cuenta dos tipos de motores de expresiones regulares: _regex-directed_ y _text-directed_. El primero es el habitual, y que contiene todas las características interesantes: _look-ahead_, _look-behind_, _lazy_, _possessive_, _back-references_, etc.
El segundo es más limitado en sintaxis pero a cambio ofrece un rendimiento lineal, logrando en algunos casos un rendimiento 1000 veces superior.

En general, siempre que he logrado encontrar una expresión satisfactoria, he optado por usar la versión _text-directed_. Aquí el único cuantificador válido es «+». Para el resto de casos he usado la versión estándar, usando el cuantificador «+?» en la mayoría de ocasiones por ser el más eficiente en los experimentos.


## Indexación

El sistema comprueba semanalmente el último _dump_ generado y lo procesa, esto es, lee uno a uno los artículos, busca los potenciales reemplazos y los añade a la base de datos.

El proceso de indexación tiene dos partes principales: la lectura de cada uno de los artículos y el procesado de los artículos (si procede):

- Solo se procesan los contenidos de tipo «Artículo» o «Anexo».
- Se procesan los artículos ya indexados para tener en cuenta nuevas excepciones o potenciales reemplazos.
- No se procesan los artículos ya revisados manualmente.
- Hay una opción para forzar y reindexarlo todo, incluso los artículos revisados.

El sistema además ofrece una sección para comprobar el estado de la indexación en tiempo real.

### Optimizaciones

En cuanto a la lectura de los artículos del _dump_ no hay optimización posible.
La lectura de un XML es mucho más rápida que de un XML comprimido (estimo que unas 10 veces) pero en la máquina de Wikipedia solo tenemos la opción de usar la versión comprimida.

En cuanto al procesado de los artículos, también tenemos 3 partes claras:
1. Consultar la BD para ver si el artículo ya existe y cuál es su estado.
El sistema busca varios (1000) artículos a la vez para reducir el número de llamadas a BD.
2. Buscar los errores potenciales y descartar los contenidos en excepciones.
El sistema intenta terminar lo antes posible en el caso de no encontrar errores potenciales.
3. Guardar en BD los reemplazos detectados en el artículo. El sistema intenta realizar solo las inserciones, borrados y actualizaciones necesarias.

La herramienta puede llegar a procesar más de un millón de artículos, con lo cual el uso de memoria por parte de JPA no para de crecer. Para evitarlo se ha eliminado la relación _one-to-many_ entre artículos y reemplazos (aunque se mantiene la clave ajena en la BD), y cada cierto número de artículos procesados se limpia el gestor JPA (_flush-clear_). Con esto conseguimos mantener a raya el _heap_ de la JVM.
