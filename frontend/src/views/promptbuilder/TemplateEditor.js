import React, { useState, useEffect } from "react"
import { useLocation, useParams, useNavigate } from "react-router-dom"
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CFormLabel,
  CRow,
  CCol,
  CFormSelect,
  CFormTextarea,
  CButton,
  CContainer,
} from "@coreui/react"

import AutoSystemChatBox from "./AutoSystemChatBox" // Chat-based auto prompt

/**
 * A simple spinner component.
 */
const LoadingSpinner = () => (
  <div className="text-center my-4">
    <CSpinner color="primary" />
  </div>
)

/**
 * A simple alert for errors.
 */
const ErrorAlert = ({ message }) => <CAlert color="danger">{message}</CAlert>

/**
 * Main TemplateEditor Component
 */
const TemplateEditor = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const { id } = useParams()

  // Template state: if we navigated with location.state, we can use that,
  // otherwise, we fetch from the server by ID.
  const [template, setTemplate] = useState(location.state || null)

  // Basic UI states
  const [loading, setLoading] = useState(!location.state)
  const [error, setError] = useState(null)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [previewResult, setPreviewResult] = useState(null)

  // Show/hide the chat-based auto system prompt modal
  const [showAutoSystemChatBox, setShowAutoSystemChatBox] = useState(false)

  // **New**: dynamic list of model types from the server
  const [modelTypes, setModelTypes] = useState([])

  /**
   * Fetch the template if not already provided via location.state.
   */
  useEffect(() => {
    if (!template) {
      fetchTemplate()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  /**
   * Fetch the list of model types on mount (or if you want to do it only once, you can ignore [id])
   */
  useEffect(() => {
    fetchModelTypes()
  }, [])

  /**
   * Loads the template from the server.
   */
  const fetchTemplate = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch(`http://localhost:8888/api/templates/${id}`)
      if (!response.ok) {
        throw new Error(`Error fetching template: ${response.statusText}`)
      }
      const data = await response.json()
      // If llmconfig is missing, set defaults
      if (!data.llmconfig) {
        data.llmconfig = {
          aiModel: "",
          temperature: 0.7,
          max_tokens: 100,
          stream: false,
        }
      }
      setTemplate(data)
    } catch (err) {
      console.error(err)
      setError("Failed to fetch the template. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  /**
   * Loads the available model types from the server (dynamic).
   */
  const fetchModelTypes = async () => {
    try {
      const response = await fetch("http://localhost:8888/api/model-types")
      if (!response.ok) {
        throw new Error("Failed to fetch model types.")
      }
      const data = await response.json()
      setModelTypes(data)
    } catch (err) {
      console.error(err)
      // fallback or show an error for model type loading
      setModelTypes([]) // or a default list
    }
  }

  /**
   * Updates top-level fields of the template.
   */
  const handleInputChange = (key, value) => {
    setTemplate((prev) => ({
      ...prev,
      [key]: value,
    }))
  }

  /**
   * Updates nested LLM config fields
   */
  const handleLLMConfigChange = (key, value) => {
    setTemplate((prev) => ({
      ...prev,
      llmconfig: {
        ...prev.llmconfig,
        [key]: value,
      },
    }))
  }

  /**
   * Saves the template (PUT if ID exists, else POST).
   */
  const handleSave = async () => {
    try {
      const response = await fetch(
        `http://localhost:8888/api/templates/${id || ""}`, 
        {
          method: id ? "PUT" : "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(template),
        }
      )
      if (!response.ok) {
        throw new Error("Failed to save the template.")
      }
      alert("Template saved successfully!")
    } catch (err) {
      console.error(err)
      alert("Failed to save the template. Please try again.")
    }
  }

  /**
   * Generates a "preview" of the prompt + LLM response
   */
  const handlePreview = async () => {
    setPreviewLoading(true)
    setPreviewResult(null)
    try {
      const requestBody = {
        templateId: id,
        message: "Generate sample result for the system role",
        llmconfig: {
          modelType: template.llmconfig.aiModel,
          temperature: template.llmconfig.temperature,
          max_tokens: template.llmconfig.max_tokens,
          stream: template.llmconfig.stream,
        },
      }

      const response = await fetch("http://localhost:8888/api/v1/ai/preview", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      })
      if (!response.ok) {
        throw new Error("Failed to fetch preview.")
      }
      const data = await response.json()
      setPreviewResult(data)
    } catch (err) {
      console.error(err)
      alert("Failed to generate preview. Please try again.")
    } finally {
      setPreviewLoading(false)
    }
  }

  /**
   * When chatbox is successful, we get the newly generated system prompt.
   */
  const handleAutoSystemChatBoxSuccess = (generatedPrompt) => {
    setTemplate((prev) => ({
      ...prev,
      systemPrompt: generatedPrompt,
    }))
    setShowAutoSystemChatBox(false) // close the chatbox
  }

  // -------------------------------------------------------------------------
  // Render Logic
  // -------------------------------------------------------------------------
  
  if (loading) {
    return <LoadingSpinner />
  }

  if (error) {
    return <ErrorAlert message={error} />
  }

  if (!template) {
    return <CAlert color="warning">Template not found.</CAlert>
  }

  return (
    <CContainer className="mt-4">
      {/* Top Bar */}
      <div className="d-flex justify-content-between mb-3">
        <div>
          <CButton color="secondary" onClick={() => navigate("/")}>
            Return
          </CButton>{" "}
          <CButton color="warning" onClick={() => setShowAutoSystemChatBox(true)}>
            Auto System Prompt
          </CButton>
        </div>
        <CButton color="success" onClick={handleSave}>
          Save
        </CButton>
      </div>

      {/* The chatbox modal (multi-message chat) */}
      <AutoSystemChatBox
        visible={showAutoSystemChatBox}
        templateId={id}
        onClose={() => setShowAutoSystemChatBox(false)}
        onSuccess={handleAutoSystemChatBoxSuccess}
      />

      {/* Main Panels */}
      <CRow>
        {/* Left side - Prompt Editor */}
        <CCol md={9}>
          <CCard className="mb-4">
            <CCardHeader>
              <h4>Prompt Editor</h4>
            </CCardHeader>
            <CCardBody>
              <CFormTextarea
                rows="8"
                value={template.systemPrompt || ""}
                onChange={(e) => handleInputChange("systemPrompt", e.target.value)}
                placeholder="Write your prompt here..."
              />
              <CRow className="mt-4">
                <CCol>
                  <CFormLabel>Resource</CFormLabel>
                  <CFormSelect
                    value={template.resource || ""}
                    onChange={(e) => handleInputChange("resource", e.target.value)}
                  >
                    <option value="">Select a resource...</option>
                    <option value="Flows">Flows</option>
                    <option value="Apex">Apex</option>
                  </CFormSelect>
                </CCol>
                <CCol>
                  <CFormLabel>Object Field</CFormLabel>
                  <CFormSelect
                    value={`${template.object || ""}.${template.objectField || ""}`}
                    onChange={(e) => {
                      const [object, objectField] = e.target.value.split(".")
                      handleInputChange("object", object)
                      handleInputChange("objectField", objectField)
                    }}
                  >
                    <option value="">Select an object field...</option>
                    {template.objectFields?.map((field, index) => (
                      <option key={index} value={`${template.object || "object"}.${field}`}>
                        {`${template.object || "object"}.${field}`}
                      </option>
                    ))}
                  </CFormSelect>
                </CCol>
              </CRow>
            </CCardBody>
          </CCard>
        </CCol>

        {/* Right side - LLM Config */}
        <CCol md={3}>
          <CCard>
            <CCardHeader>
              <h5>LLM Config</h5>
            </CCardHeader>
            <CCardBody>
              {/* Model Type: now dynamic */}
              <div className="mb-3">
                <CFormLabel>Model Type</CFormLabel>
                <CFormSelect
                  value={template.llmconfig?.aiModel || ""}
                  onChange={(e) => handleLLMConfigChange("aiModel", e.target.value)}
                >
                  <option value="">Select a model...</option>
                  {modelTypes.map((model) => (
                    <option key={model} value={model}>
                      {model}
                    </option>
                  ))}
                </CFormSelect>
              </div>

              {/* Temperature */}
              <div className="mb-3">
                <CFormLabel>
                  Temperature: {template.llmconfig?.temperature ?? 0.7}
                </CFormLabel>
                <input
                  type="range"
                  className="form-range"
                  min="0"
                  max="2"
                  step="0.1"
                  value={template.llmconfig?.temperature ?? 0.7}
                  onChange={(e) =>
                    handleLLMConfigChange("temperature", parseFloat(e.target.value))
                  }
                />
              </div>

              {/* Max Tokens */}
              <div className="mb-3">
                <CFormLabel>
                  Max Tokens: {template.llmconfig?.max_tokens ?? 100}
                </CFormLabel>
                <input
                  type="range"
                  className="form-range"
                  min="1"
                  max="16383"
                  step="1"
                  value={template.llmconfig?.max_tokens ?? 100}
                  onChange={(e) =>
                    handleLLMConfigChange("max_tokens", parseInt(e.target.value, 10))
                  }
                />
              </div>

              {/* Stream */}
              <div className="mb-3 form-check">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id="streamCheck"
                  checked={template.llmconfig?.stream || false}
                  onChange={(e) => handleLLMConfigChange("stream", e.target.checked)}
                />
                <label className="form-check-label" htmlFor="streamCheck">
                  Stream
                </label>
              </div>
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      {/* Preview Section */}
      <CRow className="mt-4">
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Preview</h6>
            </CCardHeader>
            <CCardBody>
              {previewLoading ? (
                <div className="text-center">
                  <CSpinner color="info" />
                  <p>Generating preview...</p>
                </div>
              ) : previewResult ? (
                <>
                  <h6>Combined Prompt</h6>
                  <pre>{previewResult.combinedPrompt}</pre>
                </>
              ) : (
                <p>No preview available.</p>
              )}
            </CCardBody>
          </CCard>
        </CCol>
        <CCol md={6}>
          <CCard>
            <CCardHeader>
              <h6>Response</h6>
            </CCardHeader>
            <CCardBody>
              {previewLoading ? (
                <div className="text-center">
                  <CSpinner color="info" />
                  <p>Fetching response...</p>
                </div>
              ) : previewResult ? (
                <>
                  <h6>Response</h6>
                  <pre>{previewResult.response}</pre>
                </>
              ) : (
                <p>No response available.</p>
              )}
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>

      <div className="text-center mt-4">
        <CButton color="info" onClick={handlePreview}>
          Preview
        </CButton>
      </div>
    </CContainer>
  )
}

export default TemplateEditor
