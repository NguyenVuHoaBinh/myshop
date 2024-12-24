import React from "react";
import { CFormTextarea, CButton } from "@coreui/react";

const ConversationPanel = ({ conversation, userPrompt, setUserPrompt, handleSendPrompt }) => (
  <div className="conversation-panel">
    <h3>Conversation Preview</h3>
    <div className="conversation-messages" style={{ flexGrow: 1, overflowY: "auto", border: "1px solid #ccc", padding: "10px" }}>
      {conversation.length === 0 ? (
        <div style={{ textAlign: "center", padding: "50px 0" }}>
          <p>Hi! I'm an AI assistant. How can I assist you today?</p>
        </div>
      ) : (
        conversation.map((msg, idx) => (
          <div key={idx} className={`message ${msg.sender}`}>
            <strong>{msg.sender === "user" ? "You" : "Agent"}: </strong>
            {msg.message}
          </div>
        ))
      )}
    </div>
    <div className="mt-3">
      <CFormTextarea
        placeholder="Type your message here..."
        value={userPrompt}
        onChange={(e) => setUserPrompt(e.target.value)}
        rows="2"
      />
      <CButton color="primary" className="mt-2" onClick={handleSendPrompt}>
        Send
      </CButton>
    </div>
  </div>
);

export default ConversationPanel;
