package dev.ramesh.mcp.transport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import dev.ramesh.mcp.server.McpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

/**
 * Stdio transport for MCP — reads JSON-RPC messages from stdin and writes responses to stdout.
 * This is the standard transport for local CLI tools like GitHub Copilot and Claude Desktop.
 */
public class StdioTransport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StdioTransport.class);

    private final McpServer server;

    public StdioTransport(McpServer server) {
        this.server = server;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("MCP stdio transport listening...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String response = server.handleRequest(line);
                if (response != null) {
                    writer.println(response);
                }
            }
        }
    }
}
