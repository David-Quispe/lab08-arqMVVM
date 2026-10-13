# Lab08 - Lista de tareas con MVVM y Room

Aplicación Android en Kotlin y Jetpack Compose para agregar tareas, marcar su estado y eliminar todas las tareas. Room guarda la lista entre aperturas de la aplicación.

## Estructura

- `data/Task.kt`: entidad de la tabla `tasks`.
- `data/TaskDao.kt`: consultas y operaciones de escritura.
- `data/TaskDatabase.kt`: base de datos Room.
- `ui/TaskViewModel.kt`: estado y acciones de la pantalla.
- `MainActivity.kt`: interfaz Compose e integración con Room.

## Ejecutar

Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar el módulo `app` en un emulador o dispositivo Android. También se puede compilar con `./gradlew assembleDebug` (o `gradlew.bat assembleDebug` en Windows).

## Verificación

Se comprobó en un emulador Android que se pueden agregar tareas, cambiar su estado, cerrar y volver a abrir la app sin perder los datos, y eliminar todas las tareas. La captura está en [docs/Lab08-funcionando.png](docs/Lab08-funcionando.png).

La historia de Git separa la configuración, la entidad, el DAO, la base de datos, el ViewModel y la vista en commits individuales.
