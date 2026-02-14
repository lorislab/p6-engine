# P6 Workflow BPMN2 engine

[![License](https://img.shields.io/github/license/lorislab/p6-engine.svg)](https://github.com/lorislab/p6-engine/blob/main/LICENSE) 
[![GitHub Release](https://img.shields.io/github/v/release/lorislab/p6-engine?include_prereleases)](https://github.com/lorislab/p6-engine/releases)

---

## Overview

p6-engine is a BPMN workflow engine component developed by lorislab. It provides a REST API, OpenAPI definitions, 
PostgreSQL persistence (Flyway migrations), and code generation from OpenAPI. The project is run as a microservice 
that executes process definitions and manages process instances, jobs, and resources.

## Quick start

### Prerequisites

- Java 25 or later
- Maven 3.9+ (or your preferred Maven setup)
- PostgreSQL (for full end-to-end persistence features)

### Run (dev mode)

If you want quick feedback during development and your environment is set up for Quarkus, run:

```bash
mvn clean compile quarkus:dev
```

## Contributing

Contributions are welcome. Please follow the repository's contribution guidelines (if present) and ensure code is formatted and tested.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
