# Mi inventario — lectura y actualización con Room

Proyecto independiente para el módulo [Cómo leer y actualizar datos con Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-update-data-room?hl=es-419). Se abrió la rama `room` del [proyecto base oficial](https://github.com/google-developer-training/basic-android-kotlin-compose-training-inventory-app) y se completaron las operaciones pendientes. La interfaz en español, la paleta azul y la tarjeta de resumen son una adaptación propia para el laboratorio.

## Qué se implementó

- La lista observa el `Flow` del repositorio con `StateFlow`, así que cambia al insertar, editar o eliminar productos.
- La vista de detalle lee el producto por ID, permite registrar una venta (resta una unidad si hay stock) y eliminarlo.
- La pantalla de edición recupera los valores existentes y guarda los cambios en Room.
- `ItemDaoTest` comprueba inserción, lectura ordenada, actualización y borrado con una base de datos temporal.

## Ejecutar

Abre esta carpeta como proyecto independiente en Android Studio y ejecuta `app`. Desde PowerShell, con Android SDK y JDK 17 configurados, usa `./gradlew.bat :app:assembleDebug`. El APK de depuración queda en `app/build/outputs/apk/debug/`.

Para comprobarlo en pantalla: añade un producto con nombre, precio y cantidad; abre el detalle; pulsa **Registrar venta**; edita el nombre o la cantidad; vuelve a la lista y comprueba los cambios. La información permanece al cerrar y volver a abrir la aplicación.

## Origen y licencia

El proyecto base pertenece a Google Developer Training y conserva su licencia Apache 2.0 en `LICENSE`. La solución y el diseño descritos arriba son cambios de este laboratorio sobre ese material.
