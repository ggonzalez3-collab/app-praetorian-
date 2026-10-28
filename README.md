# Praetorian · App Android

App móvil del gimnasio **Praetorian**, en Kotlin con Jetpack Compose (Material 3).
Tiene dos modos según quién inicia sesión:

| Socio | Staff |
|---|---|
| Ver su membresía (plan, vigencia, días restantes) | Panel: socios activos/vencidos, ingresos del día, facturación mensual |
| Registrar su ingreso al gimnasio | Ver las clases de hoy y las membresías que vencen esta semana |
| Reservar o cancelar clases (con cupos y límite según plan) | Buscar socios, registrar ingresos y renovar membresías |
| Anotar entrenamientos y ver sus récords personales | Agregar socios nuevos y ver la ocupación de cada clase |

## Cómo abrirla

1. Instala [Android Studio](https://developer.android.com/studio) (Ladybug o más nuevo).
2. **File → Open** y elige la carpeta `praetorian-app`.
3. Espera a que Gradle sincronice (la primera vez descarga dependencias).
4. Crea un emulador en **Device Manager** (o conecta tu celular con depuración USB) y pulsa ▶ **Run**.

### Cuentas de prueba (contraseña `1234`)

- Socio: `socio@praetorian.cl`
- Staff: `staff@praetorian.cl`

## Cómo está organizada

```
app/src/main/java/cl/praetorian/app/
├── MainActivity.kt          # Entrada: decide si mostrar login, socio o staff
├── data/
│   ├── Models.kt            # Member, Plan, GymClass, Workout, Session
│   └── GymRepository.kt     # Reglas del negocio + datos de ejemplo (en memoria)
└── ui/
    ├── GymViewModel.kt      # Puente entre pantallas y repositorio
    ├── Format.kt            # Pesos chilenos, fechas y días en español
    ├── theme/Theme.kt       # Colores de la marca (negro, dorado, carmesí)
    ├── components/          # Piezas reutilizables (barra, tarjetas, badges)
    ├── login/               # Pantalla de inicio de sesión
    ├── member/              # Inicio, Clases y Progreso del socio
    └── staff/               # Panel, Socios y Clases del staff
```

Los planes y precios están en `data/Models.kt` (`Plan`) y los datos de ejemplo en `data/GymRepository.kt`.

## Próximos pasos sugeridos

1. **Guardar datos de verdad**: Room (en el teléfono) o Firebase Firestore (en la nube, compartido entre socios y staff).
2. **Login real**: Firebase Authentication.
3. **Ingreso con QR**: el socio muestra un QR y el staff lo escanea (ML Kit / ZXing).
4. **Pagos**: Webpay o Mercado Pago para renovar desde la app.
5. **Notificaciones**: avisar cuando la membresía está por vencer (Firebase Cloud Messaging).
