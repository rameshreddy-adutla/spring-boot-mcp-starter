package dev.ramesh.mcp.tool;

import java.util.List;
import java.util.Map;

/**
 * Describes an MCP tool's metadata and input schema.
 */
public record ToolDefinition(
        String name,
        String description,
        InputSchema inputSchema
) {
    public record InputSchema(
            String type,
            Map<String, PropertySchema> properties,
            List<String> required
    ) {
    }

    public record PropertySchema(
            String type,
            String description
    ) {
    }
}
