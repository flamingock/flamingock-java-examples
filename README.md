![Header Image](misc/logo-with-text.png)
___

**Flamingock** brings *Change-as-Code (CaC)* to your entire stack.  
It applies **versioned, auditable changes** to the external systems your application depends on — such as schemas, message brokers, databases, APIs, cloud services, and any other external system your application needs.

Unlike infrastructure-as-code tools, Flamingock runs **inside your application** (or via the **CLI**).  
It ensures these systems evolve **safely, consistently, and in sync with your code at runtime**.


### What Flamingock manages
Flamingock focuses on **application-level changes** that your code requires to run safely:

- Database schemas and reference data
- Message queues and schemas
- APIs and configuration values
- Cloud service resources directly tied to your application
- Configuration changes (feature flags, secrets, runtime values)

### What Flamingock does *not* manage
Flamingock is **not an infrastructure-as-code tool**. It does not provision servers, clusters, or networks — those belong in Terraform, Pulumi, or similar. Instead, Flamingock **complements them by handling the runtime changes your application depends on**.

> For more information about Flamingock for Java library, please visit the main repository **[here](https://github.com/flamingock/flamingock-java)**.
> 
---

## Examples Overview

This repository is structured as **Individual Gradle Projects**, with each project demonstrating Flamingock's
integration with different frameworks, technologies, and use cases. Explore the examples to find the one that matches
your needs!

Each example is prepared to run from its own test with all infrastructure (databases, mocked servers, etc.) needed.
But you can also  run it with your own infrastructure.

---

## Index of Examples

| **Example Project**                                            | **Description**                                                                                                                                           |
|----------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
> 🚀 **New examples will be added regularly!** Stay tuned for updates as we expand the repository to cover even more
> systems and frameworks.

---

## How to Run Examples

**1. Clone this repository:**
```shell
   git clone https://github.com/flamingock/flamingock-java-examples.git
   cd flamingock-java-examples
```

**2. Navigate to the example you want to explore:**
```shell
cd inventory-orders-service
```

**3. Run example**

***3.a. Run the project test using Gradle***
```shell
./gradlew test
```

***3.b. Run the project using Gradle and your own infrastructure***
```shell
./gradlew run
```

**4. Follow the instructions in the specific project's README for further details.**

___

## Contributing
We welcome contributions! If you have an idea for a new example or improvement to an existing one, feel free to submit a
pull request. Check out our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

___

## Get Involved
⭐ Star the [Flamingock repository](https://github.com/flamingock/flamingock-java) to show your support!

🐞 Report issues or suggest features in the [Flamingock issue tracker](https://github.com/flamingock/flamingock-java/issues).

💬 Join the discussion in the [Flamingock community](https://github.com/flamingock/flamingock-java/discussions).

___

## License
This repository is licensed under the [Apache License 2.0](LICENSE.md).

___

## Explore, experiment, and empower your projects with Flamingock!
Let us know what you think or where you’d like to see Flamingock used next.
