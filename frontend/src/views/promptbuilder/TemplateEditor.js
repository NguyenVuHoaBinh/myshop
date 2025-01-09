import React, { useState, useEffect } from "react";
import { useLocation, useParams, useNavigate } from "react-router-dom";
import {
  CCard,
  CCardBody,
  CCardHeader,
  CSpinner,
  CAlert,
  CFormInput,
  CFormLabel,
  CRow,
  CCol,
  CFormSelect,
  CFormTextarea,
  CButton,
  CContainer,
} from "@coreui/react";

const TemplateEditor = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { id } = useParams();
  const [template, setTemplate] = useState(location.state || null);
  const [loading, setLoading] = useState(!location.state);
  const [error, setError] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewResult, setPreviewResult] = useState(null);

  // Fetch template data if no state is passed
  useEffect(() => {
    if (!template) {
      fetchTemplate();
    }
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
      setTemplate(data);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch the template. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (key, value) => {
    setTemplate({ ...template, [key]: value });
  };

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

  const handlePreview = async () => {
    setPreviewLoading(true);
    setPreviewResult(null);
    try {
      const response = await fetch("http://localhost:8888/api/v1/ai/preview", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          modelType: template.aiModel || "openai",
          templateId: id,
          message: "Generate sample result for the system role",
          config: {
            temperature: 0.7,
            max_tokens: 150,
          },
        }),
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
    return (
      <div className="text-center">
        <CSpinner color="primary" />
      </div>
    );
  }

  if (error) {
    return <CAlert color="danger">{error}</CAlert>;
  }

  if (!template) {
    return <CAlert color="warning">Template not found.</CAlert>;
  }

  return (
    <CContainer className="mt-4">
      {/* Save and Return Buttons */}
      <div className="d-flex justify-content-between mb-3">
        <CButton color="secondary" onClick={() => navigate("/")}>
          Return
        </CButton>
        <CButton color="success" onClick={handleSave}>
          Save
        </CButton>
      </div>

      {/* Main Content */}
      <CCard>
        <CCardHeader>
          <h4>Prompt Template Workspace</h4>
          <p>
            Create a prompt template with natural language instructions grounded
            in CRM data using the Role, Task, and Format framework. Define the
            role, describe the model's actions, and specify the response format.
          </p>
        </CCardHeader>
        <CCardBody>
          <CRow>
            {/* Left Column */}
            <CCol md={9}>
              <h5>Prompt Editor</h5>
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
                      const [object, objectField] = e.target.value.split(".");
                      setTemplate({ ...template, object, objectField });
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
            </CCol>

            {/* Right Column */}
            <CCol md={3}>
              <h5>Configuration</h5>
              <div className="mb-3">
                <CFormLabel>Model Type</CFormLabel>
                <CFormSelect
                  value={template.aiModel || ""}
                  onChange={(e) => handleInputChange("aiModel", e.target.value)}
                >
                  <option value="OpenAI GPT-3.5 Turbo">OpenAI GPT-3.5 Turbo</option>
                  <option value="Standard">Standard</option>
                </CFormSelect>
              </div>
              <div className="mb-3">
                <CFormLabel>Response Language Settings</CFormLabel>
                <CFormInput
                  type="text"
                  value={template.allowedLanguages?.join(", ") || ""}
                  readOnly
                />
              </div>
            </CCol>
          </CRow>
        </CCardBody>
      </CCard>

      {/* Bottom Section */}
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
