package dev.ramesh.mcp.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MCP server, bound to {@code mcp.server.*} in application.yml.
 */
@ConfigurationProperties(prefix = "mcp.server")
public record McpServerProperties(
        String name,
        String version,
        String transport
) {
    public McpServerProperties {
        if (name == null || name.isBlank()) name = "spring-boot-mcp-server";
        if (version == null || version.isBlank()) version = "0.1.0";
        if (transport == null || transport.isBlank()) transport = "stdio";
    }
}
