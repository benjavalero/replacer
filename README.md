# Replacer (WikiReemplazador)

El objetivo de esta herramienta es proporcionar una interfaz simple para
mostrar potenciales reemplazos en artículos de la Wikipedia en español,
especialmente errores ortográficos o de estilo. 

Aunque prácticamente la mitad de los artículos de la Wikipedia en español
contienen potenciales reemplazos, este proyecto va a utilizar y actualizar
una base de datos con la información de qué artículos contienen qué
reemplazos, y cuáles han sido ya revisados.


## Indexación

Wikipedia vuelca mensualmente toda la información en _dumps_, en particular
en un XML enorme con todo el contenido. Semanalmente el sistema comprueba
comprobaremos si hay un nuevo _dump_ disponible y lo indexa:
- Solo se tienen en cuenta los contenidos de tipo «Artículo» o «Anexo»
- Se reindexan los artículos modificados posteriormente a su inserción en
la base de datos o a su revisión
- Hay una opción para reindexarlo todo para tener en cuenta nuevas
excepciones o potenciales reemplazos

El sistema además ofrece una sección para comprobar el estado de la
indexación en tiempo real.

## Búsqueda de reemplazos

Para cada artículo, buscamos posibles reemplazos que validará el usuario.
Actualmente, los únicos reemplazos disponibles son potenciales faltas de
ortografía.

### Errores ortográficos

Las posibles faltas de ortografía se extraen del artículo
«Wikipedia:Corrector_ortográfico/Listado».

Hay muchísimos casos de falsos positivos. El sistema intenta minimizar
éstos ignorando las faltas de ortografía que se encuentran en partes del
texto que se consideran «excepciones»: frases entrecomilladas o en cursiva,
nombres de ficheros, citas, etc.


## Optimizaciones

### Búsqueda de reemplazos

Hay expresiones regulares que pueden llegar a consumir mucho tiempo o recursos
para textos muy largos. Esto no es muy importante al analizar un artículo en
concreto pero sí cuando se analiza toda la Wikipedia.

He realizado un pequeño estudio sobre el tamaño de los artículos de la
Wikipedia. Hay aproximadamente unos 1,5 millones que son artículos y anexos.
* Mínimo: 18 bytes
* Primer cuartil: 2 kB
* Mediana: 3 kB
* Tercer cuartil: 6 kB
* Máximo: 771 kB (artículo «Literatura victoriana»)

Nótese que solo el 1 % de los artículos tiene más de 55 kB y solo el 1 ‰ tiene más
de 150 kB.

Por tanto, a la hora de optimizar las expresiones regulares, he tenido en
cuenta tres tipos de artículos:
* Artículos medianos de unos 3 kB (50 % del total): «Aquifoliaceae»
* Artículos largos de unos 50 kB (1 % del total): «América del Norte»
* Artículo extremo de 771 kB: «Literatura victoriana»

También he tenido en cuenta dos tipos de motores de expresiones regulares:
_regex-directed_ y _text-directed_. El primero es el habitual, y que
contiene todas las características interesantes: _look-ahead_, _look-behind_,
_lazy_, _possessive_, _back-references_, etc. El segundo es más limitado en
sintaxis pero a cambio ofrece un rendimiento lineal, logrando en algunos
casos un rendimiento 1000 veces superior. A la hora de analizar un solo
artículo no se nota mucho, pero cuando hay que analizar toda la Wikipedia
la diferencia es notable.

En general, siempre que he logrado encontrar una expresión satisfactoria,
he optado por usar la versión _text-directed_. Aquí el único cuantificador
válido es «+». Para el resto de casos he usado la versión estándar,
usando el cuantificador «+?» en la mayoría de ocasiones por ser el más
eficiente en los experimentos.

### Indexación

El proceso de indexación tiene dos partes principales: la lectura de cada uno
de los artículos y el procesado de los artículos (si procede).

En cuanto a la lectura de los artículos del _dump_ no hay optimización posible.
La lectura de un XML es mucho más rápida que de un XML comprimido (estimo que
unas 10 veces) pero en la máquina de Wikipedia solo tenemos la opción de usar
la versión comprimida.

En cuanto al procesado de los artículos, también tenemos 3 partes claras:
1. Consultar la BD para ver si el artículo ya existe y cuál es su estado
2. Buscar los errores potenciales y descartar los contenidos en excepciones
3. Guardar en BD los reemplazos detectados en el artículo
 

 


