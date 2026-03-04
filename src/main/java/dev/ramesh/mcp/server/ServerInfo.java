package dev.ramesh.mcp.server;

import java.util.List;

import dev.ramesh.mcp.tool.ToolDefinition;

/**
 * Immutable representation of MCP server metadata and capabilities.
 */
public record ServerInfo(
        String name,
        String version,
        List<ToolDefinition> tools
) {
    public ServerInfo {
        tools = List.copyOf(tools);
    }
}
