package dev.ramesh.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ramesh.mcp.annotation.McpTool;
import dev.ramesh.mcp.annotation.ToolMethod;
import dev.ramesh.mcp.annotation.ToolParam;
import dev.ramesh.mcp.server.McpServer;
import dev.ramesh.mcp.server.McpServerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpServerTest {

    private McpServer server;

    @McpTool(name = "echo", description = "Echoes input back")
    static class EchoTool {
        @ToolMethod
        public String execute(@ToolParam(name = "message", description = "Message to echo") String message) {
            return message;
        }
    }

    @BeforeEach
    void setUp() {
        var props = new McpServerProperties("test-server", "0.1.0", "stdio");
        server = new McpServer(props, new ObjectMapper());
        server.registerTool(new EchoTool());
    }

    @Test
    void shouldHandleInitialize() {
        String request = """
                {"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
                """;

        String response = server.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.contains("test-server"));
        assertTrue(response.contains("protocolVersion"));
    }

    @Test
    void shouldHandleToolsList() {
        String request = """
                {"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
                """;

        String response = server.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.contains("echo"));
        assertTrue(response.contains("Echoes input back"));
    }

    @Test
    void shouldHandleToolCall() {
        String request = """
                {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"echo","arguments":{"message":"hello"}}}
                """;

        String response = server.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.contains("hello"));
    }

    @Test
    void shouldReturnErrorForUnknownTool() {
        String request = """
                {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"nonexistent","arguments":{}}}
                """;

        String response = server.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.contains("Unknown tool"));
    }

    @Test
    void shouldReturnErrorForUnknownMethod() {
        String request = """
                {"jsonrpc":"2.0","id":5,"method":"foo/bar","params":{}}
                """;

        String response = server.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.contains("Method not found"));
    }
}
