# Lab08: tareas con MVVM, Room y Firebase

Aplicación Android en Kotlin y Jetpack Compose. La parte guiada usa Room para guardar tareas, y el ejercicio 1 añade una vista inspirada en **Hoy** de [Todoist](https://www.todoist.com/help/todoist/get-started/plan-your-day-with-the-today-view-UVUXaiSs).

## Funciones

- Crear tareas, marcarlas como completadas y eliminar todas, como en la parte guiada.
- Buscar tareas por texto y eliminar una tarea individual con confirmación.
- Programar recordatorios locales de tareas pendientes. Android puede retrasar algunos avisos para ahorrar batería.
- Sincronizar tareas entre dispositivos con la misma cuenta de Google mediante Firebase Authentication y Cloud Firestore. La app conserva una copia local en Room para funcionar sin conexión; los cambios pendientes se envían al recuperar la conexión.

## Estructura

- `data/`: entidad, DAO y base Room. La migración 1→2 conserva las tareas previas.
- `ui/`: pantalla Compose y ViewModel.
- `reminders/`: programación con WorkManager y notificación.
- `cloud/`: inicio de sesión con Google y sincronización con Firestore.
- `firestore.rules`: reglas de acceso a las tareas del usuario autenticado.

## Ejecutar

Abre el proyecto en Android Studio y ejecuta el módulo `app`. Para compilar desde terminal usa `gradlew.bat :app:assembleDebug` en Windows. `app/google-services.json` es la configuración pública del cliente Firebase y se incluye para que la sincronización funcione al clonar el repositorio. Las reglas de Firestore protegen los datos; este archivo no contiene credenciales privadas.

El proyecto Firebase usado en este laboratorio es `lab08-tareas-mvvm-40489`. En Authentication está habilitado el proveedor Google. La huella SHA-1 de depuración de este equipo está registrada. Si compilas con otra clave, obtén su huella con `gradlew.bat :app:signingReport`, agrégala en Firebase y vuelve a descargar `google-services.json`; para distribuir la app se requiere registrar también la huella de la clave de publicación. Cloud Firestore usa la región `southamerica-west1` y las reglas de `firestore.rules`.

Para sincronizar, pulsa **Conectar**, elige una cuenta de Google y usa la misma cuenta en otro dispositivo. Las tareas locales existentes se suben al iniciar sesión por primera vez. Los borrados se conservan como marcas internas para que se propaguen a otros dispositivos.

## Capturas sugeridas para el ejercicio 1

1. **Diseño y búsqueda:** muestra la pantalla «Mis tareas», escribe una palabra en «Buscar tareas» y captura la lista filtrada.
2. **Borrado individual:** pulsa la papelera de una tarea de prueba y captura el cuadro «Eliminar tarea»; confirma y captura la lista sin esa tarea.
3. **Recordatorio:** pulsa la campana de una tarea pendiente, concede permiso para notificaciones si Android lo pide, elige fecha y hora futuras, y captura el texto «Aviso: ...» bajo la tarea. Para evidenciar la notificación, programa una hora próxima y captura el panel de notificaciones cuando llegue.
4. **Nube:** pulsa **Conectar** y elige tu cuenta de Google. Captura «Nube: sincronizada» en la app y el documento correspondiente en Firebase Console > Firestore > `users/{uid}/tasks`. Para demostrar dos dispositivos, abre la app con la misma cuenta en otro emulador o teléfono y captura la tarea allí.

En `docs/Lab08-funcionando.png` está la captura de la parte guiada. La historia Git conserva los commits de esa parte y los cambios de este ejercicio por función.
