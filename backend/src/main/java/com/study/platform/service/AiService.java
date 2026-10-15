package com.study.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class AiService {

    // 通义千问（DashScope OpenAI 兼容模式）相关配置
    @Value("${qwen.api-key}")
    private String apiKey;

    @Value("${qwen.model}")
    private String model;

    @Value("${qwen.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> chat(List<Map<String, String>> messages) {
        // 非流式调用：一次性返回完整回答
        Map<String, Object> result = new HashMap<>();
        if (apiKey == null || apiKey.isBlank()) {
            return demo("尚未配置通义千问 API Key。");
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages == null || messages.isEmpty()
                    ? List.of(Map.of("role", "user", "content", "你好"))
                    : messages);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/chat/completions", entity, Map.class);
            Map<String, Object> respBody = resp.getBody();
            Object choices = respBody.get("choices");
            String content = "（模型无返回）";
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Map<?, ?> first = (Map<?, ?>) ((List<?>) choices).get(0);
                Map<?, ?> message = (Map<?, ?>) first.get("message");
                if (message != null && message.get("content") != null) {
                    content = String.valueOf(message.get("content"));
                }
            }
            result.put("demo", false);
            result.put("content", content);
            return result;
        }
        catch (Exception e) {
            return demo("调用通义千问失败：" + e.getMessage());
        }
    }

    public void streamChat(List<Map<String, String>> messages, Consumer<String> onChunk) {
        // 流式调用：直接读取千问的 SSE 响应，把每个增量 token 交给回调
        if (apiKey == null || apiKey.isBlank()) {
            onChunk.accept("（演示模式）尚未配置通义千问 API Key。");
            return;
        }
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + "/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages == null || messages.isEmpty()
                    ? List.of(Map.of("role", "user", "content", "你好"))
                    : messages);
            body.put("stream", true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(body));
            }

            int code = conn.getResponseCode();
            if (code >= 400) {
                // 出错时把错误信息作为一条消息返回给前端
                InputStream err = conn.getErrorStream();
                String msg = err == null ? ("HTTP " + code) : new String(err.readAllBytes(), StandardCharsets.UTF_8);
                onChunk.accept("调用失败：" + msg);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) continue;
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) continue;
                    try {
                        // 解析 OpenAI 兼容的流式 JSON：choices[0].delta.content
                        JsonNode node = objectMapper.readTree(data);
                        JsonNode choices = node.path("choices");
                        if (choices.isArray() && choices.size() > 0) {
                            String delta = choices.get(0).path("delta").path("content").asText("");
                            if (!delta.isEmpty()) onChunk.accept(delta);
                        }
                    } catch (Exception ignore) {
                        // skip non-JSON keep-alive lines
                    }
                }
            }
        } catch (Exception e) {
            onChunk.accept("调用失败：" + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private Map<String, Object> demo(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("demo", true);
        result.put("content", "（演示模式）" + message);
        return result;
    }
}
