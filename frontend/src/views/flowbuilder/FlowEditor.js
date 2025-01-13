import React, { useState, useEffect, useCallback } from 'react';
import {
  ReactFlow,
  useNodesState,
  useEdgesState,
  addEdge,
  Background,
  Controls,
  MiniMap,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import axios from '../../api/axiosInstance';
import Sidebar from './Sidebar';
import Toolbar from './Toolbar';
import nodeTypes from '../nodes'; // Import custom node types
import './FlowEditor.css';

const FlowEditor = ({ flowId, onSave }) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]); // ReactFlow nodes state
  const [edges, setEdges, onEdgesChange] = useEdgesState([]); // ReactFlow edges state
  const [templates, setTemplates] = useState([]); // Store templates globally
  const [selectedNode, setSelectedNode] = useState(null); // Currently selected node in the graph

  // Fetch templates only once when the editor is mounted
  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const response = await axios.get('/api/templates');
        setTemplates(response.data.content || []); // Assuming templates are in `content`
      } catch (error) {
        console.error('Error fetching templates:', error);
      }
    };

    fetchTemplates();
  }, []);

  // Add a new node with templates to the graph
  const addNodeWithTemplates = (type) => {
    const newNode = {
      id: `node-${Date.now()}`, // Unique node ID
      type,
      position: { x: Math.random() * 250, y: Math.random() * 400 },
      data: {
        label: `${type} Node`,
        templateId: '', // No template selected by default
        selectedTemplate: null, // Will be populated later based on selection
        onChange: (updatedNodeData) => {
          // Callback to update the node
          setNodes((nodes) =>
            nodes.map((node) =>
              node.id === updatedNodeData.id
                ? { ...node, data: { ...updatedNodeData.data } }
                : node
            )
          );
        },
      },
    };

    setNodes((nodes) => [...nodes, newNode]);
  };

  // Fetch flow data when loading a flow (if flowId is set)
  const fetchFlow = useCallback(async () => {
    try {
      const response = await axios.get(`/api/v1/flows/${flowId}`);
      const { nodes: fetchedNodes, edges } = response.data;

      const updatedNodes = fetchedNodes.map((node) => ({
        ...node,
        data: {
          ...node.data,
          onChange: (updatedNodeData) => {
            setNodes((nds) =>
              nds.map((n) =>
                n.id === updatedNodeData.id
                  ? { ...n, data: { ...updatedNodeData.data } }
                  : n
              )
            );
          },
        },
      }));

      setNodes(updatedNodes || []);
      setEdges(edges || []);
    } catch (error) {
      console.error('Error fetching flow:', error);
    }
  }, [flowId]);

  useEffect(() => {
    if (flowId) fetchFlow();
  }, [flowId, fetchFlow]);

  return (
    <div style={{ display: 'flex', height: '100vh' }}>
      {/* Inject Sidebar with Template Selection */}
      <Sidebar
        selectedNode={selectedNode} // Pass currently selected node
        templates={templates} // Pass the global templates list
        onUpdateNodeData={(updatedNode) =>
          setNodes((nds) =>
            nds.map((node) =>
              node.id === updatedNode.id
                ? { ...node, data: updatedNode.data }
                : node
            )
          )
        }
      />
      <div style={{ flex: 1, position: 'relative' }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={(params) =>
            setEdges((eds) =>
              addEdge(
                {
                  ...params,
                  type: 'smoothstep',
                  arrowHeadType: 'arrow',
                  style: { strokeWidth: 3, stroke: '#888' },
                },
                eds
              )
            )
          }
          onNodeClick={(event, node) => setSelectedNode(node)}
          nodeTypes={nodeTypes}
        >
          <Background gap={16} />
          <MiniMap />
          <Controls />
        </ReactFlow>
        {/* Toolbar for controls */}
        <Toolbar
          onSave={() => console.log('Save Triggered')}
          setNodes={setNodes}
          onDeleteNode={() =>
            setNodes((nds) => nds.filter((node) => node.id !== selectedNode.id))
          }
          addNode={addNodeWithTemplates}
          isDeleteDisabled={!selectedNode}
        />
      </div>
    </div>
  );
};

export default FlowEditor;