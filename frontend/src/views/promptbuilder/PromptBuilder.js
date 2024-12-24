import React, { useState, useEffect } from 'react';
import { CTable, CTableHead, CTableRow, CTableHeaderCell, CTableBody, CTableDataCell, CButton, CFormInput, CCardHeader } from '@coreui/react';
import NewPromptTemplateModal from '../newprompttemplatemodal/NewPromptTemplateModal';
import PromptTemplateConfig from '../newprompttemplatemodal/PromptTemplateConfig';

const PromptBuilder = () => {
  const [templates, setTemplates] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [newTemplateData, setNewTemplateData] = useState(null);

  // Example templates (useEffect removed for bypassing backend)
  useEffect(() => {
    // Initial hardcoded templates, simulating a backend fetch
    setTemplates([
      {
        name: 'Order Assistance',
        templateType: 'Standard',
        category: 'Support',
        status: 'Active',
        modified: '2024-12-01',
      },
    ]);
  }, []);

  // Handle Search
  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  // Filter templates based on search term
  const filteredTemplates = templates.filter((template) =>
    template.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Handle creating a new prompt template
  const handleCreateNew = () => {
    setShowModal(true);
  };

  // Handle saving the new template without backend API call
  const handleSaveTemplate = (data) => {
    // Simulate saving by adding directly to the state
    const newTemplate = {
      ...data,
      modified: new Date().toISOString().split('T')[0], // Current date for "modified"
    };
    setTemplates([...templates, newTemplate]);
    setNewTemplateData(newTemplate); // Set to navigate to configuration screen
    setShowModal(false); // Close modal after saving
  };

  // If there's a newly created template, redirect to the config page
  if (newTemplateData) {
    return (
      <PromptTemplateConfig
        templateData={newTemplateData}
        onSave={() => {
          setNewTemplateData(null);
          // Optionally, add more logic after saving config.
        }}
      />
    );
  }

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <CCardHeader>
          <h1>Prompt Builder</h1>
          <strong>All prompt templates</strong>
        </CCardHeader>
        <CFormInput
          placeholder="Search templates..."
          value={searchTerm}
          onChange={handleSearch}
          style={{ width: '300px' }}
        />
        <CButton color="primary" onClick={handleCreateNew}>
          Create New Prompt
        </CButton>
        <NewPromptTemplateModal
          show={showModal}
          onClose={() => setShowModal(false)}
          onSave={handleSaveTemplate}
        />
      </div>
      <CTable bordered>
        <CTableHead>
          <CTableRow>
            <CTableHeaderCell>Name</CTableHeaderCell>
            <CTableHeaderCell>Template Type</CTableHeaderCell>
            <CTableHeaderCell>Category</CTableHeaderCell>
            <CTableHeaderCell>Status</CTableHeaderCell>
            <CTableHeaderCell>Date Modified</CTableHeaderCell>
          </CTableRow>
        </CTableHead>
        <CTableBody>
          {filteredTemplates.map((template, index) => (
            <CTableRow key={index}>
              <CTableDataCell>{template.name}</CTableDataCell>
              <CTableDataCell>{template.templateType}</CTableDataCell>
              <CTableDataCell>{template.category}</CTableDataCell>
              <CTableDataCell>{template.status}</CTableDataCell>
              <CTableDataCell>{template.modified}</CTableDataCell>
            </CTableRow>
          ))}
        </CTableBody>
      </CTable>
    </div>
  );
};

export default PromptBuilder;
