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
