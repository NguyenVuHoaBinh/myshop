import React from "react";
import { FaClipboardList } from "react-icons/fa";

const CentralContentPanel = () => {
  return (
    <div className="centered-content" style={{ textAlign: 'center' }}>
      <FaClipboardList size={48} style={{ color: '#007bff' }} />
      <h2 style={{ marginTop: '20px' }}>Put your topics to the test</h2>
      <p style={{ color: '#6c757d' }}>Start a conversation to preview how your agent builds a plan and executes actions based on user interactions.</p>
    </div>
  );
};

export default CentralContentPanel;
