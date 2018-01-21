# Excepciones

La herramienta ignora errores en ciertas circunstancias en las que siempre
(o casi) estos errores son falsos positivos.

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

## Falsos positivos

* **Index**. Ignoro los casos con mayúscula que son casi todos en inglés.
 Los casos en minúscula son casi todos parte de URL.
* **link**. Hay muchísimos casos del tipo `[http://www.google.es link]` que
 __pueden reemplazarse__ dejando solo la URL (hay que analizarlo detenidamente).
 Hay casos en minúscula que son un parámetro en plantillas.
 Ignoro los casos en mayúscula, que son casi todos en inglés.
* **online**. Hay muchísimos casos del tipo `[http://www.google.es online]` que
 __pueden reemplazarse__ dejando solo la URL (hay que analizarlo detenidamente).
 Hay casos en minúscula que son un parámetro en plantillas.
 Ignoro los casos en mayúscula, que son casi todos en inglés.
* **reference(s)**. Lo ignoro completamente, casi todos los casos son en inglés.
 Se corrigen automáticamente las ocurrencias de `== References ==`.
* **aun** (incluso, aunque) y **aún** (todavía). Ambos son errores potenciales, porque tienden a confundirse entre sí.
 La mayoría de ocurrencias de "aún así" se corrigen automáticamente por "aun así".
 Se ignoran los casos "aun así", "más aún" y "aún más" que en principio sí son correctos.
* **\&lt;**. "lt" se usa a veces incorrectamente como símbolo del litro.
 Hay algunos falsos positivos como la entidad HTML "&lt;",
 el código de Lituania o la abreviatura de Lieutenant.
 Ignoro la entidad HTML y los casos con mayúscula "Lt".
* **comic(s)**. Ignoro los casos en mayúscula "Comics" que son casi todos en inglés.
 Ignoro también "Comic Con” (1000 casos, con y sin guion).
* **publica** (**especifica**, **practica**, etc.). Hay muchos casos en que se refiere al verbo (3.ª persona).
 Ignoro los casos impersonales precedidos por "se": "se publica" (3500),
 "se especifica" (5600) y "se practica" (2000). 
* **tropicos**. Ignoro los casos "Tropicos.org" (21 500).
* **Domingo** y **Julio**. En mayúscula son correctos si son nombres propios.
 Ignoro los casos sucedidos por una letra mayúscula (que en principio sería el apellido).
* **magazine**. Ignoro los casos en mayúscula que son casi todos en inglés.
* **records**. Ignoro los casos en mayúscula que son casi todos en inglés.
* **propulsion**. Ignoro los casos "Jet Propulsion Laboratory" (2000).
* **sky**. Ignoro los casos en mayúscula que son casi todos en inglés (80 000 en el XML).
* **guardian**. Ignoro los casos "The Guardian" (7800).
* **Missouri**. Ignoro los casos "Missoui Botanical Garden" (21 900).
* **regreso** (**retiro**, **desempeño**, etc.). Hay muchos casos en que se refiere al sustantivo. 
 Ignoro los casos precedidos por "de", "su" y "el": "de regreso" (8900),
  "el regreso" (9500), "su regreso" (10 900), "), "de retiro" (6100),
  "el retiro" (2600) y "su retiro" (5100), "de desempeño" (400),
  "el desempeño" (2400) y "su desempeño" (2200).
* **Victor**. Ignoro los casos "Victor Hugo" (1100).
* **Menem**. Ignoro los casos "Carlos Menem" (1000).
* **Angeles**. Ignoro los casos "Los Angeles Lakers" (1200) y "Los Angeles Times" (4200).
* **geografia**. Ignoro los casos "Instituto Brasileiro de Geografia e Estatística" (1800).
* **Verano** e **Invierno**. Las estaciones van en minúscula.
 Ignoro los casos "Juegos Olímpicos de "Verano/Invierno".
* **America**. Ignoro los casos "North America" (5800), "South America" (3000),
 "Central America" (2000) y "Latin America" (2000).
* **Manchester**. Ignoro los casos "​Manchester United" (2000) y "Manchester City" (1400).
* **Martin**. Ignoro los casos "Saint Martin" (2000, con y sin guion).
* **espécies**. Ignoro los casos "Lista de espécies Flora do Brasil" (2500).
* **Superman**. Ignoro los casos en mayúscula (2300) que se refieren la mayoría al nombre propio del personaje. 
* **Sandwich**. Ignoro los casos en mayúscula (1500) que se refieren la mayoría a las islas.
* **uruguay**. Ignoro los casos `[[uruguay]]o` (2300).
* **italia**. Ignoro los casos `[[italia]]no` (4700).
* **brasil**. Ignoro los casos `[[brasil]]eño` (3500).

## Verdaderos positivos

* **éste**, **ése** y **aquél** (y derivados).
 Se recomienda escribir el pronombre sin tilde salvo en casos de confusión con el determinante.
 Hay muchos usuarios reticentes así que de momento ignoro estos casos.
* **sólo**. Se recomienda escribir el adverbio sin tilde salvo en casos de confusión con el adjetivo.
 Hay muchos usuarios reticentes así que de momento ignoro estos casos.
* **fans**. Hay miles de casos (unos 11 000). Aunque está asentado el uso de fans,
 se recomienda acomodar esta palabra a la morfología española.
 De momento la dejo como está.
* **setiembre**. En la normal culta se recomienda "septiembre".
 Aun así, algunos usuarios prefieren que no se corrija.
 Prefiero ignorar estos casos de momento.
* **guión**. Los monosílabos ortográficos no se tildan nunca, salvo en los casos de tilde diacrítica.
 Ahora mismo la mayoría de los casos están reemplazados, pero aún quedan unos 6500 casos con tilde.
 No ignoro estos casos.
