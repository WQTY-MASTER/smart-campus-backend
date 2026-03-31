package com.qfedu.smartcampusseckill.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent debug logger: appends NDJSON lines for runtime diagnosis.
 */
public final class AgentDebugLogger {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String LOG_PATH = "g:/代码1/idea代码/smart-campus-seckill/debug-96613d.log";

    private AgentDebugLogger() {
    }

    public static synchronized void log(String runId,
                                        String hypothesisId,
                                        String location,
                                        String message,
                                        Map<String, Object> data) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sessionId", "96613d");
            payload.put("runId", runId);
            payload.put("hypothesisId", hypothesisId);
            payload.put("location", location);
            payload.put("message", message);
            payload.put("data", data);
            payload.put("timestamp", System.currentTimeMillis());
            String line = MAPPER.writeValueAsString(payload) + System.lineSeparator();
            try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
                fw.write(line);
            }
        } catch (Exception ignored) {
            // Avoid breaking business flow if debug logging fails.
        }
    }
}

