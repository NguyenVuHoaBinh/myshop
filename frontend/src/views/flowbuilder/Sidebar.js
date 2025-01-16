import React, { useState, useEffect } from 'react';
import Draggable from 'react-draggable';

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

  // Synchronize the node's data with the state
  useEffect(() => {
    if (selectedNode) {
      const { name, label, botResponse, inputType, options, templateId } =
        selectedNode.data || {};
      setName(name || '');
      setLabel(label || '');
      setBotResponse(botResponse || '');
      setInputType(inputType || 'text');
      setOptions(options || []);
      setSelectedTemplateId(templateId || '');
    }
  }, [selectedNode]);

  // Handlers for node-specific fields
  const handleNameChange = (value) => {
    setName(value);
    onUpdateNodeData({
      ...selectedNode,
      data: { ...selectedNode.data, name: value },
    });
  };

  const handleLabelChange = (value) => {
    setLabel(value);
    onUpdateNodeData({
      ...selectedNode,
      data: { ...selectedNode.data, label: value },
    });
  };

  const handleBotResponseChange = (value) => {
    setBotResponse(value);
    onUpdateNodeData({
      ...selectedNode,
      data: { ...selectedNode.data, botResponse: value },
    });
  };

  const handleInputTypeChange = (type) => {
    setInputType(type);
    onUpdateNodeData({
      ...selectedNode,
      data: {
        ...selectedNode.data,
        inputType: type,
        options: type === 'text' ? [] : options,
      },
    });
  };

  const handleOptionsChange = (index, value) => {
    const newOptions = [...options];
    newOptions[index] = value;
    setOptions(newOptions);
    onUpdateNodeData({
      ...selectedNode,
      data: { ...selectedNode.data, options: newOptions },
    });
  };

  const handleTemplateChange = (templateId) => {
    setSelectedTemplateId(templateId);
    const selectedTemplate = templates.find(
      (template) => template.id === templateId
    );
    onUpdateNodeData({
      ...selectedNode,
      data: {
        ...selectedNode.data,
        templateId,
        selectedTemplate,
      },
    });
  };

  const addOption = () => {
    setOptions([...options, '']);
  };

  const removeOption = (index) => {
    const newOptions = options.filter((_, i) => i !== index);
    setOptions(newOptions);
    onUpdateNodeData({
      ...selectedNode,
      data: { ...selectedNode.data, options: newOptions },
    });
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
          <label style={{ display: 'block', marginBottom: '5px' }}>
            Flow Name:
          </label>
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
          <label style={{ display: 'block', marginBottom: '5px' }}>
            Flow Description:
          </label>
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
                onChange={(e) => handleNameChange(e.target.value)}
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
                onChange={(e) => handleLabelChange(e.target.value)}
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
                    onChange={(e) => handleBotResponseChange(e.target.value)}
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
                    onChange={(e) => handleInputTypeChange(e.target.value)}
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
                          onChange={(e) =>
                            handleOptionsChange(index, e.target.value)
                          }
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
                    onChange={(e) =>
                      handleTemplateChange(e.target.value)
                    }
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