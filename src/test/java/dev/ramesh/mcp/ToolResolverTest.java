package dev.ramesh.mcp;

import dev.ramesh.mcp.annotation.McpTool;
import dev.ramesh.mcp.annotation.ToolMethod;
import dev.ramesh.mcp.annotation.ToolParam;
import dev.ramesh.mcp.tool.ToolDefinition;
import dev.ramesh.mcp.tool.ToolResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ToolResolverTest {

    @McpTool(name = "greet", description = "Greets a user by name")
    static class GreetTool {
        @ToolMethod
        public String execute(@ToolParam(name = "name", description = "User's name") String name) {
            return "Hello, " + name;
        }
    }

    @Test
    void shouldResolveToolDefinition() {
        GreetTool bean = new GreetTool();
        ToolDefinition def = ToolResolver.resolve(bean);

        assertEquals("greet", def.name());
        assertEquals("Greets a user by name", def.description());
        assertNotNull(def.inputSchema());
        assertEquals("object", def.inputSchema().type());
        assertEquals(1, def.inputSchema().properties().size());
        assertEquals("string", def.inputSchema().properties().get("name").type());
        assertEquals(1, def.inputSchema().required().size());
    }

    @McpTool(name = "add", description = "Adds two numbers")
    static class AddTool {
        @ToolMethod
        public int execute(
                @ToolParam(name = "a", description = "First number") int a,
                @ToolParam(name = "b", description = "Second number") int b) {
            return a + b;
        }
    }

    @Test
    void shouldHandleMultipleParams() {
        ToolDefinition def = ToolResolver.resolve(new AddTool());

        assertEquals("add", def.name());
        assertEquals(2, def.inputSchema().properties().size());
        assertEquals("integer", def.inputSchema().properties().get("a").type());
        assertEquals("integer", def.inputSchema().properties().get("b").type());
    }

    @McpTool(name = "search", description = "Search with optional limit")
    static class SearchTool {
        @ToolMethod
        public String execute(
                @ToolParam(name = "query", description = "Search query") String query,
                @ToolParam(name = "limit", description = "Max results", required = false) Integer limit) {
            return query + ":" + limit;
        }
    }

    @Test
    void shouldHandleOptionalParams() {
        ToolDefinition def = ToolResolver.resolve(new SearchTool());

        assertEquals(2, def.inputSchema().properties().size());
        assertEquals(1, def.inputSchema().required().size());
        assertEquals("query", def.inputSchema().required().get(0));
    }
}
