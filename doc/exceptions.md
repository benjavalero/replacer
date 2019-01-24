# Excepciones

La herramienta ignora errores en ciertas circunstancias en las que siempre (o casi) estos errores son falsos positivos.

## Expresiones

* Comentarios HTML: `<!-- españa -->`
* Nombres de archivos: `[[File:españa.png|España]]`
* Parámetro índice: `{{... | índice = españa | ...}}`
* Cursiva, negrita y entrecomillados: `''online''`, `'''Lopez'''`,
 `"In Paris"`, `«In Paris»`, `“In Paris”` 
* Código fuente (`source`, `syntaxhighlight` y `math`): `<math>LaTeX</math>`
* Nombres de plantillas: `{{Album | ...}`
* Parámetros: `{{ ... | pais = España | ... }}`
* Plantillas completas: `ORDENAR`, `DEFAULTSORT`, `NF`, `Cita`, `Quote`,
 ` Coord`, `Commonscat`
* Categorías: `[[Categoría:Jennifer Lopez]]` 
* URL: `http://www.jenniferlopez.com`
* Etiquetas XML: `<ref name="españa">`
