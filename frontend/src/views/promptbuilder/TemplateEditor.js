import React, { useState, useEffect } from "react";
import { useLocation, useParams, useNavigate } from "react-router-dom";
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
} from "@coreui/react";

/* ------------------- Helper UI Components ------------------- */
const LoadingSpinner = () => (
  <div className="text-center my-4">
    <CSpinner color="primary" />
  </div>
);

const ErrorAlert = ({ message }) => <CAlert color="danger">{message}</CAlert>;

/* ------------------- Prompt Editor Panel ------------------- */
const PromptEditorPanel = ({ template, onInputChange }) => (
  <CCard className="mb-4">
    <CCardHeader>
      <h4>Prompt Editor</h4>
    </CCardHeader>
    <CCardBody>
      <CFormTextarea
        rows="8"
        value={template.systemPrompt || ""}
        onChange={(e) => onInputChange("systemPrompt", e.target.value)}
        placeholder="Write your prompt here..."
      />
      <CRow className="mt-4">
        <CCol>
          <CFormLabel>Resource</CFormLabel>
          <CFormSelect
            value={template.resource || ""}
            onChange={(e) => onInputChange("resource", e.target.value)}
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
              const [object, objectField] = e.target.value.split(".");
              onInputChange("object", object);
              onInputChange("objectField", objectField);
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
);

/* ------------------- LLM Config Component ------------------- */
const LLMConfig = ({ llmconfig, onLLMConfigChange }) => (
  <CCard>
    <CCardHeader>
      <h5>LLM Config</h5>
    </CCardHeader>
    <CCardBody>
      <div className="mb-3">
        <CFormLabel>Model Type</CFormLabel>
        <CFormSelect
          value={llmconfig?.aiModel || ""}
          onChange={(e) => onLLMConfigChange("aiModel", e.target.value)}
        >
          {/* Note: The option values below represent the internal value to store. */}
          <option value="gpt-4o">gpt-4o</option>
          <option value="gpt-4o-mini">gpt-4o-mini</option>
          <option value="o3-mini">o3-mini</option>
        </CFormSelect>
      </div>
      <div className="mb-3">
        <CFormLabel>
          Temperature: {llmconfig?.temperature !== undefined ? llmconfig.temperature : 0.7}
        </CFormLabel>
        <input
          type="range"
          className="form-range"
          min="0"
          max="2"
          step="0.1"
          value={llmconfig?.temperature !== undefined ? llmconfig.temperature : 0.7}
          onChange={(e) =>
            onLLMConfigChange("temperature", parseFloat(e.target.value))
          }
        />
      </div>
      <div className="mb-3">
        <CFormLabel>
          Max Tokens: {llmconfig?.max_tokens !== undefined ? llmconfig.max_tokens : 100}
        </CFormLabel>
        <input
          type="range"
          className="form-range"
          min="1"
          max="16383"
          step="1"
          value={llmconfig?.max_tokens !== undefined ? llmconfig.max_tokens : 100}
          onChange={(e) =>
            onLLMConfigChange("max_tokens", parseInt(e.target.value, 10))
          }
        />
      </div>
      <div className="mb-3 form-check">
        <input
          type="checkbox"
          className="form-check-input"
          id="streamCheck"
          checked={llmconfig?.stream || false}
          onChange={(e) => onLLMConfigChange("stream", e.target.checked)}
        />
        <label className="form-check-label" htmlFor="streamCheck">
          Stream
        </label>
      </div>
    </CCardBody>
  </CCard>
);

/* ------------------- Preview Panel ------------------- */
const PreviewPanel = ({ previewLoading, previewResult }) => (
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
);

/* ------------------- Main TemplateEditor Component ------------------- */
const TemplateEditor = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { id } = useParams();

  const [template, setTemplate] = useState(location.state || null);
  const [loading, setLoading] = useState(!location.state);
  const [error, setError] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewResult, setPreviewResult] = useState(null);

  // Fetch template if not provided via state
  useEffect(() => {
    if (!template) {
      fetchTemplate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const fetchTemplate = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`http://localhost:8888/api/templates/${id}`);
      if (!response.ok) {
        throw new Error(`Error fetching template: ${response.statusText}`);
      }
      const data = await response.json();
      // Ensure llmconfig exists in the fetched template
      if (!data.llmconfig) {
        data.llmconfig = {
          aiModel: "gpt-4o",
          temperature: 0.7,
          max_tokens: 100,
          stream: false,
        };
      }
      setTemplate(data);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch the template. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Update top-level fields
  const handleInputChange = (key, value) => {
    setTemplate((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  // Update nested LLM configuration fields
  const handleLLMConfigChange = (key, value) => {
    setTemplate((prev) => ({
      ...prev,
      llmconfig: {
        ...prev.llmconfig,
        [key]: value,
      },
    }));
  };

  // Save payload – the nested llmconfig is already stored
  const handleSave = async () => {
    try {
      const response = await fetch(`http://localhost:8888/api/templates/${id || ""}`, {
        method: id ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(template),
      });
      if (!response.ok) {
        throw new Error("Failed to save the template.");
      }
      alert("Template saved successfully!");
    } catch (err) {
      console.error(err);
      alert("Failed to save the template. Please try again.");
    }
  };

  // Build preview payload using nested llmconfig values.
  // Note: We now use the exact value stored in llmconfig.aiModel.
  const handlePreview = async () => {
    setPreviewLoading(true);
    setPreviewResult(null);
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
      };

      const response = await fetch("http://localhost:8888/api/v1/ai/preview", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error("Failed to fetch preview.");
      }
      const data = await response.json();
      setPreviewResult(data);
    } catch (err) {
      console.error(err);
      alert("Failed to generate preview. Please try again.");
    } finally {
      setPreviewLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return <ErrorAlert message={error} />;
  }

  if (!template) {
    return <CAlert color="warning">Template not found.</CAlert>;
  }

  return (
    <CContainer className="mt-4">
      {/* Navigation Buttons */}
      <div className="d-flex justify-content-between mb-3">
        <CButton color="secondary" onClick={() => navigate("/")}>
          Return
        </CButton>
        <CButton color="success" onClick={handleSave}>
          Save
        </CButton>
      </div>

      {/* Main Panels: Left Prompt Editor & Right LLM Config */}
      <CRow>
        <CCol md={9}>
          <PromptEditorPanel template={template} onInputChange={handleInputChange} />
        </CCol>
        <CCol md={3}>
          <LLMConfig llmconfig={template.llmconfig} onLLMConfigChange={handleLLMConfigChange} />
        </CCol>
      </CRow>

      {/* Preview & Response Panels */}
      <PreviewPanel previewLoading={previewLoading} previewResult={previewResult} />

      {/* Preview Button */}
      <div className="text-center mt-4">
        <CButton color="info" onClick={handlePreview}>
          Preview
        </CButton>
      </div>
    </CContainer>
  );
};

export default TemplateEditor;
