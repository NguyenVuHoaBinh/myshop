import React from 'react';
import { Handle, Position } from '@xyflow/react';

const LLMNode = ({ data }) => {
  const { templateId, selectedTemplate, showConversation, llmconfig } = data;

  // Use the node's llmconfig if available, otherwise fallback to selectedTemplate.llmconfig
  const effectiveLLMConfig = llmconfig || (selectedTemplate && selectedTemplate.llmconfig);

  return (
    <div className="custom-node llm-node">
      <strong>LLM Node</strong>
      <div>
        {templateId ? (
          <>
            <p>Selected Template:</p>
            <p>
              <strong>{selectedTemplate?.name || 'Template Not Found'}</strong>
            </p>
          </>
        ) : (
          <p>No template selected</p>
        )}
      </div>
      {effectiveLLMConfig ? (
        <div className="llm-config">
          <p>AI Model: {effectiveLLMConfig.aiModel}</p>
          <p>Temperature: {effectiveLLMConfig.temperature}</p>
          <p>Max Tokens: {effectiveLLMConfig.max_tokens}</p>
          <p>Stream: {effectiveLLMConfig.stream ? 'Yes' : 'No'}</p>
        </div>
      ) : (
        <p>No LLM configuration provided.</p>
      )}
      {showConversation && (
        <div className="conversation">
          <p>Conversation will be displayed here...</p>
        </div>
      )}
      <Handle type="target" position={Position.Top} />
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default LLMNode;
