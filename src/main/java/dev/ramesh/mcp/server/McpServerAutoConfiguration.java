package dev.ramesh.mcp.server;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ramesh.mcp.annotation.McpTool;
import dev.ramesh.mcp.transport.StdioTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that discovers {@link McpTool}-annotated beans and
 * wires up the MCP server with the configured transport.
 */
@AutoConfiguration
@EnableConfigurationProperties(McpServerProperties.class)
public class McpServerAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpServerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper mcpObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpServer mcpServer(McpServerProperties properties,
                                ObjectMapper objectMapper,
                                List<Object> toolBeans) {
        McpServer server = new McpServer(properties, objectMapper);

        toolBeans.stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(McpTool.class))
                .forEach(bean -> {
                    server.registerTool(bean);
                    log.debug("Auto-discovered MCP tool: {}", bean.getClass().getSimpleName());
                });

        log.info("MCP server '{}' v{} started with {} tool(s)",
                properties.name(), properties.version(), server.getServerInfo().tools().size());

        return server;
    }

    @Bean
    @ConditionalOnProperty(name = "mcp.server.transport", havingValue = "stdio", matchIfMissing = true)
    public StdioTransport stdioTransport(McpServer server) {
        return new StdioTransport(server);
    }
}
