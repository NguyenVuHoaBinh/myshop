import React, { useState, useEffect } from 'react';
import {
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
  CButton,
  CInputGroup,
  CFormInput,
  CCardHeader
} from '@coreui/react';
import axios from 'axios';
import CreateAgentActionModal from '../createagentactionmodal/CreateAgentActionModal';
const AgentAction = () => {
  const [actions, setActions] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [showModal, setShowModal] = useState(false);
  const handleSave = () => {
    // Refresh the agent actions table
  };

  // Fetch agent actions from the backend
  const fetchActions = async () => {
    try {
      const response = await axios.post('/api/agent-actions', {
        search: searchTerm,
      });
      setActions(response.data);
    } catch (error) {
      console.error('Error fetching agent actions:', error);
    }
  };

  // Handle search input change
  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  // Trigger fetch on search
  useEffect(() => {
    fetchActions();
  }, [searchTerm]);

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
      <CCardHeader>
            
            <h1>Agent Action</h1>
            <strong>All agent actions</strong>
        </CCardHeader>
        {/* Search bar */}
        <CInputGroup>
          <CFormInput
            placeholder="Search actions..."
            value={searchTerm}
            onChange={handleSearchChange}
          />
        </CInputGroup>
        <CButton color="primary" onClick={() => setShowModal(true)}>
        Create Agent Action
      </CButton>
      <CreateAgentActionModal
        show={showModal}
        onClose={() => setShowModal(false)}
        onSave={handleSave}
      />

        
      </div>

      {/* Agent Actions Table */}
      <CTable bordered>
        <CTableHead>
          <CTableRow>
            <CTableHeaderCell>Agent Action Label</CTableHeaderCell>
            <CTableHeaderCell>Instructions</CTableHeaderCell>
            <CTableHeaderCell>Source</CTableHeaderCell>
            <CTableHeaderCell>Reference Action Type</CTableHeaderCell>
            <CTableHeaderCell>Last Modified</CTableHeaderCell>
            <CTableHeaderCell>Created By</CTableHeaderCell>
          </CTableRow>
        </CTableHead>
        <CTableBody>
          {actions.map((action, index) => (
            <CTableRow key={index}>
              <CTableDataCell>{action.AgentActionLabel}</CTableDataCell>
              <CTableDataCell>{action.Instructions}</CTableDataCell>
              <CTableDataCell>{action.Source}</CTableDataCell>
              <CTableDataCell>{action.ReferenceActionType}</CTableDataCell>
              <CTableDataCell>{action.LastModified || '--'}</CTableDataCell>
              <CTableDataCell>{action.CreatedBy}</CTableDataCell>
            </CTableRow>
          ))}
        </CTableBody>
      </CTable>
    </div>
  );
};

export default AgentAction;
