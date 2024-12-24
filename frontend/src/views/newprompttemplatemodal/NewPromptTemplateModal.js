import React, { useState, useEffect } from 'react';
import {
  CModal,
  CModalHeader,
  CModalBody,
  CModalFooter,
  CButton,
  CForm,
  CFormLabel,
  CFormInput,
  CFormSelect,
  CFormTextarea,
} from '@coreui/react';

const NewPromptTemplateModal = ({ show, onClose }) => {
  const [formData, setFormData] = useState({
    templateType: '',
    name: '',
    apiName: '',
    description: '',
    object: '',
    objectField: '',
  });

  const [templateTypes, setTemplateTypes] = useState([]);
  const [objects, setObjects] = useState([]);
  const [objectFields, setObjectFields] = useState([]);

  // Fetch options for Template Type, Object, and Object Field
  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const templateTypeResponse = await fetch('/api/template-types'); // Replace with API endpoint
        const objectResponse = await fetch('/api/objects'); // Replace with API endpoint
        const objectFieldResponse = await fetch('/api/object-fields'); // Replace with API endpoint

        const templateTypes = await templateTypeResponse.json();
        const objects = await objectResponse.json();
        const objectFields = await objectFieldResponse.json();

        setTemplateTypes(templateTypes);
        setObjects(objects);
        setObjectFields(objectFields);
      } catch (error) {
        console.error('Error fetching dropdown data:', error);
      }
    };

    fetchDropdownData();
  }, []);

  // Handle form changes
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = () => {
    // Post data to backend
    fetch('/api/templates', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData),
    })
      .then((response) => response.json())
      .then((data) => {
        alert('Template created successfully!');
        onClose();
      })
      .catch((error) => {
        console.error('Error creating template:', error);
      });
  };

  return (
    <CModal visible={show} onClose={onClose}>
      <CModalHeader closeButton>
        <h5>New Prompt Template</h5>
      </CModalHeader>
      <CModalBody>
        <CForm>
          {/* Prompt Template Type */}
          <div className="mb-3">
            <CFormLabel>Prompt Template Type</CFormLabel>
            <CFormSelect
              name="templateType"
              value={formData.templateType}
              onChange={handleChange}
            >
              <option value="">Select Template Type</option>
              {templateTypes.map((type, index) => (
                <option key={index} value={type.value}>
                  {type.label}
                </option>
              ))}
            </CFormSelect>
          </div>

          {/* Prompt Template Name */}
          <div className="mb-3">
            <CFormLabel>Prompt Template Name</CFormLabel>
            <CFormInput
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter template name"
              required
            />
          </div>

          {/* API Name */}
          <div className="mb-3">
            <CFormLabel>API Name</CFormLabel>
            <CFormInput
              name="apiName"
              value={formData.apiName}
              onChange={handleChange}
              placeholder="Enter API name"
              required
            />
          </div>

          {/* Template Description */}
          <div className="mb-3">
            <CFormLabel>Template Description</CFormLabel>
            <CFormTextarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows="3"
              placeholder="Enter description"
            />
          </div>

          {/* Object */}
          <div className="mb-3">
            <CFormLabel>Object</CFormLabel>
            <CFormSelect
              name="object"
              value={formData.object}
              onChange={handleChange}
            >
              <option value="">Select Object</option>
              {objects.map((object, index) => (
                <option key={index} value={object.value}>
                  {object.label}
                </option>
              ))}
            </CFormSelect>
          </div>

          {/* Object Field */}
          <div className="mb-3">
            <CFormLabel>Object Field</CFormLabel>
            <CFormSelect
              name="objectField"
              value={formData.objectField}
              onChange={handleChange}
            >
              <option value="">Select Object Field</option>
              {objectFields.map((field, index) => (
                <option key={index} value={field.value}>
                  {field.label}
                </option>
              ))}
            </CFormSelect>
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

export default NewPromptTemplateModal;
