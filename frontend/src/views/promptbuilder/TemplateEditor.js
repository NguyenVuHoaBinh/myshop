import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CSpinner,
  CAlert,
  CFormInput,
  CFormLabel,
  CRow,
  CCol,
  CFormSelect,
  CFormTextarea,
} from "@coreui/react";

const TemplateEditor = () => {
  const { id } = useParams(); // Fetch the ID from the route
  const [template, setTemplate] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchTemplate();
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
      setTemplate(data); // Set the template data
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
    <div className="container mt-4">
      <CCard>
        <CCardHeader>
          <h2 className="text-center">Template Editor</h2>
        </CCardHeader>
        <CCardBody>
          <CRow>
            {/* Left Column */}
            <CCol md={6}>
              <div className="mb-4">
                <h5>System Prompt</h5>
                <CFormTextarea
                  rows="8"
                  value={template.systemPrompt || ""}
                  onChange={(e) => handleInputChange("systemPrompt", e.target.value)}
                  style={{
                    background: "#fff",
                    border: "1px solid #ddd",
                    padding: "10px",
                    borderRadius: "5px",
                  }}
                />
              </div>
              <div className="mb-4">
                <h5>Fields</h5>
                <div
                  style={{
                    background: "#f9f9f9",
                    border: "1px solid #ddd",
                    padding: "15px",
                    borderRadius: "5px",
                  }}
                >
                  {template.fields?.length > 0 ? (
                    <ul>
                      {template.fields.map((field, index) => (
                        <li key={index}>
                          <strong>{field.fieldName}</strong> ({field.fieldType})
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p>No fields available.</p>
                  )}
                </div>
              </div>
            </CCol>
            {/* Right Column */}
            <CCol md={6}>
              <div className="mb-4">
                <h5>AI Model</h5>
                <CFormLabel>Model</CFormLabel>
                <CFormSelect
                  value={template.aiModel || ""}
                  onChange={(e) => handleInputChange("aiModel", e.target.value)}
                >
                  <option value="OpenAI GPT-3.5 Turbo">OpenAI GPT-3.5 Turbo</option>
                  <option value="Standard">Standard</option>
                </CFormSelect>
              </div>
              <div className="mb-4">
                <h5>Response Language Settings</h5>
                <CFormLabel>Allowed Response Languages</CFormLabel>
                <CFormInput
                  type="text"
                  value={template.allowedLanguages?.join(", ") || ""}
                  readOnly
                  style={{
                    background: "#fff",
                    border: "1px solid #ddd",
                    padding: "10px",
                    borderRadius: "5px",
                  }}
                />
              </div>
            </CCol>
          </CRow>
          <div className="text-center">
            <CButton
              color="primary"
              className="me-3"
              onClick={() => console.log("Save changes", template)}
            >
              Save Changes
            </CButton>
            <CButton color="secondary" onClick={() => console.log("Cancel")}>
              Cancel
            </CButton>
          </div>
        </CCardBody>
      </CCard>
    </div>
  );
};

export default TemplateEditor;
