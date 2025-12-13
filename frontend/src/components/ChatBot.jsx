import React, { useState, useEffect, useRef } from 'react';
import './ChatBot.css';
import { sendChatMessage, getChatSession } from '../api/documentApi';

export default function ChatBot({ sessionId, documentName, onUploadNew }) {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const messagesEndRef = useRef(null);

  // Load chat history on mount
  useEffect(() => {
    loadChatHistory();
  }, [sessionId]);

  // Scroll to bottom when messages change
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadChatHistory = async () => {
    try {
      const response = await getChatSession(sessionId);
      setMessages(response.data.messages || []);
    } catch (err) {
      console.error('Error loading chat history:', err);
      setError('Failed to load chat history');
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();

    if (!inputValue.trim()) return;

    const userMessage = inputValue.trim();
    setInputValue('');
    setError(null);

    // Add user message to UI
    const newUserMessage = {
      role: 'user',
      content: userMessage,
    };
    setMessages((prev) => [...prev, newUserMessage]);

    setLoading(true);

    try {
      const response = await sendChatMessage(sessionId, userMessage);

      // Add assistant response to UI
      const assistantMessage = {
        role: 'assistant',
        content: response.data.currentResponse,
      };
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err) {
      const errorMessage =
        err.response?.data?.message ||
        err.message ||
        'Failed to get response. Please try again.';
      setError(`❌ ${errorMessage}`);
      console.error('Chat error:', err);

      // Remove the user message if request failed
      setMessages((prev) => prev.slice(0, -1));
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-card">
        {/* Chat Header */}
        <div className="chat-header">
          <div>
            <h2>💬 Chat with Document</h2>
            <div className="document-info">📄 {documentName}</div>
          </div>
          <button className="upload-new-btn" onClick={onUploadNew}>
            📤 Upload New
          </button>
        </div>

        {/* Messages Container */}
        <div className="messages-container">
          {messages.length === 0 ? (
            <div className="empty-state">
              <h3>👋 Start a Conversation</h3>
              <p>Ask any questions about the document below</p>
            </div>
          ) : (
            messages.map((msg, idx) => (
              <div key={idx} className={`message ${msg.role}`}>
                <div className="message-avatar">
                  {msg.role === 'user' ? '👤' : '🤖'}
                </div>
                <div className="message-content">{msg.content}</div>
              </div>
            ))
          )}

          {loading && (
            <div className="message assistant">
              <div className="message-avatar">🤖</div>
              <div className="typing-indicator">
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Error Toast */}
        {error && (
          <div className="error-toast">
            {error}
            <span
              className="close-toast"
              onClick={() => setError(null)}
            >
              ✕
            </span>
          </div>
        )}

        {/* Input Container */}
        <div className="input-container">
          <div className="chat-input-wrapper">
            <textarea
              className="chat-input"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Ask a question about the document..."
              disabled={loading}
              rows="1"
            />
            <button
              className="send-btn"
              onClick={handleSendMessage}
              disabled={loading || !inputValue.trim()}
              title={loading ? 'Processing...' : 'Send message'}
            >
              {loading ? (
                <div className="spinner-small"></div>
              ) : (
                <span>📤</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

