package dev.ramesh.mcp.server;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.ramesh.mcp.annotation.McpTool;
import dev.ramesh.mcp.annotation.ToolParam;
import dev.ramesh.mcp.tool.ToolDefinition;
import dev.ramesh.mcp.tool.ToolResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Core MCP server that handles JSON-RPC messages, discovers tools from Spring context,
 * and dispatches tool invocations.
 */
public class McpServer {

    private static final Logger log = LoggerFactory.getLogger(McpServer.class);
    private static final String JSONRPC_VERSION = "2.0";
    private static final String PROTOCOL_VERSION = "2025-03-26";

    private final McpServerProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> toolBeans = new LinkedHashMap<>();
    private final List<ToolDefinition> toolDefinitions = new ArrayList<>();

    public McpServer(McpServerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void registerTool(Object bean) {
        ToolDefinition def = ToolResolver.resolve(bean);
        toolBeans.put(def.name(), bean);
        toolDefinitions.add(def);
        log.info("Registered MCP tool: {}", def.name());
    }

    public ServerInfo getServerInfo() {
        return new ServerInfo(properties.name(), properties.version(), toolDefinitions);
    }

    /**
     * Handles a raw JSON-RPC request string and returns a JSON-RPC response string.
     */
    public String handleRequest(String requestJson) {
        try {
            JsonNode request = objectMapper.readTree(requestJson);
            String method = request.path("method").asText();
            JsonNode id = request.path("id");

            return switch (method) {
                case "initialize" -> handleInitialize(id);
                case "tools/list" -> handleToolsList(id);
                case "tools/call" -> handleToolsCall(id, request.path("params"));
                case "notifications/initialized" -> null; // no response for notifications
                default -> errorResponse(id, -32601, "Method not found: " + method);
            };
        } catch (Exception e) {
            log.error("Error handling MCP request", e);
            return errorResponse(null, -32700, "Parse error: " + e.getMessage());
        }
    }

    private String handleInitialize(JsonNode id) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", PROTOCOL_VERSION);

        ObjectNode capabilities = objectMapper.createObjectNode();
        capabilities.putObject("tools");
        result.set("capabilities", capabilities);

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", properties.name());
        serverInfo.put("version", properties.version());
        result.set("serverInfo", serverInfo);

        return jsonRpcResponse(id, result);
    }

    private String handleToolsList(JsonNode id) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = result.putArray("tools");

        for (ToolDefinition def : toolDefinitions) {
            ObjectNode tool = objectMapper.createObjectNode();
            tool.put("name", def.name());
            tool.put("description", def.description());
            tool.set("inputSchema", objectMapper.valueToTree(def.inputSchema()));
            tools.add(tool);
        }

        return jsonRpcResponse(id, result);
    }

    private String handleToolsCall(JsonNode id, JsonNode params) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        Object bean = toolBeans.get(toolName);
        if (bean == null) {
            return errorResponse(id, -32602, "Unknown tool: " + toolName);
        }

        try {
            Method method = ToolResolver.findToolMethod(bean.getClass());
            method.setAccessible(true);
            Object[] args = resolveArguments(method, arguments);
            Object result = method.invoke(bean, args);

            ObjectNode response = objectMapper.createObjectNode();
            ArrayNode content = response.putArray("content");
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", objectMapper.writeValueAsString(result));
            content.add(textContent);

            return jsonRpcResponse(id, response);
        } catch (Exception e) {
            log.error("Error invoking tool: {}", toolName, e);
            return errorResponse(id, -32603, "Tool execution failed: " + e.getMessage());
        }
    }

    private Object[] resolveArguments(Method method, JsonNode arguments) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            ToolParam tp = params[i].getAnnotation(ToolParam.class);
            if (tp != null && arguments.has(tp.name())) {
                args[i] = objectMapper.convertValue(arguments.get(tp.name()), params[i].getType());
            }
        }
        return args;
    }

    private String jsonRpcResponse(JsonNode id, JsonNode result) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", JSONRPC_VERSION);
            response.set("id", id);
            response.set("result", result);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    private String errorResponse(JsonNode id, int code, String message) {
        try {
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", JSONRPC_VERSION);
            response.set("id", id);
            ObjectNode error = response.putObject("error");
            error.put("code", code);
            error.put("message", message);
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize error response", e);
        }
    }
}
