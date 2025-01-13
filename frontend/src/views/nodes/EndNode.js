import React from 'react';
import { Handle, Position } from '@xyflow/react';

const EndNode = () => (
  <div className="custom-node end-node">
    <strong>End Node</strong>
    {/* Only target handle, no source handle */}
    <Handle type="target" position={Position.Top} />
  </div>
);

export default EndNode;