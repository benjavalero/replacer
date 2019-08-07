# 2.1.0 (8 de agosto de 2019)
- Corrección en la captura de plantillas completas, como las citas.
- Cambios en la búsqueda de artículos con reemplazos. Se recupera la funcionalidad de que la búsqueda devuelva un
artículo con reemplazos, gestionando los que no tenga en segundo plano, disminuyendo por tanto la cantidad de mensajes
en pantalla.

# 2.0.6 (4 de agosto de 2019)
- Corrección en la ordenación de la tabla de reemplazos
- Mejoras en la indexación para evitar reemplazos duplicados

# 2.0.5 (28 de julio de 2019)
- Mejoras de rendimiento al buscar etiquetas para ignorar
- Ignorar texto rodeado por subrayados o barras
- Ignorar la etiqueta `nowiki`
- Ignorar la plantilla `cita libro`
- Ignorar líneas comenzando por espacio (texto preformateado)
- Correcciones en el paginado del listado de reemplazos
- Mejora en la indexación para ignorar los artículos ya indexados sin reemplazos encontrados

# 2.0.4 (10 de julio de 2019)
- Mejora en la indexación para ignorar los artículos ya indexados sin reemplazos encontrados
- Corrección de un error al recargar las pantallas

# 2.0.3 (8 de julio de 2019)
- Corrige captura de fechas que comienzan por 0
- Muestra el texto original siempre como la primera sugerencia
- Corrige un bucle infinito en los reemplazos personalizados cuando todos están revisados
- Corrige la plantilla "Caja de cita" que no se estaba ignorando

# 2.0.2 (7 de julio de 2019)
- Corrige indexación truncada
- Pasa a mayúsculas los reemplazos personalizados si procede
- Otras correcciones menores

# 2.0.1 (5 de julio de 2019)
- Corrección de enlaces a algunas secciones

# 2.0.0 (4 de julio de 2019)
- Frontend independiente implementado en Angular aunque se sigue desplegando junto con el backend
- Proceso de autenticación separado para facilitar el desarrollo
- Implementación propia para llamar al API de Wikipedia sin depender de librerías externas
- Optimización de los buscadores de errores ortográficos y excepciones
- Se ofrecen varias alternativas de reemplazos (si las hay)
- Al editar un tipo de reemplazo en concreto solo se muestra ese
- Buscador de reemplazos para fechas con meses en mayúscula
- Posibilidad de buscar reemplazos personalizados 