package dev.ramesh.mcp.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.ramesh.mcp.annotation.McpTool;
import dev.ramesh.mcp.annotation.ToolMethod;
import dev.ramesh.mcp.annotation.ToolParam;
import dev.ramesh.mcp.tool.ToolDefinition.InputSchema;
import dev.ramesh.mcp.tool.ToolDefinition.PropertySchema;

/**
 * Resolves {@link ToolDefinition} metadata from a bean annotated with {@link McpTool}.
 */
public final class ToolResolver {

    private ToolResolver() {
    }

    public static ToolDefinition resolve(Object bean) {
        Class<?> clazz = bean.getClass();
        McpTool annotation = clazz.getAnnotation(McpTool.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Bean " + clazz.getName() + " is not annotated with @McpTool");
        }

        Method toolMethod = findToolMethod(clazz);
        InputSchema schema = buildInputSchema(toolMethod);

        return new ToolDefinition(annotation.name(), annotation.description(), schema);
    }

    public static Method findToolMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ToolMethod.class)) {
                return method;
            }
        }
        throw new IllegalStateException("No @ToolMethod found on " + clazz.getName());
    }

    private static InputSchema buildInputSchema(Method method) {
        Map<String, PropertySchema> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Parameter param : method.getParameters()) {
            ToolParam tp = param.getAnnotation(ToolParam.class);
            if (tp != null) {
                properties.put(tp.name(), new PropertySchema(mapJavaType(param.getType()), tp.description()));
                if (tp.required()) {
                    required.add(tp.name());
                }
            }
        }
        return new InputSchema("object", properties, required);
    }

    private static String mapJavaType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class || type == float.class || type == Float.class) return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        return "object";
    }
}
