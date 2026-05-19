# AI 会话界面

基于 React + Vite 构建的 AI 会话界面，支持流式响应。

## 功能特性

- ✨ 流式响应显示，实时展示 AI 回复
- 💬 支持多轮对话，自动维护会话上下文
- 🎨 现代化 UI 设计，渐变色主题
- ⌨️ 支持 Enter 发送，Shift+Enter 换行
- 🛑 支持中断正在进行的请求
- 🔄 一键开启新会话

## 技术栈

- React 18
- Vite 5
- 原生 Fetch API (SSE 流式处理)

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## API 配置

接口地址配置在 `vite.config.js` 中：

```javascript
server: {
  proxy: {
    '/agent-core': {
      target: 'http://localhost:8080',  // 修改为你的后端地址
      changeOrigin: true
    }
  }
}
```

## 项目结构

```
src/
├── components/
│   ├── ChatInterface.jsx      # 主聊天容器
│   ├── ChatInterface.css
│   ├── MessageList.jsx         # 消息列表
│   ├── MessageList.css
│   ├── MessageInput.jsx        # 输入框
│   └── MessageInput.css
├── services/
│   └── chatService.js          # API 服务
├── App.jsx
├── App.css
├── main.jsx
└── index.css
```

## API 接口说明

详见 `D:\C-Learning\LongAgent\docs\chat-stream-api.md`

- 接口路径: `POST /agent-core/chat/stream`
- 响应方式: SSE (Server-Sent Events) 流式返回
- 自动管理 sessionId 和会话上下文
