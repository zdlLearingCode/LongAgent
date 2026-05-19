# /chat/stream 接口文档

## 基本信息

| 项目 | 说明 |
|------|------|
| 接口路径 | `POST /agent-core/chat/stream` |
| 请求方式 | POST |
| Content-Type | `application/json` |
| 响应方式 | **SSE (Server-Sent Events)** 流式返回 |
| 超时时间 | 300 秒 |

---

## 请求参数

### Request Body (JSON)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | String | 是 | 用户唯一标识 |
| `sessionId` | String | 否 | 会话 ID。不传或为空时，服务端自动生成 UUID |
| `message` | String | 是 | 用户发送的消息内容 |

### 请求示例

```json
{
  "userId": "user_001",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "帮我总结一下这篇文章的要点"
}
```

---

## 响应说明

响应为 **SSE (Server-Sent Events)** 流，前端需使用 `EventSource` 或 `fetch` + `ReadableStream` 接收。

每个 SSE 事件的 `data` 字段为 JSON 对象，结构如下：

| 字段 | 类型 | 说明 |
|------|------|------|
| `sessionId` | String | 当前会话 ID（首次请求未传 sessionId 时，以此字段为准） |
| `messageType` | String | 消息类型，取值见下表 |
| `content` | String | 消息内容 |

### messageType 枚举

| 值 | 说明 |
|----|------|
| `TEXT` | 文本片段，`content` 为本次推送的文本增量内容 |
| `DONE` | 流结束标志，`content` 为空字符串，收到后应关闭连接 |

### 响应示例

SSE 原始数据流：

```
data:{"sessionId":"550e8400-e29b-41d4-a716-446655440000","messageType":"TEXT","content":"这篇文章"}

data:{"sessionId":"550e8400-e29b-41d4-a716-446655440000","messageType":"TEXT","content":"主要讲了"}

data:{"sessionId":"550e8400-e29b-41d4-a716-446655440000","messageType":"TEXT","content":"以下几个要点："}

data:{"sessionId":"550e8400-e29b-41d4-a716-446655440000","messageType":"DONE","content":""}
```

---

## 错误处理

SSE 连接异常时，连接会被服务端关闭。前端应监听 `onerror` 事件进行重连或提示。

常见错误场景：

| 场景 | 表现 |
|------|------|
| 模型调用失败 | SSE 连接异常关闭 |
| 工具调用超过上限（10 次） | SSE 连接异常关闭 |
| 超时（300 秒无响应） | SSE 连接超时关闭 |

---

## 前端对接示例

### 方式一：fetch + ReadableStream（推荐）

```javascript
async function chatStream(userId, message, sessionId) {
  const response = await fetch('/agent-core/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, sessionId, message })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop(); // 保留未完成的行

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const json = JSON.parse(line.slice(5));
        if (json.messageType === 'TEXT') {
          // 追加显示文本
          appendText(json.content);
        } else if (json.messageType === 'DONE') {
          // 流结束，sessionId 可用于后续对话
          console.log('会话完成, sessionId:', json.sessionId);
        }
      }
    }
  }
}
```

### 方式二：EventSource（仅支持 GET，需配合后端调整）

> 注意：原生 `EventSource` 仅支持 GET 请求，本接口为 POST，因此推荐使用方式一，
> 或使用第三方库如 `@microsoft/fetch-event-source`。

```javascript
import { fetchEventSource } from '@microsoft/fetch-event-source';

fetchEventSource('/agent-core/chat/stream', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    userId: 'user_001',
    message: '你好'
  }),
  onmessage(event) {
    const data = JSON.parse(event.data);
    if (data.messageType === 'TEXT') {
      appendText(data.content);
    } else if (data.messageType === 'DONE') {
      console.log('流结束');
    }
  },
  onerror(err) {
    console.error('SSE 连接异常:', err);
  }
});
```

---

## 注意事项

1. **sessionId 管理**：首次对话可不传 `sessionId`，从首条响应的 `sessionId` 字段获取，后续对话携带该值以保持上下文连续
2. **增量拼接**：`TEXT` 类型的 `content` 是增量文本片段，前端需自行拼接为完整回复
3. **连接关闭**：收到 `DONE` 消息后应主动关闭连接/停止读取
4. **历史上下文**：服务端自动加载最近 20 条历史消息作为上下文，无需前端传递
5. **超时**：单次请求最长 300 秒，超时后连接自动断开
