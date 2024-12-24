import React, { useState, useEffect } from 'react';
import {
  CModal,
  CModalHeader,
  CModalBody,
  CModalFooter,
  CForm,
  CFormLabel,
  CFormInput,
  CFormSelect,
  CButton,
} from '@coreui/react';
import axios from 'axios';

const CreateAgentActionModal = ({ show, onClose, onSave }) => {
  const [referenceActionTypes, setReferenceActionTypes] = useState([]);
  const [referenceActions, setReferenceActions] = useState([]);
  const [formData, setFormData] = useState({
    referenceActionType: '',
    referenceAction: '',
    agentActionLabel: '',
    agentActionAPIName: '',
  });

  // Fetch Reference Action Types when the modal is shown
  useEffect(() => {
    if (show) {
      const fetchReferenceActionTypes = async () => {
        try {
          // Uncomment the below line if the backend is ready
          // const response = await axios.get('/api/reference-action-types');
          // Replace this mock data with `response.data` if backend is available
          const mockReferenceActionTypes = [
            { type: 'Prompt Template' },
            { type: 'Order' },
          ];
          setReferenceActionTypes(mockReferenceActionTypes);
        } catch (error) {
          console.error('Error fetching reference action types:', error);
          setReferenceActionTypes([]);
        }
      };

      fetchReferenceActionTypes();
    }
  }, [show]);

  // Fetch Reference Actions based on selected Reference Action Type
  useEffect(() => {
    if (formData.referenceActionType) {
      const fetchReferenceActions = async () => {
        try {
          // Uncomment the below line if the backend is ready
          // const response = await axios.post('/api/reference-actions', {
          //   actionType: formData.referenceActionType,
          // });
          // Replace this mock data with `response.data` if backend is available
          const mockReferenceActions = {
            'Prompt Template': [
              { id: 1, name: 'Create Prompt Template' },
              { id: 2, name: 'Edit Prompt Template' },
            ],
            Order: [
              { id: 3, name: 'Create Order' },
              { id: 4, name: 'Cancel Order' },
            ],
          };
          setReferenceActions(mockReferenceActions[formData.referenceActionType] || []);
        } catch (error) {
          console.error('Error fetching reference actions:', error);
          setReferenceActions([]);
        }
      };

      fetchReferenceActions();
    } else {
      setReferenceActions([]);
    }
  }, [formData.referenceActionType]);

  // Handle form input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'referenceAction'
        ? {
            agentActionLabel: value, // Default Agent Action Label
            agentActionAPIName: value.replace(/\s+/g, '_').toLowerCase(), // Default Agent Action API Name
          }
        : {}),
    }));
  };

  // Handle form submission
  const handleSubmit = async () => {
    if (
      !formData.referenceActionType ||
      !formData.referenceAction ||
      !formData.agentActionLabel ||
      !formData.agentActionAPIName
    ) {
      alert('All fields are required!');
      return;
    }

    try {
      // Uncomment the below line if the backend is ready
      // await axios.post('/api/create-agent-action', formData);
      console.log('Form submitted successfully:', formData); // For testing without backend
      alert('Agent Action created successfully!');
      onSave(); // Callback to refresh data
      onClose(); // Close the modal
    } catch (error) {
      console.error('Error creating agent action:', error);
      alert('Failed to create agent action!');
    }
  };

  return (
    <CModal visible={show} onClose={onClose}>
      <CModalHeader>
        <h5>Create an Agent Action</h5>
      </CModalHeader>
      <CModalBody>
        <CForm>
          {/* Reference Action Type */}
          <div className="mb-3">
            <CFormLabel>Reference Action Type</CFormLabel>
            <CFormSelect
              name="referenceActionType"
              value={formData.referenceActionType}
              onChange={handleChange}
              required
            >
              <option value="">Select Reference Action Type</option>
              {referenceActionTypes.map((type, index) => (
                <option key={index} value={type.type}>
                  {type.type}
                </option>
              ))}
            </CFormSelect>
          </div>

          {/* Reference Action */}
          <div className="mb-3">
            <CFormLabel>Reference Action</CFormLabel>
            <CFormSelect
              name="referenceAction"
              value={formData.referenceAction}
              onChange={handleChange}
              disabled={!formData.referenceActionType}
              required
            >
              <option value="">Select Reference Action</option>
              {referenceActions.map((action, index) => (
                <option key={index} value={action.name}>
                  {action.name}
                </option>
              ))}
            </CFormSelect>
          </div>

          {/* Agent Action Label */}
          <div className="mb-3">
            <CFormLabel>Agent Action Label</CFormLabel>
            <CFormInput
              name="agentActionLabel"
              value={formData.agentActionLabel}
              onChange={handleChange}
              required
            />
          </div>

          {/* Agent Action API Name */}
          <div className="mb-3">
            <CFormLabel>Agent Action API Name</CFormLabel>
            <CFormInput
              name="agentActionAPIName"
              value={formData.agentActionAPIName}
              onChange={handleChange}
              required
            />
          </div>
        </CForm>
      </CModalBody>
      <CModalFooter>
        <CButton color="secondary" onClick={onClose}>
          Cancel
        </CButton>
        <CButton color="primary" onClick={handleSubmit}>
          Save
        </CButton>
      </CModalFooter>
    </CModal>
  );
};

export default CreateAgentActionModal;
