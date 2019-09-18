# Versiones

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