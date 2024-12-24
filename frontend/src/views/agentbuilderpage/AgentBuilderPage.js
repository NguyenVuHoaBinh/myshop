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
  CModal,
  CModalHeader,
  CModalBody,
  CModalFooter,
  CForm,
  CFormLabel,
  CFormSelect,
  CFormTextarea,
  CDropdown,
  CDropdownToggle,
  CDropdownMenu,
  CDropdownItem,
  CCardHeader,
} from '@coreui/react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const AgentBuilderPage = ({ navigateToCreateAgent, navigateToAgentBuilderModal }) => {
  const [agents, setAgents] = useState([]); // Agents list
  const [searchTerm, setSearchTerm] = useState(''); // Search input
  const [showEditModal, setShowEditModal] = useState(false); // Edit modal state
  const [editAgent, setEditAgent] = useState(null); // Current agent being edited
  const navigate = useNavigate();

  // Fetch agents on component mount
  useEffect(() => {
    const fetchAgents = async () => {
      try {
        const response = await axios.get('/api/agents');
        setAgents(Array.isArray(response.data) ? response.data : getMockData());
      } catch (error) {
        console.error('Error fetching agents, using mock data:', error);
        setAgents(getMockData());
      }
    };
    fetchAgents();
  }, []);

  // Mock data fallback
  const getMockData = () => [
    {
      id: 1,
      name: 'Einstein Copilot',
      type: 'Copilot',
      description: 'An AI assistant for in-org business tasks.',
      active: true,
      createdBy: 'Admin',
      lastModified: 'Nov 19, 2024',
    },
    {
      id: 2,
      name: 'Service Assistant',
      type: 'Service Agent',
      description: 'Helps with customer queries and escalations.',
      active: false,
      createdBy: 'Admin',
      lastModified: 'Nov 20, 2024',
    },
  ];

  // Filter agents based on search term
  const filteredAgents = agents.filter((agent) =>
    agent.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Handle opening the edit modal
  const handleEdit = (agent) => {
    setEditAgent(agent);
    setShowEditModal(true);
  };

  // Handle deleting an agent
  const handleDelete = async (agentId) => {
    if (window.confirm('Are you sure you want to delete this agent?')) {
      try {
        await axios.delete(`/api/agents/${agentId}`);
        setAgents((prev) => prev.filter((agent) => agent.id !== agentId));
        alert('Agent deleted successfully!');
      } catch (error) {
        console.error('Error deleting agent:', error);
        alert('Failed to delete agent.');
      }
    }
  };

  // Handle saving an edited agent
  const handleSaveEdit = async () => {
    if (!editAgent) return;

    try {
      await axios.put(`/api/agents/${editAgent.id}`, editAgent);
      setAgents((prev) =>
        prev.map((agent) => (agent.id === editAgent.id ? { ...editAgent } : agent))
      );
      setShowEditModal(false);
      alert('Agent updated successfully!');
    } catch (error) {
      console.error('Error updating agent:', error);
      alert('Failed to update agent.');
    }
  };

  // Close the edit modal
  const closeEditModal = () => {
    setShowEditModal(false);
    setEditAgent(null);
  };

  return (
    <div className="container mt-4">
      {/* Header Section */}
      <div className="d-flex justify-content-between align-items-center mb-3">
        <CCardHeader>
          <h1>Agent Builder</h1>
          <strong>All Agents</strong>
        </CCardHeader>
        <CInputGroup>
          <CFormInput
            placeholder="Search agents..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </CInputGroup>
        <CButton
          color="primary"
          onClick={() => {
            navigateToCreateAgent();
            navigate('/createagent');
          }}
        >
          + Create New Agent
        </CButton>
      </div>

      {/* Agents Table */}
      <CTable bordered>
        <CTableHead>
          <CTableRow>
            <CTableHeaderCell>Agent Name</CTableHeaderCell>
            <CTableHeaderCell>Type</CTableHeaderCell>
            <CTableHeaderCell>Description</CTableHeaderCell>
            <CTableHeaderCell>Active</CTableHeaderCell>
            <CTableHeaderCell>Created By</CTableHeaderCell>
            <CTableHeaderCell>Last Modified</CTableHeaderCell>
            <CTableHeaderCell>Actions</CTableHeaderCell>
          </CTableRow>
        </CTableHead>
        <CTableBody>
          {filteredAgents.map((agent) => (
            <CTableRow key={agent.id}>
              <CTableDataCell>{agent.name}</CTableDataCell>
              <CTableDataCell>{agent.type}</CTableDataCell>
              <CTableDataCell>{agent.description}</CTableDataCell>
              <CTableDataCell>{agent.active ? '✔' : '✖'}</CTableDataCell>
              <CTableDataCell>{agent.createdBy}</CTableDataCell>
              <CTableDataCell>{agent.lastModified}</CTableDataCell>
              <CTableDataCell>
                <CDropdown>
                  <CDropdownToggle color="secondary">Actions</CDropdownToggle>
                  <CDropdownMenu>
                    <CDropdownItem onClick={() => handleEdit(agent)}>Edit</CDropdownItem>
                    <CDropdownItem
                      onClick={() => handleDelete(agent.id)}
                      style={{ color: 'red' }}
                    >
                      Delete
                    </CDropdownItem>
                    <CDropdownItem
                    onClick={() => {
                        console.log('Opening Agent Builder Modal...');
                        navigateToAgentBuilderModal(); // Trigger the modal state update
                        navigate('/createagentbuildermodal')

                    }}
                    >
                    Open in Builder
                    </CDropdownItem>

                  </CDropdownMenu>
                </CDropdown>
              </CTableDataCell>
            </CTableRow>
          ))}
        </CTableBody>
      </CTable>

      {/* Edit Modal */}
      {editAgent && (
        <CModal visible={showEditModal} onClose={closeEditModal}>
          <CModalHeader>
            <h5>Edit Agent</h5>
          </CModalHeader>
          <CModalBody>
            <CForm>
              <div className="mb-3">
                <CFormLabel>Agent Name</CFormLabel>
                <CFormInput
                  name="name"
                  value={editAgent.name}
                  onChange={(e) =>
                    setEditAgent({ ...editAgent, name: e.target.value })
                  }
                  required
                />
              </div>
              <div className="mb-3">
                <CFormLabel>Type</CFormLabel>
                <CFormSelect
                  name="type"
                  value={editAgent.type}
                  onChange={(e) =>
                    setEditAgent({ ...editAgent, type: e.target.value })
                  }
                  required
                >
                  <option value="">Select Type</option>
                  <option value="Copilot">Copilot</option>
                  <option value="Service Agent">Service Agent</option>
                  <option value="Digital Channel">Digital Channel</option>
                </CFormSelect>
              </div>
              <div className="mb-3">
                <CFormLabel>Description</CFormLabel>
                <CFormTextarea
                  name="description"
                  value={editAgent.description}
                  onChange={(e) =>
                    setEditAgent({ ...editAgent, description: e.target.value })
                  }
                  rows="3"
                  required
                />
              </div>
              <div className="mb-3">
                <CFormLabel>
                  <CFormInput
                    type="checkbox"
                    checked={editAgent.active}
                    onChange={(e) =>
                      setEditAgent({ ...editAgent, active: e.target.checked })
                    }
                  />{' '}
                  Active
                </CFormLabel>
              </div>
            </CForm>
          </CModalBody>
          <CModalFooter>
            <CButton color="secondary" onClick={closeEditModal}>
              Cancel
            </CButton>
            <CButton color="primary" onClick={handleSaveEdit}>
              Save
            </CButton>
          </CModalFooter>
        </CModal>
      )}
    </div>
  );
};

export default AgentBuilderPage;
