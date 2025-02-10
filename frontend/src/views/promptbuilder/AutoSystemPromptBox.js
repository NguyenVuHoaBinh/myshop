import React, { useState } from "react";
import {
  CButton,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CFormInput,
} from "@coreui/react";

/**
 * A reusable modal component to capture user intent and submit it to
 * the parent component for processing.
 *
 * Props:
 * - visible: Boolean controlling visibility of modal
 * - onClose: Callback to close the modal
 * - onSend: Callback, receives the intent string
 */
const AutoSystemPromptBox = ({ visible, onClose, onSend }) => {
  const [intent, setIntent] = useState("");

  const handleSend = () => {
    onSend(intent);
    // Optional: clear field after send
    setIntent("");
  };

  return (
    <CModal visible={visible} onClose={onClose}>
      <CModalHeader>
        <CModalTitle>Enter Your Intent</CModalTitle>
      </CModalHeader>
      <CModalBody>
        <CFormInput
          type="text"
          placeholder="Type your intent here..."
          value={intent}
          onChange={(e) => setIntent(e.target.value)}
        />
      </CModalBody>
      <CModalFooter>
        <CButton color="secondary" onClick={onClose}>
          Cancel
        </CButton>
        <CButton color="primary" onClick={handleSend}>
          Send
        </CButton>
      </CModalFooter>
    </CModal>
  );
};

export default AutoSystemPromptBox;