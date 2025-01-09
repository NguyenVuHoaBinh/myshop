import React, { useState, useEffect, useCallback } from 'react';
import { ReactFlow, useNodesState, useEdgesState, addEdge, Handle, Position, Background, Controls, MiniMap } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { v4 as uuidv4 } from 'uuid';
import axios from 'axios';
import Draggable from 'react-draggable';
import './FlowEditor.css';

// Custom Node Components with Styling
const InteractionNode = ({ data }) => (
  <div className="custom-node interaction-node">
    <strong>Interaction Node</strong>
    <div>Prompt: {data.prompt || 'N/A'}</div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

const LLMNode = ({ data }) => (
  <div className="custom-node llm-node">
    <strong>LLM Node</strong>
    <div>Template ID: {data.templateId || 'N/A'}</div>
    <div>AI Model: {data.aiModel || 'N/A'}</div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

const LogicNode = ({ data }) => (
  <div className="custom-node logic-node">
    <strong>Logic Node</strong>
    <div>Condition: {data.condition || 'None'}</div>
    <Handle type="source" position={Position.Bottom} id="true" />
    <Handle type="source" position={Position.Bottom} id="false" />
  </div>
);

const DataNode = ({ data }) => (
  <div className="custom-node data-node">
    <strong>Data Node</strong>
    <div>Key: {data.key || 'N/A'}</div>
    <div>Value: {data.value || 'N/A'}</div>
    <Handle type="source" position={Position.Bottom} />
  </div>
);

// Node Types Mapping
const nodeTypes = {
  interactionNode: InteractionNode,
  llmNode: LLMNode,
  logicNode: LogicNode,
  dataNode: DataNode,
};

// Flow Editor Component
const FlowEditor = ({ flowId, onSave }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [selectedNode, setSelectedNode] = useState(null);

  const fetchFlow = useCallback(async () => {
    try {
      const response = await axios.get(`/api/v1/flows/${flowId}`);
      const { nodes, edges } = response.data;
      setNodes(nodes || []);
      setEdges(edges || []);
    } catch (error) {
      console.error('Error fetching flow:', error);
    }
  }, [flowId]);

  const saveFlow = useCallback(async () => {
    try {
      const graphData = { nodes, edges };
      await axios.post('/api/v1/flows', graphData);
      alert('Flow saved successfully!');
      if (onSave) onSave(graphData);
    } catch (error) {
      console.error('Error saving flow:', error);
    }
  }, [nodes, edges, onSave]);

  const addNode = useCallback(
    (type) => {
      const newNode = {
        id: uuidv4(),
        type,
        position: { x: 100 + nodes.length * 50, y: 100 },
        data: { label: `${type} Node` },
      };
      setNodes((nds) => nds.concat(newNode));
    },
    [nodes, setNodes]
  );

  const updateNodeProperties = useCallback(
    (key, value) => {
      if (selectedNode) {
        const updatedNodes = nodes.map((node) => {
          if (node.id === selectedNode.id) {
            let validationError = '';
            // Validation rules for properties
            if (key === 'prompt' && value.trim() === '') {
              validationError = 'Prompt cannot be empty';
            } else if (key === 'templateId' && value.trim().length < 3) {
              validationError = 'Template ID must have at least 3 characters';
            } else if (key === 'condition' && value.trim() === '') {
              validationError = 'Condition cannot be empty';
            } else if ((key === 'key' || key === 'value') && value.trim() === '') {
              validationError = `${key} cannot be empty`;
            }

            return {
              ...node,
              data: { ...node.data, [key]: value, validationError },
            };
          }
          return node;
        });
        setNodes(updatedNodes);
      }
    },
    [selectedNode, nodes, setNodes]
  );

  const handleNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
  }, []);

  useEffect(() => {
    if (flowId) fetchFlow();
  }, [flowId, fetchFlow]);

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* Draggable Sidebar */}
      <Draggable>
        <div style={{ width: '300px', padding: '10px', borderRight: '1px solid #ccc', background: '#f9f9f9' }}>
          <h4>Node Properties</h4>
          {selectedNode ? (
            <div>
              <p>ID: {selectedNode.id}</p>
              <label>
                Label:
                <input
                  type="text"
                  value={selectedNode.data.label || ''}
                  onChange={(e) => updateNodeProperties('label', e.target.value)}
                />
              </label>
              <br />
              {/* Additional properties based on node type */}
              {selectedNode.type === 'interactionNode' && (
                <label>
                  Prompt:
                  <input
                    type="text"
                    value={selectedNode.data.prompt || ''}
                    onChange={(e) => updateNodeProperties('prompt', e.target.value)}
                  />
                </label>
              )}
              {selectedNode.type === 'llmNode' && (
                <>
                  <label>
                    Template ID:
                    <input
                      type="text"
                      value={selectedNode.data.templateId || ''}
                      onChange={(e) => updateNodeProperties('templateId', e.target.value)}
                    />
                  </label>
                  <br />
                  <label>
                    AI Model:
                    <input
                      type="text"
                      value={selectedNode.data.aiModel || ''}
                      onChange={(e) => updateNodeProperties('aiModel', e.target.value)}
                    />
                  </label>
                </>
              )}
              {selectedNode.type === 'logicNode' && (
                <label>
                  Condition:
                  <input
                    type="text"
                    value={selectedNode.data.condition || ''}
                    onChange={(e) => updateNodeProperties('condition', e.target.value)}
                  />
                </label>
              )}
              {selectedNode.type === 'dataNode' && (
                <>
                  <label>
                    Key:
                    <input
                      type="text"
                      value={selectedNode.data.key || ''}
                      onChange={(e) => updateNodeProperties('key', e.target.value)}
                    />
                  </label>
                  <br />
                  <label>
                    Value:
                    <input
                      type="text"
                      value={selectedNode.data.value || ''}
                      onChange={(e) => updateNodeProperties('value', e.target.value)}
                    />
                  </label>
                </>
              )}
              {selectedNode?.data?.validationError && (
                <p style={{ color: 'red', fontSize: '12px' }}>{selectedNode.data.validationError}</p>
              )}
            </div>
          ) : (
            <p>Select a node to edit properties.</p>
          )}
        </div>
      </Draggable>

      {/* Graph Editor */}
      <div style={{ flex: 1, position: 'relative' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={(params) => {
            params.label = params.sourceHandle === 'true' ? 'True' : 'False';
            setEdges((eds) => addEdge(params, eds));
          }}
          onNodeClick={handleNodeClick}
          nodeTypes={nodeTypes}
        >
          <Controls />
          <MiniMap />
          <Background gap={16} />
        </ReactFlow>
        {/* Toolbar */}
        <div style={{ position: 'absolute', top: 10, right: 10, display: 'flex', gap: '10px' }}>
          <button onClick={() => addNode('interactionNode')}>Add Interaction Node</button>
          <button onClick={() => addNode('llmNode')}>Add LLM Node</button>
          <button onClick={() => addNode('logicNode')}>Add Logic Node</button>
          <button onClick={() => addNode('dataNode')}>Add Data Node</button>
          <button onClick={saveFlow}>Save Flow</button>
        </div>
      </div>
    </div>
  );
};

export default FlowEditor;