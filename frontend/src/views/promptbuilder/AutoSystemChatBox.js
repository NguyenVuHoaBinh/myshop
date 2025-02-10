// src/components/AutoSystemChatBox.js

import React, { useState } from "react"
import {
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CButton,
  CSpinner,
  CFormTextarea,
} from "@coreui/react"

/**
 * A small chatbox-like component for user input ("intent") that, on Send:
 * 1. Displays a spinner until the request completes.
 * 2. Calls the auto-system-prompt endpoint to generate a new system prompt.
 * 3. Closes automatically when done, passing the generated prompt back to the parent.
 *
 * Props:
 * - visible: boolean to show/hide the modal
 * - templateId: string ID of the template
 * - onClose: callback to close/hide the modal without action
 * - onSuccess: callback receiving (generatedPrompt) after a successful fetch
 */
const AutoSystemChatBox = ({ visible, templateId, onClose, onSuccess }) => {
  // The user’s typed "intent"
  const [userMessage, setUserMessage] = useState("")
  // Track loading state to show spinner
  const [isLoading, setIsLoading] = useState(false)
  // Optionally store the error message
  const [error, setError] = useState("")

  const handleSend = async () => {
    if (!userMessage.trim()) return

    try {
      setIsLoading(true)
      setError("")

      const endpoint = `http://localhost:8888/api/auto-system-prompt/${templateId}/generate?userInput=${encodeURIComponent(
        userMessage
      )}`

      const response = await fetch(endpoint, { method: "POST" })
      if (!response.ok) {
        throw new Error("Request failed.")
      }
      const data = await response.json()

      // The controller might return the prompt in different places:
      // e.g., data.content, or data.choices[0].message.content
      const generatedPrompt =
        data.content ||
        (data.choices &&
          data.choices[0]?.message?.content) ||
        "No prompt found."

      // Pass the newly generated prompt back up
      onSuccess(generatedPrompt)

      // Close and reset
      setIsLoading(false)
      setUserMessage("")
    } catch (err) {
      console.error(err)
      setError("Failed to generate prompt. Please try again.")
      setIsLoading(false)
    }
  }

  return (
    <CModal visible={visible} onClose={onClose}>
      <CModalHeader>
        <CModalTitle>Chat Box</CModalTitle>
      </CModalHeader>

      <CModalBody>
        <div style={{ marginBottom: "1rem" }}>
          <strong>Describe your intent:</strong>
        </div>
        <CFormTextarea
          rows={4}
          placeholder="Enter your request or intent..."
          value={userMessage}
          onChange={(e) => setUserMessage(e.target.value)}
          disabled={isLoading}
        />
        {error && (
          <div className="text-danger mt-2">
            <small>{error}</small>
          </div>
        )}
      </CModalBody>

      <CModalFooter>
        {/* Display spinner if loading */}
        {isLoading && (
          <div className="me-auto">
            <CSpinner color="primary" size="sm" />
            <span className="ms-2">Generating...</span>
          </div>
        )}

        <CButton color="secondary" onClick={onClose} disabled={isLoading}>
          Cancel
        </CButton>
        <CButton color="primary" onClick={handleSend} disabled={isLoading}>
          Send
        </CButton>
      </CModalFooter>
    </CModal>
  )
}

export default AutoSystemChatBox
