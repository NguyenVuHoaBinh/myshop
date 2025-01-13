import React, { useState, useEffect } from 'react';
import Draggable from 'react-draggable';

const Sidebar = ({ selectedNode, onUpdateNodeData, templates = [] }) => {
  const [name, setName] = useState('');
  const [label, setLabel] = useState('');
  const [botResponse, setBotResponse] = useState('');
  const [inputType, setInputType] = useState('text');
  const [options, setOptions] = useState([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState('');

  useEffect(() => {
    if (selectedNode) {
      const { name, label, botResponse, inputType, options, templateId } = selectedNode.data || {};
      setName(name || '');
      setLabel(label || '');
      setBotResponse(botResponse || '');
      setInputType(inputType || 'text');
      setOptions(options || []);
      setSelectedTemplateId(templateId || '');
    }
  }, [selectedNode]);

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
      data: { ...selectedNode.data, inputType: type, options: type === 'text' ? [] : options },
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
    const selectedTemplate = templates.find((template) => template.id === templateId);
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

  if (!selectedNode) {
    return (
      <Draggable>
        <div
          style={{
            width: '300px',
            padding: '10px',
            borderRight: '1px solid #ccc',
            background: '#f9f9f9',
          }}
        >
          <p>Select a node to edit properties.</p>
        </div>
      </Draggable>
    );
  }

  return (
    <Draggable>
      <div
        style={{
          width: '300px',
          padding: '10px',
          borderRight: '1px solid #ccc',
          background: '#f9f9f9',
        }}
      >
        <h4>Node Properties</h4>
        <div>
          <label>Name:</label>
          <input
            type="text"
            value={name}
            onChange={(e) => handleNameChange(e.target.value)}
            placeholder="Enter node name..."
          />
        </div>
        <div>
          <label>Label:</label>
          <input
            type="text"
            value={label}
            onChange={(e) => handleLabelChange(e.target.value)}
            placeholder="Enter node label..."
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
              />
            </div>
            <div>
              <label>Input Type:</label>
              <select
                value={inputType}
                onChange={(e) => handleInputTypeChange(e.target.value)}
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
                  <div key={index} style={{ display: 'flex', alignItems: 'center' }}>
                    <input
                      type="text"
                      value={option}
                      onChange={(e) => handleOptionsChange(index, e.target.value)}
                      placeholder={`Option ${index + 1}`}
                    />
                    <button onClick={() => removeOption(index)}>Remove</button>
                  </div>
                ))}
                <button onClick={addOption}>Add Option</button>
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
      </div>
    </Draggable>
  );
};

export default Sidebar;
