import { useRef, useEffect, memo } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { oneDark } from 'react-syntax-highlighter/dist/esm/styles/prism'
import './MessageList.css'

const MarkdownContent = memo(({ content }) => (
  <ReactMarkdown
    remarkPlugins={[remarkGfm]}
    components={{
      code({ node, inline, className, children, ...props }) {
        const match = /language-(\w+)/.exec(className || '')
        if (!inline && match) {
          return (
            <div className="code-block-wrapper">
              <div className="code-block-header">
                <span className="code-lang">{match[1]}</span>
                <button
                  className="copy-btn"
                  onClick={() => navigator.clipboard.writeText(String(children))}
                >
                  Copy
                </button>
              </div>
              <SyntaxHighlighter
                style={oneDark}
                language={match[1]}
                PreTag="div"
                customStyle={{
                  margin: 0,
                  borderRadius: '0 0 10px 10px',
                  fontSize: '13px',
                  padding: '16px',
                  background: '#1a1b26',
                }}
                {...props}
              >
                {String(children).replace(/\n$/, '')}
              </SyntaxHighlighter>
            </div>
          )
        }
        return (
          <code className="inline-code" {...props}>{children}</code>
        )
      },
      table({ children }) {
        return <div className="table-wrapper"><table>{children}</table></div>
      },
      a({ href, children }) {
        return <a href={href} target="_blank" rel="noopener noreferrer">{children}</a>
      },
      blockquote({ children }) {
        return <blockquote className="md-blockquote">{children}</blockquote>
      }
    }}
  >
    {content}
  </ReactMarkdown>
))

const TypingIndicator = () => (
  <div className="typing-indicator">
    <span /><span /><span />
  </div>
)

function MessageList({ messages, isLoading }) {
  const listRef = useRef(null)
  const prevCountRef = useRef(0)

  useEffect(() => {
    const list = listRef.current
    if (!list) return

    const isNewMessage = messages.length !== prevCountRef.current
    prevCountRef.current = messages.length

    if (isNewMessage) {
      list.scrollTop = list.scrollHeight
    } else {
      const isNearBottom = list.scrollHeight - list.scrollTop - list.clientHeight < 100
      if (isNearBottom) {
        list.scrollTop = list.scrollHeight
      }
    }
  }, [messages])

  return (
    <div className="message-list" ref={listRef}>
      {messages.length === 0 ? (
        <div className="empty-state">
          <div className="empty-orb" />
          <h2 className="empty-title">What's on your mind?</h2>
          <p className="empty-desc">Start a conversation with Aether</p>
        </div>
      ) : (
        <div className="messages">
          {messages.map((msg) => (
            <div key={msg.id} className={`message ${msg.role} msg-enter`}>
              {msg.role === 'assistant' && (
                <div className="message-avatar">
                  <div className="avatar-dot" />
                </div>
              )}
              <div className="message-body">
                <div className={`message-bubble ${msg.error ? 'error' : ''}`}>
                  {msg.role === 'assistant' ? (
                    msg.content ? (
                      <MarkdownContent content={msg.content} />
                    ) : isLoading ? (
                      <TypingIndicator />
                    ) : null
                  ) : (
                    <span>{msg.content}</span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default MessageList
