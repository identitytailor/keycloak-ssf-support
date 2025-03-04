Shared Signals Framework for Keycloak
---

This repository provides PoC support for the Shared Signals Framework as a Keycloak extension
for Keycloak 26.1.3.

Currently, the following features are supported:
- Management of shared signals receivers
- Event delivery with SET POLL and SET PUSH
- Pluggable security event listeners (Security Event Listener SPI)
- Pluggable Shared Signal Framework Component (Shared Signals SPI)
- Support for using managed and external streams

# Build

```
mvn clean verify
```

# Run

After running the following command, Keycloak will be available on:
http://localhost:18080/auth/admin

Username: `admin`
Password: `admin`

```
docker compose up
```

# Example Requests

You can find some [example requests](./requests) to register a SSF receiver with a managed stream in the requests folder.
Note that you need to create an access token on the [caep.dev/](https://caep.dev/) website first to use their API.

The example requests use the `ssf-demo` realm imported during Keycloak startup.

For demo purposes, you can login as the `tester` user with password `test`
via the accounts console: http://localhost:18080/auth/realms/ssf-demo/account/