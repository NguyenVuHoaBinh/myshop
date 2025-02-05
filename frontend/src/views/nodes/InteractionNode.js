import React from 'react';
import { Handle, Position } from '@xyflow/react';

const InteractionNode = ({ data }) => {
  return (
    <div className="custom-node interaction-node">
      <strong>{data.label || 'Interaction Node'}</strong>
      <p>{data.botResponse || 'No bot response defined.'}</p>
      <div>
        {data.inputType === 'text' && (
          <input type="text" placeholder="User input..." disabled />
        )}
        {data.inputType === 'dropdown' && (
          <select disabled>
            {data.options?.map((option, index) => (
              <option key={index} value={option}>
                {option}
              </option>
            ))}
          </select>
        )}
        {data.inputType === 'radio' && (
          <div>
            {data.options?.map((option, index) => (
              <label key={index}>
                <input type="radio" disabled /> {option}
              </label>
            ))}
          </div>
        )}
      </div>
      {/* Ensure handles are correctly positioned for floating edges */}
      <Handle
        type="target"
        position={Position.Top}
        style={{ background: '#555' }}
      />
      <Handle
        type="source"
        position={Position.Bottom}
        style={{ background: '#555' }}
      />
    </div>
  );
};

export default InteractionNode;
