# Replacer - Reemplazador de la Wikipedia en español

El objetivo de esta herramienta es proporcionar una interfaz simple para revisar y corregir los errores ortográficos o de estilo más comunes en la Wikipedia en español.

La herramienta está desplegada en los servidores de Wikimedia Toolforge, y disponible en https://tools.wmflabs.org/replacer. Para utilizar la herramienta, basta iniciar sesión con un usuario de la Wikipedia. Las ediciones que se realicen serán registradas en las contribuciones del usuario.

El código fuente de la herramienta está disponible en [GitHub](https://github.com/benjavalero/replacer).

## Arquitectura

La aplicación está compuesta de dos capas independientes, el frontend en Angular y el backend en Java 8 y Spring Boot, además de una base de datos MariaDB alojada en los servidores de Toolforge.

### Despliegue en local

La aplicación se puede probar en local, sin necesidad de conectarnos a los servidores de Toolforge, desplegando el backend y el frontend de manera independiente, con un perfil especial de Maven.

```
$ cd replacer-backend
$ mvn clean spring-boot:run -DskipTests -Poffline
```
```
$ cd replacer-frontend
$ npm start
```

La aplicación estará disponible en http://localhost:8080. Al usar el perfil _offline_, se truca la aplicación para usar una base de datos en memoria que siempre devuelve el mismo artículo en `replacer-backend\src\main\resources\article-long.txt`.

### Despliegue en Toolforge

En los servidores de Toolforge, la aplicación se empaqueta como un solo _jar_ ejecutable. Además hay que configurar los tokens de autenticación, así como la clave de la base de datos, dentro del fichero `<CARPETA  REPLACER>/replacer-backend/src/main/resources/application.yml`.

El siguiente comando se ejecuta en la carpeta raíz del proyecto, y copia los estáticos del frontend dentro del backend. El archivo a ejecutar se genera en `replacer-backend/target/replacer.jar`.
```
$ mvn clean package -DskipTests
```

## Nomenclatura

- *Page*: Cada una de las páginas de la Wikipedia: artículos, discusiones, anexos, usuarios, plantillas, etc.
- *Namespace*: espacio de nombres que determina el tipo de página: artículo (`0`), anexo (`104`), etc.
- *Article*: artículo de la Wikipedia, i. e. página en el _namespace_ de artículos.
- *Replacement*: potencial reemplazo, e. g. el término «habia» debe reemplazarse con «había», y el término «entreno» es candidato a reemplazarse con «entrenó» o en cambio ser correcto.
- *Ignored Replacement*: porción de un artículo que no se tiene en cuenta para buscar los reemplazos, e. g. las frases entrecomilladas.
- *Dump*: ficheros generados mensualmente con toda la información en la Wikipedia. El que usa esta herramienta es un XML enorme (~13 GB, ~3 GB comprimidos) con todos los artículos de la Wikipedia.

## Módulos

### Autenticación

La autenticación se realiza mediante el protocolo Oauth 1.0a contra la API de Wikimedia, lo que permite utilizar la aplicación con los mismos usuarios con los que editamos normalmente en Wikipedia. Por seguridad, se implementa la autenticación en el backend, y el frontend solo contendrá el último token de acceso.

La dirección de vuelta tras autenticar, si se usan los tokens de Producción, es la de inicio: https://tools.wmflabs.org/replacer/. En cambio, si se usan los tokens de desarrollo, es http://localhost:8080/.

### Wikipedia API

La herramienta no precisa de muchas llamadas distintas a la API de la Wikipedia, principalmente necesita recuperar el contenido de una página y editarlo. Aunque hay librerías que lo facilitan, como [Mediawiki-Japi](https://github.com/WolfgangFahl/Mediawiki-Japi), no permiten la autenticación con Oauth por lo que el autor de las ediciones no quedaría reflejado.

Finalmente he decidido implementar todas las llamadas dentro de la propia herramienta, que irán firmadas con el token OAuth, incluso las de lectura.

### Buscador de reemplazos

Este módulo expone una interfaz que deben implementar todos los buscadores de reemplazos a partir de un texto. Así mismo expone una interfaz para buscar _excepciones_.

#### Excepciones

Hay ciertas partes de un texto que queremos ignorar pues se suelen detectar muchos falsos positivos, por ejemplo una cita en español antiguo o un parámetro propio de la Wikipedia que no acepta diacríticos:
* Comentarios HTML: `<!-- españa -->`
* Nombres de archivos: `[[File:españa.png|España]]`
* Etiquetas XML completas, e. g. para citas o código fuente: `<math>LaTeX</math>`
* Plantillas completas, e. g. para citas o coordenadas: `{{Cita|Texto}}`
* Textos entrecomillados: `«In Paris»`
* Textos en cursiva: `''online''`
* Valores de algunos parámetros:  `{{... | índice = españa | ...}}`
* Categorías: `[[Categoría:Jennifer Lopez]]`
* Etiquetas XML: `<span style="color:green;">` o `<br />`
* Nombres de plantillas: `{{Album|...}}`
* Parámetros de plantillas: `{{...| pais = España | ...}}`
* URL: `http://www.jenniferlopez.com`
* Enlaces internos sufijados: `[[brasil]]eño`
* Enlaces internos con alias: `[[Taiwan|Taiwán]]`

### Errores ortográficos

Los errores ortográficos o _misspellings_ son posibles faltas de ortografía que se extraen del artículo «[Wikipedia:Corrector_ortográfico/Listado](https://es.wikipedia.org/wiki/Wikipedia:Corrector_ortogr%C3%A1fico/Listado)».

Se omiten los términos del listado que contienen números o puntos, puesto que éstos ya serán tratados en un buscador distinto enfocado exclusivamente en unidades de medida.

#### Excepciones
* Términos que suelen ser un nombre propio de persona: Julio, Frances, etc.
* Términos que deben escribirse en minúscula salvo cuando la puntuación lo exija.
* Falsos positivos muy comunes, e. g. `Los Angeles Lakers`. Se extraen del artículo «[Usuario:Benjavalero/FalsePositives](https://es.wikipedia.org/wiki/Usuario:Benjavalero/FalsePositives)».

### Article

Este módulo contiene las interacciones entre los reemplazos encontrados por los distintos buscadores y los reemplazos en la base de datos. Además busca artículos que estén por revisar así como distintas estadísticas sobre los reemplazos ya indexados.

### Indexación

El sistema comprueba semanalmente el último _dump_ generado y lo procesa, esto es, lee una a una las páginas, busca los potenciales reemplazos y los añade a la base de datos.

Para cada página encontrada:
- Solo se indexan las páginas de tipo «Artículo» o «Anexo»
- Se ignoran las páginas que redirigen a otras

Además no se vuelven a revisar aquellas páginas que no hayan sido modificadas desde la última indexación. El pequeño inconveniente es que en dichas páginas no se detectarán los nuevos reemplazos que se vayan implementando en la herramienta. Para solventar esto, existe una opción oculta para forzar la indexación en este caso, así como para observar el estado actual de la indexación.

## Base de datos

Se ha reducido el modelo de la base de datos a una sola tabla con todos los reemplazos indexados. Un reemplazo se caracteriza por tener:
- El artículo en que ha sido detectado
- El tipo de reemplazo (e. g. error ortográfico)
- El subtipo (e. g. el término erróneo)
- La posición en el texto en que ha sido detectado

Un reemplazo tiene dos estados: por revisar y corregido. Un reemplazo revisado pero no modificado (falso positivo) se considera por tanto como corregido. Por tanto, un reemplazo tiene también:
- Fecha de la última actualización, ya sea por haber sido revisado/corregido o reindexado.
- Usuario que lo ha revisado/corregido

## Optimizaciones

### Búsqueda de reemplazos

El número de páginas de la Wikipedia en español es inmenso (unos 3,7 millones), de las cuales la gran mayoría son artículos (aprox. 85%). Y entre los artículos más de la mitad son redirecciones (aprox. 55%).

A la hora de indexar los artículos para encontrar reemplazos, es fundamental optimizar los algoritmos o expresiones regulares utilizados.

Para las pruebas de rendimiento, se han extraído algunas estadísticas sobre la longitud de los artículos:

|        |       |
|--------|-------|
|N.º artículos|1 468 690|
|Media  |5974,42|
|Mínimo |21.00  |
|Q1     |1816   |
|Mediana|3289   |
|Q3     |6022   |
|Máximo |745 039 |

Si dibujamos el diagrama de caja y bigote, se infiere que los artículos con más de 10.000 caracteres son casos puntuales. Por otra parte, los artículos más cortos se corresponden en su mayoría con artículos de desambiguación.

Para hacer las pruebas de los distintos algoritmos, tomaremos 100 artículos de forma aleatoria. Ejecutaremos cada uno de los algoritmos repetidas veces y compararemos los tiempos.

Para los algoritmos con expresiones regulares, he tenido en cuenta dos tipos de motores de expresiones regulares, el tradicional _regex-directed_ y un autómata _text-directed_. El primero es el que viene con las librerías de Java y que contiene todas las características interesantes: _look-ahead_, _look-behind_, _lazy_, _possessive_, _back-references_, etc. El segundo es más limitado en sintaxis pero a cambio ofrece un rendimiento lineal, logrando en muchos casos un rendimiento muy superior.

Como regla general, dependiendo de la complejidad de la expresión regular, usaremos el autómata. Por otra parte, con el motor tradicional, siempre que sea posible, aplicaremos los cuantificadores _lazy_ o _possessive_, que mejoran un poquito el rendimiento.

### Indexación

Aunque la lectura de un XML es mucho más rápida que de un XML comprimido (aprox. 10 veces), en los servidores de Toolforge solo existe la opción de usar la versión comprimida.

En cuanto al procesado de los artículos, también tenemos 3 partes claras:
1. Consultar la BD para ver si el artículo ya existe y cuál es su estado. El sistema busca varios artículos a la vez para reducir el número de llamadas a BD.
2. Buscar los errores potenciales y descartar los contenidos en excepciones. El sistema intenta terminar lo antes posible en el caso de no encontrar errores potenciales.
3. Guardar en BD los reemplazos detectados en el artículo. El sistema intenta realizar solo las inserciones, borrados y actualizaciones necesarias, y en bloque.

La herramienta procesa más de un millón de artículos, con lo cual el uso de memoria por parte de JPA no para de crecer. Para evitarlo, cada cierto número de artículos procesados se limpia el gestor JPA (_flush-clear_). Con esto conseguimos mantener a raya el _heap_ de la JVM.
