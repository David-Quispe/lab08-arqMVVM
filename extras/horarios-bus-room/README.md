# Mi ruta diaria — horarios con Room

Proyecto independiente para la [práctica de Bus Schedule](https://developer.android.com/codelabs/basic-android-kotlin-compose-practice-bus-schedule-app?hl=es-419). Se utilizó la rama `starter` del [proyecto base oficial](https://github.com/google-developer-training/basic-android-kotlin-compose-training-bus-schedule-app). La pantalla en español, su paleta verde agua, tarjetas y cabecera explicativa son una adaptación propia para el laboratorio.

## Qué se implementó

- La entidad `BusSchedule` corresponde a la tabla `Schedule` de la base SQLite incluida en `assets`.
- `BusScheduleDao` consulta todos los horarios por hora de llegada y también filtra por nombre de parada.
- `BusScheduleDatabase` crea la base Room a partir del archivo precargado.
- El ViewModel expone los datos del DAO como `Flow` y la interfaz muestra la lista completa y el detalle de una parada.
- `BusScheduleDaoTest` verifica en el emulador los 31 horarios precargados, su orden y el filtro por `Main Street`.

## Ejecutar

Abre esta carpeta como proyecto independiente en Android Studio y ejecuta `app`. Desde PowerShell, con Android SDK y JDK 21 configurados, usa `./gradlew.bat :app:assembleDebug`. El APK de depuración queda en `app/build/outputs/apk/debug/`.

Toca una parada de la lista para ver solo sus horarios. Por ejemplo, `Main Street` muestra cuatro llegadas.

## Origen y licencia

El proyecto base y la base de datos de ejemplo pertenecen a Google Developer Training; se conserva la licencia Apache 2.0 en `LICENSE`. La solución y el diseño descritos arriba son cambios de este laboratorio sobre ese material.
