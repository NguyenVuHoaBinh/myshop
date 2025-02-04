import React from 'react';
import { 
  CFormInput, 
  CButton, 
  CFormLabel, 
  CCard, 
  CCardHeader, 
  CCardBody, 
  CFormSelect 
} from '@coreui/react';

const PromptTemplateConfig = ({ templateData, onSave, onLLMConfigChange }) => {
  return (
    <div className="container mt-4">
      <CCard>
        <CCardHeader>
          <h1>Prompt Template Workspace</h1>
          <strong>Configure your prompt template</strong>
        </CCardHeader>
        <CCardBody>
          {/* Read-only Template Info */}
          <div className="mb-3">
            <CFormLabel>Template Name</CFormLabel>
            <CFormInput value={templateData.name} disabled />
          </div>
          <div className="mb-3">
            <CFormLabel>API Name</CFormLabel>
            <CFormInput value={templateData.apiName} disabled />
          </div>
          <div className="mb-3">
            <CFormLabel>Description</CFormLabel>
            <CFormInput value={templateData.description} disabled />
          </div>
          <div className="mb-3">
            <CFormLabel>Object</CFormLabel>
            <CFormInput value={templateData.object} disabled />
          </div>
          <div className="mb-3">
            <CFormLabel>Object Field</CFormLabel>
            <CFormInput value={templateData.objectField} disabled />
          </div>

          {/* LLM Configuration */}
          <div className="mb-3">
            <CFormLabel>LLM Model</CFormLabel>
            <CFormSelect
              value={templateData.llmconfig?.aiModel || ""}
              onChange={(e) => onLLMConfigChange("aiModel", e.target.value)}
            >
              <option value="gpt-4o">gpt-4o</option>
              <option value="gpt-4o-mini">gpt-4o-mini</option>
              <option value="o3-mini">o3-mini</option>
            </CFormSelect>
          </div>
          <div className="mb-3">
            <CFormLabel>Temperature</CFormLabel>
            <CFormInput
              type="number"
              value={templateData.llmconfig?.temperature || 0.7}
              onChange={(e) => onLLMConfigChange("temperature", parseFloat(e.target.value))}
            />
          </div>
          <div className="mb-3">
            <CFormLabel>Max Tokens</CFormLabel>
            <CFormInput
              type="number"
              value={templateData.llmconfig?.max_tokens || 100}
              onChange={(e) => onLLMConfigChange("max_tokens", parseInt(e.target.value, 10))}
            />
          </div>
          <div className="mb-3">
            <CFormLabel>Stream</CFormLabel>
            <CFormSelect
              value={templateData.llmconfig?.stream ? "true" : "false"}
              onChange={(e) => onLLMConfigChange("stream", e.target.value === "true")}
            >
              <option value="false">Disabled</option>
              <option value="true">Enabled</option>
            </CFormSelect>
          </div>

          {/* Save & Preview Button */}
          <div className="mt-4">
            <CButton color="primary" onClick={onSave}>
              Save & Preview
            </CButton>
          </div>
        </CCardBody>
      </CCard>
    </div>
  );
};

export default PromptTemplateConfig;
