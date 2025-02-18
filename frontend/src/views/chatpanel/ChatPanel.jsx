// src/components/flow/ChatPanel.js
import React, { useState, useRef, useEffect } from "react";

/**
 * ChatPanel: a standalone chat component that handles:
 *   - WebSocket connection
 *   - chat messages state
 *   - user input
 *   - optionally triggers an initial POST request for a "start chat" scenario
 */
const ChatPanel = ({ flowId }) => {
  // Chat messages
  const [chatMessages, setChatMessages] = useState([]);

  // Current user input
  const [userInput, setUserInput] = useState("");

  // Whether the chat is "started" (to show/hide input and button)
  const [chatStarted, setChatStarted] = useState(false);

  // Refs for the chat container (for auto-scroll) and the WebSocket
  const chatContainerRef = useRef(null);
  const wsRef = useRef(null);

  // 1) Establish the WebSocket connection on mount
  useEffect(() => {
    // Replace with your actual WebSocket URL, e.g. "ws://localhost:8888/ws/chat"
    const wsUrl = "ws://localhost:8888/ws/chat";
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("Chat WebSocket connection established:", wsUrl);
    };

    ws.onmessage = (evt) => {
      try {
        const data = JSON.parse(evt.data);
        // Expected shape: { sender: "bot", message: "..." }
        const newMessage = {
          sender: data.sender || "bot",
          text: data.message || "",
        };
        setChatMessages((prev) => [...prev, newMessage]);
      } catch (err) {
        console.error("Failed to parse chat WebSocket message:", err);
      }
    };

    ws.onclose = () => {
      console.log("Chat WebSocket connection closed.");
    };

    ws.onerror = (error) => {
      console.error("Chat WebSocket error:", error);
    };

    // Cleanup on unmount
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, []);

  // 2) Auto-scroll to bottom when new messages appear
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [chatMessages]);

  // 3) Handle the "Start Chat" button
  const handleStartChat = async () => {
    setChatStarted(true);

    // // OPTION A: Make a POST request to get the initial message
    // try {
    //   const response = await fetch("/api/chat/start", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({ flowId: flowId || "defaultFlow" }),
    //   });
    //   if (!response.ok) {
    //     throw new Error("Failed to start chat");
    //   }
    //   const data = await response.json();
    //   if (data.firstMessage) {
    //     // Append the "bot" message to our local chat
    //     setChatMessages((prev) => [
    //       ...prev,
    //       { sender: "bot", text: data.firstMessage },
    //     ]);
    //   }
    // } catch (error) {
    //   console.error("Error starting chat:", error);
    // }

    // OPTION B (comment out A, enable B if you prefer purely WebSocket):
    // Instead of a REST call, you could also send a special WebSocket message
    
    const ws = wsRef.current;
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(
        JSON.stringify({
          flowId: flowId || "defaultFlow",
          // server can interpret "__start__" to skip directly to the first node
          userResponse: "__start__",
        })
      );
    }
    
  };

  // 4) Send a chat message via WebSocket
  const handleChatSend = () => {
    if (!userInput.trim()) return;
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.error("WebSocket is not open. Cannot send message.");
      return;
    }

    // 4a) Add user message to local chat
    const userMsg = { sender: "user", text: userInput };
    setChatMessages((prev) => [...prev, userMsg]);

    // 4b) Send JSON to the server
    const payload = {
      flowId: flowId || "defaultFlow",
      userResponse: userInput,
    };
    ws.send(JSON.stringify(payload));

    // 4c) Clear input
    setUserInput("");
  };

  return (
    <div
      style={{
        flex: 1,
        borderLeft: "1px solid #ccc",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* HEADER */}
      <div style={{ padding: "10px", borderBottom: "1px solid #ccc" }}>
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <h4>Chat Panel</h4>
          {/* Show the Start button if chat hasn't started */}
          {!chatStarted && (
            <button onClick={handleStartChat} style={{ padding: "8px 16px" }}>
              Start Chat
            </button>
          )}
        </div>
      </div>

      {/* MESSAGES AREA */}
      <div
        ref={chatContainerRef}
        style={{
          flex: 1,
          padding: "10px",
          backgroundColor: "#f7f7f7",
        }}
      >
        {chatMessages.map((msg, idx) => (
          <div
            key={idx}
            style={{
              display: "flex",
              justifyContent:
                msg.sender === "user" ? "flex-end" : "flex-start",
              marginBottom: "8px",
            }}
          >
            <div
              style={{
                maxWidth: "60%",
                borderRadius: "8px",
                padding: "8px 12px",
                backgroundColor: msg.sender === "user" ? "#0d6efd" : "#e2e2e2",
                color: msg.sender === "user" ? "#fff" : "#000",
                whiteSpace: "pre-wrap",
              }}
            >
              {msg.text}
            </div>
          </div>
        ))}
      </div>

      {/* INPUT AREA (only if chatStarted) */}
      {chatStarted && (
        <div
          style={{
            display: "flex",
            borderTop: "1px solid #ccc",
            padding: "10px",
          }}
        >
          <input
            type="text"
            placeholder="Type your message..."
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            style={{
              flex: 1,
              marginRight: "10px",
              borderRadius: "4px",
              border: "1px solid #ccc",
              padding: "8px",
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleChatSend();
            }}
          />
          <button
            onClick={handleChatSend}
            style={{
              padding: "8px 16px",
              backgroundColor: "#0d6efd",
              color: "#fff",
              border: "none",
              borderRadius: "4px",
            }}
          >
            Send
          </button>
        </div>
      )}
    </div>
  );
};

export default ChatPanel;
