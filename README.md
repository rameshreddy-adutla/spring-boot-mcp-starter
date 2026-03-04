# Spring Boot MCP Starter

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build](https://github.com/rameshreddy-adutla/spring-boot-mcp-starter/actions/workflows/build.yml/badge.svg)](https://github.com/rameshreddy-adutla/spring-boot-mcp-starter/actions)

A lightweight Spring Boot starter for building [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) servers in Java. Define tools, resources, and prompts with annotations — no boilerplate.

## Why?

MCP is the open standard for connecting AI assistants to external tools and data. Most implementations are in Python or TypeScript. If you're a **Java/Spring Boot engineer**, there's no easy way to build an MCP server that fits into your existing stack.

This starter fixes that. Drop it into any Spring Boot app, annotate your methods, and you have a working MCP server.

## Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>dev.ramesh</groupId>
    <artifactId>spring-boot-mcp-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Define a tool

```java
@McpTool(name = "get_weather", description = "Get current weather for a city")
public class WeatherTool {

    @ToolMethod
    public String execute(@ToolParam(name = "city", description = "City name") String city) {
        // Your logic here
        return "Sunny, 22°C in " + city;
    }
}
```

### 3. Run

```bash
mvn spring-boot:run
```

Your Spring Boot app is now an MCP server. Connect it to Claude Desktop, GitHub Copilot, or any MCP client.

## Features

- **`@McpTool`** — Turn any Spring bean into an MCP tool
- **`@McpResource`** — Expose data sources (databases, APIs, files) as MCP resources
- **`@McpPrompt`** — Define reusable prompt templates
- **Stdio & HTTP transports** — Works with local CLI tools (stdio) and remote clients (SSE/HTTP)
- **Auto-discovery** — Tools, resources, and prompts are auto-registered via component scanning
- **Spring Boot native** — Constructor injection, `application.yml` config, actuator health checks
- **JSON Schema generation** — Tool parameters automatically generate JSON Schema from Java types

## Configuration

```yaml
mcp:
  server:
    name: my-mcp-server
    version: 1.0.0
    transport: stdio          # or 'sse' for HTTP
  sse:
    port: 8080
    endpoint: /mcp
```

## Architecture

```
┌─────────────────────────────────────────────┐
│  Spring Boot Application                     │
│                                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐    │
│  │ @McpTool │ │@McpResource│ │@McpPrompt│    │
│  └────┬─────┘ └────┬──────┘ └────┬─────┘    │
│       │             │             │           │
│  ┌────▼─────────────▼─────────────▼────┐     │
│  │        McpServerAutoConfiguration    │     │
│  │   (auto-discovers & registers all)   │     │
│  └────────────────┬────────────────────┘     │
│                   │                           │
│  ┌────────────────▼────────────────────┐     │
│  │         Transport Layer              │     │
│  │    ┌─────────┐  ┌──────────┐        │     │
│  │    │  Stdio  │  │ SSE/HTTP │        │     │
│  │    └─────────┘  └──────────┘        │     │
│  └─────────────────────────────────────┘     │
└─────────────────────────────────────────────┘
```

## Examples

### Database query tool

```java
@McpTool(name = "query_users", description = "Search users by name")
@RequiredArgsConstructor
public class UserQueryTool {

    private final UserRepository userRepository;

    @ToolMethod
    public List<User> execute(
            @ToolParam(name = "name", description = "Name to search for") String name,
            @ToolParam(name = "limit", description = "Max results", required = false) Integer limit) {
        return userRepository.findByNameContaining(name, PageRequest.of(0, limit != null ? limit : 10));
    }
}
```

### REST API resource

```java
@McpResource(
    uri = "api://orders/{orderId}",
    name = "order",
    description = "Fetch order details by ID"
)
@RequiredArgsConstructor
public class OrderResource {

    private final OrderService orderService;

    @ResourceMethod
    public Order fetch(@ResourceParam("orderId") String orderId) {
        return orderService.getById(orderId);
    }
}
```

### Prompt template

```java
@McpPrompt(name = "code_review", description = "Review code for common issues")
public class CodeReviewPrompt {

    @PromptMethod
    public String generate(
            @PromptParam(name = "language") String language,
            @PromptParam(name = "code") String code) {
        return """
            Review this %s code for bugs, security issues, and performance problems.
            Be specific and actionable. Only flag things that matter.

            ```%s
            %s
            ```
            """.formatted(language, language, code);
    }
}
```

## Transport Options

| Transport | Use Case | Config |
|-----------|----------|--------|
| **Stdio** | CLI tools, local AI assistants (Copilot, Claude Desktop) | `mcp.server.transport=stdio` |
| **SSE** | Remote clients, web-based integrations | `mcp.server.transport=sse` |

## Roadmap

- [ ] Auto-generate JSON Schema from records and POJOs
- [ ] Streaming responses for long-running tools
- [ ] OAuth2 authentication for SSE transport
- [ ] Spring Boot Actuator endpoint for MCP server health
- [ ] GraalVM native image support
- [ ] Publish to Maven Central

## Contributing

Contributions welcome. Open an issue first to discuss what you'd like to change.

## License

[Apache License 2.0](LICENSE)
