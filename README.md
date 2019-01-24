# Replacer (WikiReemplazador)

El objetivo de esta herramienta es proporcionar una interfaz simple para corregir errores ortográficos o de estilo en la Wikipedia en español.

Está alojada y disponible en los servidores de Wikimedia Toolforge:

https://tools.wmflabs.org/replacer

La herramient busca artículos que contienen errores ortográficos potenciales, basándose en la lista del artículo «[Wikipedia:Corrector_ortográfico/Listado](https://es.wikipedia.org/wiki/Wikipedia:Corrector_ortogr%C3%A1fico/Listado)».

Además, excluye ciertas expresiones para tratar de ignorar un buen número de falsos positivos.
* V. [Excepciones](exceptions.md)
* V. [Lista de falsos positivos](https://es.wikipedia.org/wiki/Usuario:Benjavalero/FalsePositives)

Prácticamente la mitad de los artículos de la Wikipedia en español contienen potenciales reemplazos. La herramienta mantiene una base de datos (también en los servidores de Wikimedia Toolforge) con información relativamente reciente sobre qué artículos contienen reemplazos, y si ya han sido revisados.

El código está disponible en [GitHub](https://github.com/benjavalero/replacer).
* V. [Detalles técnicos](docs/technical-details.md)


## TODO

* Ofrecer más de una opción para reemplazar, ahora mismo solo ofrece la primera.
* Ofrecer otros potenciales reemplazos, como artículos de desambiguación o mejoras de estilo en fechas, cantidades, monedas, etc.
