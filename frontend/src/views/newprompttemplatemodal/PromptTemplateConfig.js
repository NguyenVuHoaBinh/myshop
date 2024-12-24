import React from 'react';
import { CFormInput, CButton, CFormLabel, CCard, CCardHeader, CCardBody, CFormSelect } from '@coreui/react';

const PromptTemplateConfig = ({ templateData, onSave }) => {
  return (
    <div className="container mt-4">
      <CCard>
        <CCardHeader>
          <h1>Prompt Template Workspace</h1>
          <strong>Configure your prompt template</strong>
        </CCardHeader>
        <CCardBody>
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
          <div className="mb-3">
            <CFormLabel>Model Type</CFormLabel>
            <CFormSelect>
              <option>Standard</option>
              <option>Advanced</option>
            </CFormSelect>
          </div>
          <div className="mb-3">
            <CFormLabel>Model</CFormLabel>
            <CFormSelect>
              <option>OpenAI GPT-3.5 Turbo</option>
              <option>OpenAI GPT-4</option>
            </CFormSelect>
          </div>
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
