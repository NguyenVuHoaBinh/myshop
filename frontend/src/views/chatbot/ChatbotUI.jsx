import React, { useEffect, useRef, useState } from 'react';
import {
  CContainer,
  CRow,
  CCol,
  CCard,
  CCardHeader,
  CCardBody,
  CCardFooter,
  CForm,
  CFormInput,
  CButton
} from '@coreui/react';

const ChatbotUI = () => {
  const [messages, setMessages] = useState([
    { sender: 'bot', text: 'Hello! How can I assist you?' },
  ]);
  const [userInput, setUserInput] = useState('');
  const chatContainerRef = useRef(null);
  const [socket, setSocket] = useState(null);

  // 1. Establish WebSocket connection on component mount
  useEffect(() => {
    const newSocket = new WebSocket('ws://localhost:8080/ws/chat');
    setSocket(newSocket);

    // Cleanup on unmount
    return () => {
      if (newSocket.readyState === WebSocket.OPEN) {
        newSocket.close();
      }
    };
  }, []);

  // 2. Define WebSocket event handlers
  useEffect(() => {
    if (!socket) return;

    socket.onopen = () => {
      console.log('WebSocket connection established.');
    };

    socket.onmessage = (event) => {
      // Parse the incoming message
      try {
        const data = JSON.parse(event.data);

        // Expecting something like: { sender: 'bot', message: 'some text' }
        const newMessage = {
          sender: data.sender || 'bot',
          text: data.message || '',
        };
        setMessages((prev) => [...prev, newMessage]);
      } catch (err) {
        console.error('Failed to parse WebSocket message:', err);
      }
    };

    socket.onclose = () => {
      console.log('WebSocket connection closed.');
    };

    socket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }, [socket]);

  // 3. Scroll to bottom when messages change
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
    }
  }, [messages]);

  // 4. Handle send message
  const handleSendMessage = () => {
    if (!userInput.trim() || !socket) return;

    // Add user's message to the chat
    const newMessage = { sender: 'user', text: userInput };
    setMessages((prev) => [...prev, newMessage]);

    // Send JSON message to the server
    const payload = {
      flowId: 'yourFlowId',   // adapt to your actual flow
      userResponse: userInput,
    };
    socket.send(JSON.stringify(payload));

    // Clear input
    setUserInput('');
  };

  // 5. Render UI with CoreUI components
  return (
    <CContainer fluid className="vh-100 d-flex flex-column">
      <CRow className="flex-grow-0">
        <CCol>
          <h2 className="mt-4">My Chatbot</h2>
        </CCol>
      </CRow>

      <CRow className="flex-grow-1">
        <CCol>
          <CCard className="h-100 d-flex flex-column">
            <CCardHeader>Flow-based Chat</CCardHeader>
            <CCardBody className="flex-grow-1 overflow-auto" innerRef={chatContainerRef}>
              {messages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`d-flex mb-2 ${
                    msg.sender === 'user' ? 'justify-content-end' : 'justify-content-start'
                  }`}
                >
                  <div
                    style={{
                      backgroundColor: msg.sender === 'user' ? '#0d6efd' : '#e4e4e4',
                      color: msg.sender === 'user' ? '#fff' : '#000',
                      borderRadius: '12px',
                      padding: '8px 12px',
                      maxWidth: '75%',
                    }}
                  >
                    {msg.text}
                  </div>
                </div>
              ))}
            </CCardBody>
            <CCardFooter>
              <CForm
                onSubmit={(e) => {
                  e.preventDefault();
                  handleSendMessage();
                }}
                className="d-flex"
              >
                <CFormInput
                  type="text"
                  placeholder="Type your message..."
                  value={userInput}
                  onChange={(e) => setUserInput(e.target.value)}
                />
                <CButton
                  color="primary"
                  className="ms-2"
                  onClick={handleSendMessage}
                >
                  Send
                </CButton>
              </CForm>
            </CCardFooter>
          </CCard>
        </CCol>
      </CRow>
    </CContainer>
  );
};

export default ChatbotUI;
