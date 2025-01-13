import React from 'react';
import { Handle, Position } from '@xyflow/react';

const StartNode = () => (
  <div className="custom-node start-node">
    <strong>Start Node</strong>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

export default StartNode;