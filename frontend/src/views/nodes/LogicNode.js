import React, { useState } from 'react';
import { Handle, Position } from '@xyflow/react';

const LogicNode = ({ data, onChange }) => {
  const [condition, setCondition] = useState(data.condition || '');

  const handleConditionChange = (e) => {
    const newCondition = e.target.value;
    setCondition(newCondition);

    // Call the onChange handler to update the node's data
    if (onChange) {
      onChange({ ...data, condition: newCondition });
    }
  };

  return (
    <div className="custom-node logic-node">
      <strong>Logic Node</strong>
      <div>
        <label>Condition:</label>
        <input
          type="text"
          value={condition}
          placeholder="Enter a condition, e.g., x > 10"
          onChange={handleConditionChange}
        />
        <p>Outputs:</p>
        <ul>
          <li><strong>True:</strong> Proceeds to the "True" path</li>
          <li><strong>False:</strong> Proceeds to the "False" path</li>
        </ul>
      </div>
      {/* Handles */}
      <Handle
        id="true"
        type="source"
        position={Position.Right}
        style={{ background: 'green' }}
      />
      <Handle
        id="false"
        type="source"
        position={Position.Left}
        style={{ background: 'red' }}
      />
      <Handle type="target" position={Position.Top} />
    </div>
  );
};

export default LogicNode;