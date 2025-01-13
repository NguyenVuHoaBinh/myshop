import React from 'react';
import { Handle, Position } from '@xyflow/react';

const LLMNode = ({ data }) => {
  const { templateId, selectedTemplate } = data;

  return (
    <div className="custom-node llm-node">
      <strong>LLM Node</strong>
      <div>
        {templateId ? (
          <>
            <p>Selected Template:</p>
            <p><strong>{selectedTemplate?.name || 'Template Not Found'}</strong></p>
          </>
        ) : (
          <p>No template selected</p>
        )}
      </div>
      <Handle type="target" position={Position.Top} />
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default LLMNode;