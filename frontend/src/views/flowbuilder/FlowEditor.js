import React, { useState, useEffect, useCallback } from 'react';
import { ReactFlow, useNodesState, useEdgesState, addEdge, Handle, Position, Background, Controls, MiniMap } from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { v4 as uuidv4 } from 'uuid';
import axios from 'axios';
import Draggable from 'react-draggable';
import './FlowEditor.css';

// --- Custom Node Components ---
const StartNode = () => (
  <div className="custom-node start-node">
    <strong>Start Node</strong>
    {/* Only Source Handle (No Target Handle) */}
    <Handle type="source" position={Position.Bottom} />
  </div>
);

const EndNode = () => (
  <div className="custom-node end-node">
    <strong>End Node</strong>
    {/* Only Target Handle (No Source Handle) */}
    <Handle type="target" position={Position.Top} />
  </div>
);

const InteractionNode = ({ data }) => (
  <div className="custom-node interaction-node">
    <strong>Interaction Node</strong>
    <div>Prompt: {data.prompt || 'N/A'}</div>
    <Handle type="target" position={Position.Top} />
    <Handle type="source" position={Position.Bottom} />
  </div>
);

const LLMNode = ({ data }) => (
  <div className="custom-node llm-node">
    <strong>LLM Node</strong>
    <div>Template ID: {data.templateId || 'N/A'}</div>
    <div>AI Model: {data.aiModel || 'N/A'}</div>
    <Handle type="target" position={Position.Top} />
    <Handle type="source" position={Position.Bottom} />
  </div>
);

const LogicNode = ({ data }) => (
  <div className="custom-node logic-node">
    <strong>Logic Node</strong>
    <div>Condition: {data.condition || 'None'}</div>
    <Handle type="target" position={Position.Top} />
    <Handle type="source" position={Position.Bottom} id="true" />
    <Handle type="source" position={Position.Bottom} id="false" />
  </div>
);

const DataNode = ({ data }) => (
  <div className="custom-node data-node">
    <strong>Data Node</strong>
    <div>Key: {data.key || 'N/A'}</div>
    <div>Value: {data.value || 'N/A'}</div>
    <Handle type="target" position={Position.Top} />
    <Handle type="source" position={Position.Bottom} />
  </div>
);

// --- Node Types Mapping ---
const nodeTypes = {
  startNode: StartNode,
  endNode: EndNode,
  interactionNode: InteractionNode,
  llmNode: LLMNode,
  logicNode: LogicNode,
  dataNode: DataNode,
};

const FlowEditor = ({ flowId, onSave }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [selectedNode, setSelectedNode] = useState(null);

  // --- Fetch Existing Flow ---
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

  // --- Save Current Flow ---
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

  // --- Add a New Node ---
  const addNode = useCallback(
    (type) => {
      // Limit to only one Start and End Node
      if (type === 'startNode' && nodes.some((node) => node.type === 'startNode')) {
        alert('There can be only one Start Node!');
        return;
      }
      if (type === 'endNode' && nodes.some((node) => node.type === 'endNode')) {
        alert('There can be only one End Node!');
        return;
      }
      const newNode = {
        id: uuidv4(),
        type,
        position: { x: 100 + nodes.length * 50, y: 100 },
        data: { label: `${type} Node` },
      };
      setNodes((nds) => nds.concat(newNode));
    },
    [nodes, setNodes],
  );

  // --- Handle Node Click (for Selecting a Node) ---
  const handleNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
  }, []);

  // --- Delete Selected Node and Its Edges ---
  const deleteNode = useCallback(() => {
    if (!selectedNode) return;

    // Remove the selected node and its edges
    setNodes((nds) => nds.filter((node) => node.id !== selectedNode.id));
    setEdges((eds) =>
      eds.filter((edge) => edge.source !== selectedNode.id && edge.target !== selectedNode.id),
    );

    // Clear the selected node
    setSelectedNode(null);
  }, [selectedNode, setNodes, setEdges]);

  // --- Fetch the Flow When Component Mounts ---
  useEffect(() => {
    if (flowId) fetchFlow();
  }, [flowId, fetchFlow]);

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* Draggable Sidebar */}
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
          {selectedNode ? (
            <div>
              <p>ID: {selectedNode.id}</p>
              <label>
                Label:
                <input
                  type="text"
                  value={selectedNode.data.label || ''}
                  onChange={(e) =>
                    setNodes((nds) =>
                      nds.map((node) =>
                        node.id === selectedNode.id
                          ? { ...node, data: { ...node.data, label: e.target.value } }
                          : node,
                      ),
                    )
                  }
                />
              </label>
            </div>
          ) : (
            <p>Select a node to edit properties.</p>
          )}
        </div>
      </Draggable>

      {/* Main Flow Editor Area */}
      <div style={{ flex: 1, position: 'relative' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={(params) => setEdges((eds) => addEdge(params, eds))}
          onNodeClick={handleNodeClick}
          nodeTypes={nodeTypes}
        >
          <Background gap={16} />
          <MiniMap />
          <Controls />
        </ReactFlow>

        {/* Toolbar */}
        <div style={{ position: 'absolute', top: 10, right: 10, display: 'flex', gap: '10px' }}>
          <button onClick={() => addNode('startNode')}>Add Start Node</button>
          <button onClick={() => addNode('endNode')}>Add End Node</button>
          <button onClick={() => addNode('interactionNode')}>Add Interaction Node</button>
          <button onClick={() => addNode('llmNode')}>Add LLM Node</button>
          <button onClick={() => addNode('logicNode')}>Add Logic Node</button>
          <button onClick={() => addNode('dataNode')}>Add Data Node</button>
          <button onClick={saveFlow}>Save Flow</button>
          <button onClick={deleteNode} disabled={!selectedNode}>
            Delete Node
          </button>
        </div>
      </div>
    </div>
  );
};

export default FlowEditor;
