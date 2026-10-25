# Kaiju Inventario

Aplicación móvil Android para la **gestión y trazabilidad de inventario** de Kaiju. Permite registrar productos, controlar su stock, registrar movimientos y ventas, recibir alertas de stock bajo y revisar indicadores de ventas.

Proyecto académico (MVP) de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**, Duoc UC, Escuela de Informática y Telecomunicaciones, sede CITT San Bernardo. Corresponde a la **Evaluación Parcial 2**.

> Todos los datos de la aplicación son **ficticios**. No se utilizan datos personales, credenciales ni información productiva real.

## Integrantes (Grupo 8)

- Martin Cortes
- Christian Montecinos
- Gabriel Fuentes

## Problema que resuelve

Kaiju controla su inventario con registros manuales y planillas, lo que genera diferencias entre el stock registrado y el real, olvidos de entradas o salidas, poca visibilidad de los productos con pocas unidades y dificultad para saber qué reponer. La aplicación centraliza esa información y deja un historial trazable de cada cambio de stock.

## Funcionalidades implementadas

**Inicio de sesión**
- Formulario con validación de campos y mensaje de error por credenciales incorrectas.
- Usuario de prueba (ficticio): `admin` / `kaiju123`.

**Productos**
- Formulario de alta con código, nombre, descripción, categoría, precio, stock actual y stock mínimo.
- Validaciones por campo con ícono y mensaje de error (campos obligatorios, números válidos, valores no negativos, código no repetido).
- Lista del inventario con aviso de **stock bajo** cuando el stock es igual o menor al mínimo.
- Eliminación de productos.
- Lectura del código del producto con la **cámara** (QR o código de barras).

**Movimientos de stock**
- Registro de **entradas**, **salidas** y **ajustes**.
- Reglas de negocio: no se permite una salida mayor al stock disponible y el ajuste exige un motivo.
- Historial con tipo, cantidad, stock resultante, motivo y fecha (trazabilidad).

**Ventas**
- Selección de producto (manual o escaneando su código) y cantidad, con total calculado en pantalla.
- Cada venta descuenta stock, queda registrada y genera una salida en el historial de movimientos.
- No permite vender más unidades que el stock disponible.

**Resumen**
- Indicadores: total vendido, unidades vendidas, ventas realizadas y productos con stock bajo.
- Productos más vendidos, menos vendidos y ventas por categoría, con barras animadas.

**Notificaciones**
- Aviso de stock bajo cuando una venta, salida o ajuste deja un producto en su stock mínimo o menos.

## Recursos nativos del dispositivo

| Recurso | Uso en la app |
|---|---|
| Cámara (CameraX + ML Kit) | Escanear QR o códigos de barras para completar el código de un producto o seleccionarlo en una venta. El permiso se solicita en tiempo de ejecución y la app sigue funcionando si se rechaza. |
| Notificaciones | Alerta de stock bajo. El permiso se solicita al iniciar sesión (Android 13 o superior) y la app sigue funcionando si se rechaza. |

La app no guarda fotos ni imágenes: solo lee el texto del código escaneado.

## Animaciones

- Transición `Crossfade` al cambiar de sección en la barra inferior.
- Barras de progreso del resumen que se llenan de forma animada.
- Sacudida de los campos de formulario cuando falla una validación.
- Pulso del aviso de stock bajo en la tarjeta del producto.
- Animación de entrada y salida de las tarjetas de las listas (`animateItem`).
- Parpadeo del marco guía del escáner.

## Arquitectura y tecnologías

- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose con Material 3
- **Arquitectura:** MVVM con flujo de datos unidireccional (`UiState` + `StateFlow`)
- **Persistencia local:** Room (SQLite)
- **Cámara:** CameraX y ML Kit Barcode Scanning
- **SDK mínimo:** API 30 (Android 11)

Las validaciones están en los ViewModel, desacopladas de la interfaz.

```
app/src/main/java/cl/duoc/kaiju
├── data/
│   └── local/
│       ├── AppDatabase.kt
│       ├── dao/        ProductoDao, MovimientoDao, VentaDao
│       └── entity/     ProductoEntity, MovimientoEntity, VentaEntity
├── model/          estados de UI, errores y tipos
├── repository/     capa de repositorio para acceso a datos
├── ui/
│   ├── components/ componentes reutilizables
│   ├── navigation/ navegación raíz y destinos
│   ├── screen/     pantallas
│   └── theme/      tema Material 3
├── utils/          validaciones y notificaciones
├── viewmodel/      Login, Producto, Movimiento, Venta y Resumen
└── MainActivity.kt
```

## Cómo ejecutar el proyecto

**Requisitos:** Android Studio actualizado, conexión a internet para la primera sincronización de Gradle y un dispositivo o emulador con Android 11 (API 30) o superior.

1. Clona el repositorio:
   ```
   git clone https://github.com/MartinCC77/kaiju-inventario.git
   ```
2. Abre la carpeta en Android Studio (**File → Open**).
3. Espera a que termine el **Gradle Sync**. Si Android Studio solicita instalar algún SDK, acéptalo.
4. Conecta un celular con depuración USB activada o inicia un emulador.
5. Pulsa **Run** (▶).
6. Inicia sesión con `admin` / `kaiju123`.

**Para probar el escáner:** genera un código QR con el texto del código de un producto (por ejemplo `P001`) y escanéalo desde Productos o Ventas.

**Nota:** la base de datos usa migración destructiva (`fallbackToDestructiveMigration`), por lo que al cambiar la versión del esquema se borran los datos de prueba.

## Planificación

https://trello.com/invite/b/6add3a90d2d39b51422d26f7/ATTI29956f90b1204e577b84c5017c435087057433D7/kaiju-inventario-grupo-8

## Estado del proyecto

Avance intermedio (Evaluación Parcial 2). Será mejorado y ampliado para la Evaluación Final Transversal.
