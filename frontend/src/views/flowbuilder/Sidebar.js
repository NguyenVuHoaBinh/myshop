import React, { useState, useEffect } from 'react';
import Draggable from 'react-draggable';

// Assume defaultLLMConfig is imported or defined in this file.
const defaultLLMConfig = {
  aiModel: "gpt-4o",
  temperature: 0.7,
  max_tokens: 100,
  stream: false,
};

const Sidebar = ({
  selectedNode,
  onUpdateNodeData,
  templates = [],
  flowName,
  description,
  setFlowName,
  setDescription,
}) => {
  // Node-specific state
  const [name, setName] = useState('');
  const [label, setLabel] = useState('');
  const [botResponse, setBotResponse] = useState('');
  const [inputType, setInputType] = useState('text');
  const [options, setOptions] = useState([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [showConversation, setShowConversation] = useState(false);

  // Synchronize the node's data with the state
  useEffect(() => {
    if (selectedNode) {
      const {
        name,
        label,
        botResponse,
        inputType,
        options,
        templateId,
        showConversation,
      } = selectedNode.data || {};
      setName(name || '');
      setLabel(label || '');
      setBotResponse(botResponse || '');
      setInputType(inputType || 'text');
      setOptions(options || []);
      setSelectedTemplateId(templateId || '');
      setShowConversation(showConversation || false);
    }
  }, [selectedNode]);

  // Handler to update node data while preserving existing fields.
  const updateNodeField = (updatedData) => {
    onUpdateNodeData({
      ...selectedNode,
      data: {
        ...selectedNode.data,
        ...updatedData,
      },
    });
  };

  // Handle template selection: update node data with selected template
  // and copy its LLM configuration (if available); otherwise, use the default.
  const handleTemplateChange = (templateId) => {
    setSelectedTemplateId(templateId);
    const selectedTemplate = templates.find(
      (template) => template.id === templateId
    );
    const newLLMConfig = (selectedTemplate && selectedTemplate.llmconfig) || defaultLLMConfig;
    updateNodeField({
      templateId,
      selectedTemplate,
      llmconfig: newLLMConfig,
    });
  };

  // Update showConversation without resetting other node data.
  const handleShowConversationChange = (value) => {
    setShowConversation(value);
    updateNodeField({
      showConversation: value,
    });
  };

  // ... (other handlers remain unchanged)

  const addOption = () => {
    const newOptions = [...options, ''];
    setOptions(newOptions);
    updateNodeField({ options: newOptions });
  };

  const removeOption = (index) => {
    const newOptions = options.filter((_, i) => i !== index);
    setOptions(newOptions);
    updateNodeField({ options: newOptions });
  };

  return (
    <Draggable>
      <div
        style={{
          width: '300px',
          padding: '10px',
          borderRight: '1px solid #ccc',
          background: '#f9f9f9',
          height: '100%',
          overflowY: 'auto',
        }}
      >
        {/* Flow-Level Metadata Section */}
        <h4>Flow Metadata</h4>
        <div style={{ marginBottom: '10px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>Flow Name:</label>
          <input
            type="text"
            value={flowName}
            onChange={(e) => setFlowName(e.target.value)}
            placeholder="Enter flow name..."
            style={{
              width: '100%',
              padding: '5px',
              fontSize: '14px',
              marginBottom: '10px',
            }}
          />
        </div>
        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', marginBottom: '5px' }}>Flow Description:</label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Enter flow description..."
            style={{
              width: '100%',
              height: '60px',
              padding: '5px',
              fontSize: '14px',
            }}
          />
        </div>

        {/* Node-Level Properties Section */}
        {selectedNode ? (
          <>
            <h4>Node Properties</h4>
            <div>
              <label>Name:</label>
              <input
                type="text"
                value={name}
                onChange={(e) => updateNodeField({ name: e.target.value })}
                placeholder="Enter node name..."
                style={{
                  width: '100%',
                  padding: '5px',
                  fontSize: '14px',
                  marginBottom: '10px',
                }}
              />
            </div>
            <div>
              <label>Label:</label>
              <input
                type="text"
                value={label}
                onChange={(e) => updateNodeField({ label: e.target.value })}
                placeholder="Enter node label..."
                style={{
                  width: '100%',
                  padding: '5px',
                  fontSize: '14px',
                  marginBottom: '10px',
                }}
              />
            </div>
            {selectedNode.type === 'interactionNode' && (
              <>
                <div>
                  <label>Bot Response:</label>
                  <textarea
                    value={botResponse}
                    onChange={(e) => updateNodeField({ botResponse: e.target.value })}
                    placeholder="Enter bot response..."
                    style={{
                      width: '100%',
                      height: '60px',
                      padding: '5px',
                      fontSize: '14px',
                      marginBottom: '10px',
                    }}
                  />
                </div>
                <div>
                  <label>Input Type:</label>
                  <select
                    value={inputType}
                    onChange={(e) => updateNodeField({ inputType: e.target.value })}
                    style={{
                      width: '100%',
                      padding: '5px',
                      fontSize: '14px',
                      marginBottom: '10px',
                    }}
                  >
                    <option value="text">Text</option>
                    <option value="dropdown">Dropdown</option>
                    <option value="radio">Radio Buttons</option>
                  </select>
                </div>
                {(inputType === 'dropdown' || inputType === 'radio') && (
                  <div>
                    <label>Options:</label>
                    {options.map((option, index) => (
                      <div
                        key={index}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          marginBottom: '5px',
                        }}
                      >
                        <input
                          type="text"
                          value={option}
                          onChange={(e) => handleOptionsChange(index, e.target.value)}
                          placeholder={`Option ${index + 1}`}
                          style={{
                            flex: '1',
                            padding: '5px',
                            fontSize: '14px',
                          }}
                        />
                        <button
                          onClick={() => removeOption(index)}
                          style={{
                            marginLeft: '5px',
                            padding: '5px',
                            cursor: 'pointer',
                          }}
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                    <button
                      onClick={addOption}
                      style={{
                        padding: '5px',
                        cursor: 'pointer',
                        marginTop: '5px',
                      }}
                    >
                      Add Option
                    </button>
                  </div>
                )}
              </>
            )}
            {selectedNode.type === 'llmNode' && (
              <>
                <div>
                  <label htmlFor="template-select">Choose Template:</label>
                  <select
                    id="template-select"
                    value={selectedTemplateId}
                    onChange={(e) => handleTemplateChange(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '5px',
                      fontSize: '14px',
                      marginBottom: '10px',
                    }}
                  >
                    <option value="">Select a template</option>
                    {templates.map((template) => (
                      <option key={template.id} value={template.id}>
                        {template.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div style={{ marginBottom: '10px' }}>
                  <input
                    type="checkbox"
                    id="showConversation"
                    checked={showConversation}
                    onChange={(e) => handleShowConversationChange(e.target.checked)}
                  />
                  <label htmlFor="showConversation" style={{ marginLeft: '5px' }}>
                    Show Conversation
                  </label>
                </div>
              </>
            )}
          </>
        ) : (
          <p>Select a node to edit properties.</p>
        )}
      </div>
    </Draggable>
  );
};

export default Sidebar;
