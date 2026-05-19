export async function sendMessage({ userId, sessionId, message, signal, onMessage }) {
  const response = await fetch('/agent-core/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, sessionId, message }),
    signal
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()

    for (const line of lines) {
      if (line.startsWith('data:')) {
        try {
          const data = JSON.parse(line.slice(5))
          onMessage(data)
          if (data.messageType === 'DONE') {
            return
          }
        } catch (err) {
          console.error('解析 SSE 数据失败:', err)
        }
      }
    }
  }
}
