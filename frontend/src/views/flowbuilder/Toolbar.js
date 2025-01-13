import React from 'react';
import { v4 as uuidv4 } from 'uuid';

const Toolbar = ({ onSave, setNodes, onDeleteNode, isDeleteDisabled }) => {
  const addNode = (type) => {
    const newNode = {
      id: uuidv4(),
      type,
      position: { x: Math.random() * 250, y: Math.random() * 400 },
      data: { label: `${type} Node` },
    };
    setNodes((nodes) => [...nodes, newNode]);
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: 10,
        right: 10,
        display: 'flex',
        gap: 10,
      }}
    >
      <button onClick={() => addNode('startNode')}>Add Start Node</button>
      <button onClick={() => addNode('endNode')}>Add End Node</button>
      <button onClick={() => addNode('interactionNode')}>
        Add Interaction Node
      </button>
      <button onClick={() => addNode('llmNode')}>Add LLM Node</button>
      <button onClick={() => addNode('logicNode')}>Add Logic Node</button>
      <button onClick={() => addNode('dataNode')}>Add Data Node</button>
      <button onClick={onSave}>Save Flow</button>
      <button onClick={onDeleteNode} disabled={isDeleteDisabled}>
        Delete Node
      </button>
    </div>
  );
};

export default Toolbar;