package com.dragon.agentCore.engine;

import com.dragon.agentCore.dto.ChatRequest;
import com.dragon.agentCore.dto.ChatStreamResponse;
import com.dragon.agentCore.entity.ConversationHistoryEntity;
import com.dragon.agentCore.service.ConversationService;
import com.dragon.agentCore.tools.WebFetchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * llm对话主循环
 *
 * @author dlzhang13
 * @create 2026/4/29 17:11
 */
@Slf4j
@Component
public class AgentLoopEngine {

    private final ChatClient chatClient;
    private final ConversationService conversationService;
    private final WebFetchTool webFetchTool;

    @Value("${agent.system-prompt:你是一个有用的AI助手。}")
    private String systemPrompt;

    private static final int MAX_TOOL_ITERATIONS = 10;

    public AgentLoopEngine(@Qualifier("openaiChatClient") ChatClient chatClient,
                           ConversationService conversationService,
                           WebFetchTool webFetchTool) {
        this.chatClient = chatClient;
        this.conversationService = conversationService;
        this.webFetchTool = webFetchTool;
    }

    public Flux<ChatStreamResponse> execute(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                ToolCallback[] toolCallbacks = ToolCallbacks.from(webFetchTool);

                List<Message> messages = new ArrayList<>();
                messages.add(new SystemMessage(systemPrompt));
                messages.addAll(loadHistory(request.getUserId(), request.getSessionId()));
                messages.add(new UserMessage(request.getMessage()));

                conversationService.saveMessage(
                        request.getUserId(), request.getSessionId(), "user", request.getMessage());

                ToolCallingChatOptions options = ToolCallingChatOptions.builder()
                        .toolCallbacks(toolCallbacks)
                        .internalToolExecutionEnabled(false)
                        .build();

                executeAgentLoop(sink, messages, options, toolCallbacks, request, 0);

            } catch (Exception e) {
                log.error("执行对话失败", e);
                sink.error(e);
            }
        });
    }

    private void executeAgentLoop(FluxSink<ChatStreamResponse> sink, List<Message> messages,
                                   ToolCallingChatOptions options, ToolCallback[] toolCallbacks,
                                   ChatRequest request, int iteration) {
        if (iteration >= MAX_TOOL_ITERATIONS) {
            sink.error(new RuntimeException("超过最大工具调用次数: " + MAX_TOOL_ITERATIONS));
            return;
        }

        StringBuilder fullContent = new StringBuilder();
        Map<String, AssistantMessage.ToolCall> toolCallsMap = new HashMap<>();

        chatClient.prompt(new Prompt(messages, options))
                .stream()
                .chatResponse()
                .doOnNext(chunk -> {
                    if (chunk.getResult() != null) {
                        AssistantMessage output = chunk.getResult().getOutput();

                        String text = output.getText();
                        if (text != null && !text.isEmpty()) {
                            sink.next(ChatStreamResponse.text(request.getSessionId(), text));
                            log.info("[TEXT]: {}", text);
                            fullContent.append(text);
                        }

                        for (AssistantMessage.ToolCall toolCall : output.getToolCalls()) {
                            toolCallsMap.put(toolCall.id(), toolCall);
                        }
                    }
                })
                .doOnError(error -> {
                    log.error("流式调用失败", error);
                    sink.error(error);
                })
                .doOnComplete(() -> {
                    try {
                        if (toolCallsMap.isEmpty()) {
                            String content = fullContent.toString();
                            if (!content.isEmpty()) {
                                conversationService.saveMessage(
                                        request.getUserId(), request.getSessionId(), "assistant", content);
                            }
                            sink.next(ChatStreamResponse.done(request.getSessionId()));
                            sink.complete();
                        } else {
                            log.info("模型请求调用 {} 个工具", toolCallsMap.size());

                            AssistantMessage assistantMessage = AssistantMessage.builder()
                                    .content(fullContent.toString())
                                    .toolCalls(new ArrayList<>(toolCallsMap.values()))
                                    .build();
                            messages.add(assistantMessage);

                            List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
                            for (AssistantMessage.ToolCall toolCall : toolCallsMap.values()) {
                                log.info("执行工具: {} 参数: {}", toolCall.name(), toolCall.arguments());
                                String result = executeToolCall(toolCall, toolCallbacks);
                                log.info("工具执行结果: {}", result.substring(0, Math.min(200, result.length())));
                                toolResponses.add(new ToolResponseMessage.ToolResponse(
                                        toolCall.id(), toolCall.name(), result));
                            }
                            messages.add(ToolResponseMessage.builder().responses(toolResponses).build());

                            executeAgentLoop(sink, messages, options, toolCallbacks, request, iteration + 1);
                        }
                    } catch (Exception e) {
                        log.error("处理工具调用失败", e);
                        sink.error(e);
                    }
                })
                .subscribe();
    }

    private String executeToolCall(AssistantMessage.ToolCall toolCall, ToolCallback[] toolCallbacks) {
        for (ToolCallback callback : toolCallbacks) {
            if (callback.getToolDefinition().name().equals(toolCall.name())) {
                return callback.call(toolCall.arguments());
            }
        }
        return "工具未找到: " + toolCall.name();
    }

    private List<Message> loadHistory(String userId, String sessionId) {
        List<Message> messages = new ArrayList<>();
        try {
            List<ConversationHistoryEntity> historyList =
                    conversationService.getHistory(userId, sessionId, 20);
            for (ConversationHistoryEntity history : historyList) {
                if ("user".equals(history.getRole())) {
                    messages.add(new UserMessage(history.getContent()));
                } else if ("assistant".equals(history.getRole())) {
                    messages.add(new AssistantMessage(history.getContent()));
                }
            }
        } catch (Exception e) {
            log.warn("加载历史消息失败，使用空历史", e);
        }
        return messages;
    }
}
