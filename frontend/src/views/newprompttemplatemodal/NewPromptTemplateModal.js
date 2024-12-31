import React, { useState, useEffect } from "react";
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
} from "@coreui/react";
import { useNavigate } from "react-router-dom";

const NewPromptTemplateModal = ({ show, onClose }) => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    templateType: "",
    name: "",
    apiName: "", // API Name (ID) will be renamed to "id" in the payload
    description: "",
    object: "",
    objectField: "",
    aiModel: "OpenAI", // Default AI Model
  });

  const [templateTypes, setTemplateTypes] = useState([]);
  const [objects, setObjects] = useState([]);
  const [fields, setFields] = useState([]);
  const [loading, setLoading] = useState(false);

  // Fetch dropdown data
  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const templateTypeResponse = await fetch(
          "http://localhost:8888/api/templates/field-options"
        );
        const objectResponse = await fetch("http://localhost:8888/api/tables");

        const templateTypes = await templateTypeResponse.json();
        const objects = await objectResponse.json();

        setTemplateTypes(templateTypes);
        setObjects(objects);
      } catch (error) {
        console.error("Error fetching dropdown data:", error);
      }
    };

    fetchDropdownData();
  }, []);

  // Update fields dropdown when object changes
  useEffect(() => {
    const selectedObject = objects.find(
      (object) => object.objectName === formData.object
    );
    setFields(selectedObject?.fields || []);
  }, [formData.object, objects]);

  // Handle form changes
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Handle Next button click
  const handleNext = async () => {
    try {
      setLoading(true);

      // Transform formData for the API payload
      const payload = {
        type: formData.templateType,
        name: formData.name,
        id: formData.apiName || formData.name.replace(/\s+/g, "-").toLowerCase(),
        description: formData.description,
        object: formData.object,
        objectField: formData.objectField,
        aiModel: formData.aiModel,
      };

      // Send POST request to create the new template
      const response = await fetch("http://localhost:8888/api/templates", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error("Failed to create the template.");
      }

      const newTemplate = await response.json();

      // Navigate to the TemplateEditor with the new template ID
      navigate(`/template/${newTemplate.id}`);
    } catch (error) {
      console.error("Error creating template:", error);
      alert("Failed to create the template. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <CModal visible={show} onClose={onClose}>
      <CModalHeader closeButton>
        <h5>New Prompt Template</h5>
      </CModalHeader>
      <CModalBody>
        <CForm>
          {/* Template Type */}
          <div className="mb-3">
            <CFormLabel>Template Type</CFormLabel>
            <CFormSelect
              name="templateType"
              value={formData.templateType}
              onChange={handleChange}
            >
              <option value="">Select Template Type</option>
              {templateTypes.map((type, index) => (
                <option key={index} value={type}>
                  {type}
                </option>
              ))}
            </CFormSelect>
          </div>

          {/* Template Name */}
          <div className="mb-3">
            <CFormLabel>Template Name</CFormLabel>
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
            <CFormLabel>API Name (ID)</CFormLabel>
            <CFormInput
              name="apiName"
              value={formData.apiName}
              onChange={handleChange}
              placeholder="Enter API name or leave blank to auto-generate"
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
                <option key={index} value={object.objectName}>
                  {object.objectName}
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
              disabled={!fields.length}
            >
              <option value="">Select Object Field</option>
              {fields.map((field, index) => (
                <option key={index} value={field}>
                  {field}
                </option>
              ))}
            </CFormSelect>
          </div>
        </CForm>
      </CModalBody>
      <CModalFooter>
        <CButton color="secondary" onClick={onClose} disabled={loading}>
          Cancel
        </CButton>
        <CButton color="primary" onClick={handleNext} disabled={loading}>
          {loading ? "Processing..." : "Next"}
        </CButton>
      </CModalFooter>
    </CModal>
  );
};

export default NewPromptTemplateModal;
