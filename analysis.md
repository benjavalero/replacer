El propósito del proyecto es proporcionar una interfaz web sencilla para corregir algunos de los errores ortográficos o de estilo más comunes en la Wikipedia en español.

Este documento desglosa algunas de las historias de usuario resueltas, con comentarios técnicos.

## Buscar el dump más reciente

> Como sistema, quiero encontrar el **dump** más reciente para indexarlo.

La Wikipedia proporciona periódicamente (más o menos cada mes) una serie de exportaciones, denominadas dumps, de todas las páginas existentes. Se pueden descargar desde https://dumps.wikimedia.org/. Hay varios tipos, aunque para este proyecto nos interesa el fichero de nombre `eswiki-yyyymmdd-pages-articles.xml.bz2` que contiene las últimas versiones de todas las páginas en la fecha de generación `yyyymmdd`.

Por otra parte, la aplicación está pensada para desplegarse en los servidores ToolForge de Wikimedia, por lo que estos dumps son accesibles directamente en el sistema de ficheros en una ruta específica, en subcarpetas cuyo nombre se corresponde a la fecha de generación en formato `yyyymmdd`.

 Puede darse el caso de que la subcarpeta más reciente aún no contenga el _dump_ que nos interesa porque aún no ha sido generado, y tengamos que buscar el más reciente en la subcarpeta correspondiente a la anterior generación.

 La ruta de la carpeta base que contiene los dumps de la Wikipedia en español se define en la propiedad `dump-base-path`.

 Clases relacionadas:
 - `DumpFileFinder.java`