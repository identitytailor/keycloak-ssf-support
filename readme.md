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

```
docker compose up
```

# Example Requests

You can find some [example requests](./requests) in the requests folder.
Note that you need to create an access token on the [caep.dev/](https://caep.dev/) website first to use their API.
