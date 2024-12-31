import React, { useState, useEffect } from "react";
import {
  CTable,
  CTableBody,
  CTableHead,
  CTableHeaderCell,
  CTableRow,
  CTableDataCell,
  CFormInput,
  CDropdown,
  CDropdownToggle,
  CDropdownMenu,
  CDropdownItem,
  CButton,
} from "@coreui/react";
import { format } from "date-fns";
import { useNavigate } from "react-router-dom";
import NewPromptTemplateModal from "../newprompttemplatemodal/NewPromptTemplateModal";
import PromptTemplateConfig from "../newprompttemplatemodal/PromptTemplateConfig";

const PromptBuilder = () => {
  const [templates, setTemplates] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [showNewPromptModal, setShowNewPromptModal] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const [isConfigMode, setIsConfigMode] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    try {
      const response = await fetch("http://localhost:8888/api/templates"); // Replace with your API endpoint
      const data = await response.json();
      setTemplates(data.content || []);
    } catch (error) {
      console.error("Error fetching templates:", error);
    }
  };

  const handleSearch = (event) => {
    setSearchQuery(event.target.value);
  };

  const handleEdit = (templateId) => {
    // Navigate to the edit page
    navigate(`/template/${templateId}`);
  };

  const handleDelete = async (templateId) => {
    if (window.confirm("Are you sure you want to delete this template?")) {
      try {
        await fetch(`http://localhost:8888/api/templates/${templateId}`, {
          method: "DELETE",
        });
        setTemplates(templates.filter((template) => template.id !== templateId));
      } catch (error) {
        console.error("Error deleting template:", error);
      }
    }
  };

  const handleCreateNew = () => {
    setShowNewPromptModal(true);
  };

  const handleSaveNewTemplate = (newTemplateData) => {
    // Add the new template to the templates list
    setTemplates([...templates, newTemplateData]);
    setShowNewPromptModal(false);
    setSelectedTemplate(newTemplateData); // Pass the data to the config view
    setIsConfigMode(true);
  };

  const handleConfigSave = () => {
    alert("Template configuration saved successfully!");
    setIsConfigMode(false);
  };

  const filteredTemplates = templates.filter((template) =>
    template.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formattedDate = (date) =>
    date ? format(new Date(date), "yyyy-MM-dd") : "N/A";

  if (isConfigMode && selectedTemplate) {
    return (
      <PromptTemplateConfig
        templateData={selectedTemplate}
        onSave={handleConfigSave}
      />
    );
  }

  return (
    <div className="container-fluid mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1>Prompt Templates</h1>
        <div className="d-flex align-items-center">
          <CFormInput
            type="text"
            placeholder="Search templates..."
            value={searchQuery}
            onChange={handleSearch}
            style={{ width: "300px", marginRight: "10px" }}
          />
          <CButton color="primary" onClick={handleCreateNew}>
            Create New Prompt Builder
          </CButton>
        </div>
      </div>
      <CTable hover responsive>
        <CTableHead>
          <CTableRow>
            <CTableHeaderCell>ID</CTableHeaderCell>
            <CTableHeaderCell>Name</CTableHeaderCell>
            <CTableHeaderCell>Type</CTableHeaderCell>
            <CTableHeaderCell>Description</CTableHeaderCell>
            <CTableHeaderCell>Created At</CTableHeaderCell>
            <CTableHeaderCell>Updated At</CTableHeaderCell>
            <CTableHeaderCell>Actions</CTableHeaderCell>
          </CTableRow>
        </CTableHead>
        <CTableBody>
          {filteredTemplates.map((template) => (
            <CTableRow key={template.id}>
              <CTableDataCell>{template.id}</CTableDataCell>
              <CTableDataCell>{template.name}</CTableDataCell>
              <CTableDataCell>{template.type}</CTableDataCell>
              <CTableDataCell>{template.description}</CTableDataCell>
              <CTableDataCell>{formattedDate(template.createdAt)}</CTableDataCell>
              <CTableDataCell>{formattedDate(template.updatedAt)}</CTableDataCell>
              <CTableDataCell>
                <CDropdown>
                  <CDropdownToggle color="secondary">Actions</CDropdownToggle>
                  <CDropdownMenu>
                    <CDropdownItem onClick={() => handleEdit(template.id)}>
                      Edit
                    </CDropdownItem>
                    <CDropdownItem onClick={() => handleDelete(template.id)}>
                      Delete
                    </CDropdownItem>
                  </CDropdownMenu>
                </CDropdown>
              </CTableDataCell>
            </CTableRow>
          ))}
        </CTableBody>
      </CTable>
      <NewPromptTemplateModal
        show={showNewPromptModal}
        onClose={() => setShowNewPromptModal(false)}
        onSave={handleSaveNewTemplate}
      />
    </div>
  );
};

export default PromptBuilder;
