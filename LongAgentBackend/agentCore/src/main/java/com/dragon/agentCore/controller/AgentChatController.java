package com.dragon.agentCore.controller;

import com.dragon.agentCore.dto.ChatRequest;
import com.dragon.agentCore.dto.ChatStreamResponse;
import com.dragon.agentCore.engine.AgentLoopEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 智能体对话主入口
 *
 * @author dlzhang13
 * @create 2026/4/29 17:08
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentLoopEngine agentLoopEngine;

    @PostMapping(value = "/stream")
    public SseEmitter chat(@RequestBody ChatRequest request) {
        if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString());
        }

        SseEmitter emitter = new SseEmitter(300_000L);

        agentLoopEngine.execute(request)
                .doOnNext(response -> {
                    try {
                        emitter.send(SseEmitter.event().data(response));
                    } catch (Exception e) {
                        log.error("SSE发送失败", e);
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(emitter::completeWithError)
                .doOnComplete(emitter::complete)
                .subscribe();

        return emitter;
    }
}
