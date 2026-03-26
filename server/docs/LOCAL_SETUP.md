# Local Development Setup

## Prerequisites

- `podman`
- `podman-compose` (e.g., `brew install podman-compose`)

or

- `docker`
- `docker-compose` (e.g., `brew install docker-compose`)


## Setup

1.  Copy the environment template:
    ```bash
    cp .env.example .env
    ```
2.  Review and change if needed the secrets in `.env` (keys, passwords, etc.).
3.  By default the application is configured to use default user and all APIs are directly accessible (`AUTH_ENABLED=false`)

## Usage

There are two run configurations, managed by `COMPOSE_PROFILES` and `FEATURE_GATE_REDIS_ENABLED` in the `.env` file.

### Scenario A: Default (No Redis)

This is the default mode, specified in `.env`. It runs the stack using local caching.

# Uses default FEATURE_GATE_REDIS_ENABLED=false
# Uses default COMPOSE_PROFILES=

```bash
podman-compose up --build
```

### Scenario B: With Redis
This mode enables the redis service.

#### Option 1 (Recommended): Edit .env

1. Set the following in your .env file:
```bash
FEATURE_GATE_REDIS_ENABLED=true
COMPOSE_PROFILES=redis
```

2. Run:

```bash
podman-compose up --build
```

#### Option 2: Command-line Override

Override the .env variables and activate the profile at runtime.


```bash
FEATURE_GATE_REDIS_ENABLED=true podman-compose --profile redis up --build
```

### Teardown
To stop all services and destroy the database volume:

```bash
podman-compose down -v
```