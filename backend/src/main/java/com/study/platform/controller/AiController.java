package com.study.platform.controller;

import com.study.platform.dto.AiChatRequest;
import com.study.platform.service.AiContextService;
import com.study.platform.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    // AI 助教相关接口：普通对话与流式对话（SSE）
    private final AiService aiService;
    private final AiContextService aiContextService;
    // 每个流式请求都放在独立线程里向千问拉流，避免阻塞 Tomcat 工作线程
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AiController(AiService aiService, AiContextService aiContextService) {
        this.aiService = aiService;
        this.aiContextService = aiContextService;
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody AiChatRequest req, HttpServletRequest request) {
        // 调用前注入当前用户相关的平台数据作为上下文
        return aiService.chat(withContext(req.getMessages(), request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatRequest req, HttpServletRequest request) {
        // SseEmitter：SSE 流式返回，0 表示不设超时
        SseEmitter emitter = new SseEmitter(0L);
        List<Map<String, String>> messages = withContext(req.getMessages(), request);
        executor.execute(() -> {
            try {
                // 每收到一段增量内容就推给前端，前端逐字显示
                aiService.streamChat(messages, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("message")
                                .data(Map.of("content", chunk)));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private List<Map<String, String>> withContext(List<Map<String, String>> messages,
                                                  HttpServletRequest request) {
        // 从 JWT 过滤器写入的请求属性里取出当前用户身份
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        List<Map<String, String>> msgs = new ArrayList<>();
        // 系统消息放在最前面，告知模型平台数据并约束其基于真实数据回答
        msgs.add(Map.of("role", "system", "content",
                aiContextService.buildForUser(userId, role)));
        if (messages != null) {
            msgs.addAll(messages);
        }
        return msgs;
    }
}
