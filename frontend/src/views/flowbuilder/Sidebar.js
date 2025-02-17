import React, { useState, useEffect } from 'react';
import Draggable from 'react-draggable';

// Example default LLM config (adjust to your needs)
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
  // --------------------------------
  // Flow-Level State
  // --------------------------------
  // Keep these controlled from props to demonstrate local usage is optional
  // If you also have the single-char issue here, you can do a local approach as well.

  // Node-level local states:
  // We use local states for text fields so they aren't overwritten on each keypress re-render
  const [localName, setLocalName] = useState('');
  const [localLabel, setLocalLabel] = useState('');
  const [localBotResponse, setLocalBotResponse] = useState('');

  const [inputType, setInputType] = useState('text');
  const [options, setOptions] = useState([]);
  const [selectedTemplateId, setSelectedTemplateId] = useState('');
  const [showConversation, setShowConversation] = useState(false);

  useEffect(() => {
    if (selectedNode) {
      // Destructure the node data
      const {
        name,
        label,
        botResponse,
        inputType,
        options,
        templateId,
        showConversation,
      } = selectedNode.data || {};

      // Update our local states with these values
      setLocalName(name || '');
      setLocalLabel(label || '');
      setLocalBotResponse(botResponse || '');
      setInputType(inputType || 'text');
      setOptions(options || []);
      setSelectedTemplateId(templateId || '');
      setShowConversation(showConversation || false);
    } else {
      // If no node is selected, reset local states
      setLocalName('');
      setLocalLabel('');
      setLocalBotResponse('');
      setInputType('text');
      setOptions([]);
      setSelectedTemplateId('');
      setShowConversation(false);
    }
  }, [selectedNode]);

  // A helper to fully update the node's data in the flow store
  // while preserving fields that we haven't touched locally.
  const updateNodeField = (updatedFields) => {
    if (!selectedNode) return;
    onUpdateNodeData({
      ...selectedNode,
      data: {
        ...selectedNode.data,
        ...updatedFields,
      },
    });
  };

  // Called after finishing editing text fields (onBlur) to persist to store
  const commitName = () => {
    updateNodeField({ name: localName });
  };
  const commitLabel = () => {
    updateNodeField({ label: localLabel });
  };
  const commitBotResponse = () => {
    updateNodeField({ botResponse: localBotResponse });
  };

  // For changing inputType, we can update store immediately
  const handleInputTypeChange = (newType) => {
    setInputType(newType);
    updateNodeField({ inputType: newType });
  };

  // Updating showConversation
  const handleShowConversationChange = (value) => {
    setShowConversation(value);
    updateNodeField({ showConversation: value });
  };

  // Handle template selection
  const handleTemplateChange = (templateId) => {
    setSelectedTemplateId(templateId);
    const selectedTemplate = templates.find(
      (template) => template.id === templateId
    );
    const newLLMConfig =
      (selectedTemplate && selectedTemplate.llmconfig) || defaultLLMConfig;

    updateNodeField({
      templateId,
      selectedTemplate,
      llmconfig: newLLMConfig,
    });
  };

  // For multi-option input types (dropdown/radio)
  const handleOptionsChange = (index, value) => {
    const newOptions = [...options];
    newOptions[index] = value;
    setOptions(newOptions);
    updateNodeField({ options: newOptions });
  };

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
      <div
        style={{
          width: '400px',
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
              {/* Use localName so we can type freely */}
              <input
                type="text"
                value={localName}
                onChange={(e) => setLocalName(e.target.value)}
                onBlur={commitName} // commit to store on blur
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
                value={localLabel}
                onChange={(e) => setLocalLabel(e.target.value)}
                onBlur={commitLabel} // commit to store on blur
                placeholder="Enter node label..."
                style={{
                  width: '100%',
                  padding: '5px',
                  fontSize: '14px',
                  marginBottom: '10px',
                }}
              />
            </div>

            {/* If this is an interactionNode, show related fields */}
            {selectedNode.type === 'interactionNode' && (
              <>
                <div>
                  <label>Bot Response:</label>
                  <textarea
                    value={localBotResponse}
                    onChange={(e) => setLocalBotResponse(e.target.value)}
                    onBlur={commitBotResponse} // commit on blur
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

            {/* If this is an LLM node, show template selection */}
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
  );
};

export default Sidebar;
