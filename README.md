# L13 Monitor — aplicacion-mobil

App Android nativa (Jetpack Compose, MVI) para **monitorear y comandar** el
sistema L13 Brain desplegado en Heroku (`intense-reef-08270`).

## Stack

- **Kotlin 2.0** + **Jetpack Compose** (BOM 2024.09.00)
- **MVI**: cada pantalla expone `State`, `Event` (intents) y `Effect`; reducers puros
- **Hilt** (DI) · **Retrofit/OkHttp** · **Room** (offline-first) · **WorkManager** (polling/battery-safe) · **Vico** (gráficos) · **Navigation Compose**

## Arquitectura

```text
ui/          Screens + ViewModels MVI (State/Event/Effect)
core/
  commands/  CommandBus FIFO con retry exponencial + ACK tracking
  transport/ Transport (RestTransport hoy, WebSocketTransport stub v2)
data/
  remote/    L13ApiService (/health, /infer), HerokuApiService, DTOs
  local/     Room: CycleEntity, CommandLogEntity, HealthSnapshot
  repository/ L13, Heroku, Credentials (EncryptedSharedPreferences)
domain/      model + usecases (GetHealth, SendCommand, StreamCycles, MonitorDyno)
workers/     HealthCheckWorker, CyclePollerWorker
di/          AppModule (OkHttp con pinning a herokuapp.com en release)
```

## Abrir en Android Studio

1. Abrir la carpeta raíz (`aplicacion-mobil/`), no `app/`.
2. Android Studio Hedgehog+ sincroniza automáticamente (Gradle 8.9, JDK 17).
3. Configurar permisos en **Settings** de la app:
   - Bearer token L13 (se guarda en EncryptedSharedPreferences)
   - URL base (por defecto `https://intense-reef-08270-20c09ed2b660.herokuapp.com`)

## Seguridad

- **Nunca** se versionan tokens: viven en EncryptedSharedPreferences.
- Certificate pinning a `*.herokuapp.com` solo en builds `release`; debug queda libre para emuladores/proxies.
- Los comandos (`L13Command`) llevan `cmd_id` UUID para idempotencia y ACK.

## Módulos

| Módulo | Descripción |
|---|---|
| `:app` | Aplicación única `com.ell1ot.l13monitor` |

## Estado post-auditoría técnica (rama fix/v3-fase-build)

Correcciones aplicadas sobre `decfd54`:

1. **Matemáticas (fix/math):** Jacobi corregido en `PersistentHypergraphCalculator`;
   espectro calculado desde el Laplaciano real (sin evals forzados a 0); barcodes/Betti
   ya no muestran literales inventados — cuando no hay cómputo real la UI dice `NA`;
   el diálogo de "Calculadora Espectral" ahora calcula el espectro del Laplaciano del
   hipergrafo vivo y el REPL es una calculadora aritmética honesta (sin corrupción de
   notación científica ni resultados fabricados). Osciloscopio HGS-9500 etiquetado
   como señal **SINTÉTICA (LAB)**.
2. **Runtime/datos (fix/data):** `CommandBus` es FIFO real (Channel + pump secuencial);
   `HolographicVsa` reconstruye `superPosition` al expulsar vectores (olvido correcto);
   Room: `onUpgrade` sin DROP y sin `fallbackToDestructiveMigration`; clientes LEGACY
   (`L13ApiClient`) apuntan a la URL Heroku real y reportan modo LAB cuando el fallo;
   credenciales personales hardcodeadas removidas.
3. **Infra (fix/infrastructure):** rutas HISTORY/SETTINGS montadas en NavGraph (antes
   inalcanzables); interceptor OkHttp reescribe host/esquema por request según
   `CredentialsRepository` (URL base ya no queda congelada) y solo adjunta el Bearer
   token al host L13 configurado.
4. **Build (build/fix):** `gradlew` + wrapper 8.9 añadidos; heap del daemon ajustado
   a entornos chicos (`-Xmx2048m`). `assembleDebug` genera `app-debug.apk`
   (verificado en linux-aarch64 con aapt2 bajo qemu-user: ver abajo).

### Build en hosts ARM64 Linux

`aapt2` se distribuye sólo para linux-x86_64; en arm64 (p. ej. AWS Graviton) instalar
qemu-user y registrar binfmt para x86_64 con una sysroot glibc mínima en
`QEMU_LD_PREFIX`. En x86_64 o con Android Studio no hace falta nada.
