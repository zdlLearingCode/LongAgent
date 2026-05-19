import { useState, useRef } from 'react'
import MessageList from './MessageList'
import MessageInput from './MessageInput'
import { sendMessage } from '../services/chatService'
import './ChatInterface.css'

function ChatInterface() {
  const [messages, setMessages] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [sessionId, setSessionId] = useState(null)
  const [userId] = useState('user_' + Date.now())
  const abortControllerRef = useRef(null)

  const handleSendMessage = async (content) => {
    if (!content.trim() || isLoading) return

    const userMsg = { id: Date.now(), role: 'user', content: content.trim() }
    const aiMsg = { id: Date.now() + 1, role: 'assistant', content: '' }

    setMessages(prev => [...prev, userMsg, aiMsg])
    setIsLoading(true)
    abortControllerRef.current = new AbortController()

    try {
      await sendMessage({
        userId,
        sessionId,
        message: content.trim(),
        signal: abortControllerRef.current.signal,
        onMessage: (data) => {
          if (data.sessionId && !sessionId) setSessionId(data.sessionId)
          if (data.messageType === 'TEXT') {
            setMessages(prev => {
              const updated = [...prev]
              const last = updated[updated.length - 1]
              updated[updated.length - 1] = {
                ...last,
                content: last.content + data.content
              }
              return updated
            })
          }
        }
      })
    } catch (err) {
      if (err.name !== 'AbortError') {
        setMessages(prev => {
          const updated = [...prev]
          const last = updated[updated.length - 1]
          updated[updated.length - 1] = {
            ...last,
            content: '连接出现问题，请稍后重试。',
            error: true
          }
          return updated
        })
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleStop = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
      setIsLoading(false)
    }
  }

  const handleNewChat = () => {
    if (abortControllerRef.current) abortControllerRef.current.abort()
    setMessages([])
    setSessionId(null)
    setIsLoading(false)
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <div className="chat-header-left">
          <div className="chat-logo">
            <div className="chat-logo-inner" />
          </div>
          <div className="chat-header-info">
            <h1 className="chat-title">Aether</h1>
            <span className="chat-subtitle">
              {sessionId ? `Session · ${sessionId.slice(0, 8)}` : 'New conversation'}
            </span>
          </div>
        </div>
        <button className="new-chat-btn" onClick={handleNewChat}>
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 1v14M1 8h14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <span>New Chat</span>
        </button>
      </div>
      <MessageList messages={messages} isLoading={isLoading} />
      <MessageInput onSend={handleSendMessage} onStop={handleStop} isLoading={isLoading} />
    </div>
  )
}

export default ChatInterface
