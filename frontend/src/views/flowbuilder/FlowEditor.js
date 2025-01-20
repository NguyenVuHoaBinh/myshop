import React, { useState, useEffect, useCallback } from "react";
import {
  ReactFlow,
  useNodesState,
  useEdgesState,
  addEdge,
  Background,
  Controls,
  MiniMap,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import axiosInstance from "../../api/axiosInstance";
import Sidebar from "./Sidebar";
import Toolbar from "./Toolbar";
import nodeTypes from "../nodes";
import CustomAnimatedEdge from "./CustomAnimatedEdge";
import { useParams } from "react-router-dom";
import {
  addNode,
  updateNodeData,
  removeEdgesForNode,
  removeOrphanEdges,
} from "./FlowUtils"; // Import utility functions for managing nodes and edges
import ChatPanel from "../chatpanel/ChatPanel";
import "./FlowEditor.css";

const edgeTypes = {
  animatedEdge: CustomAnimatedEdge,
};

const FlowEditor = () => {
  const { flowId } = useParams();

  // ---------------------------
  // State Management
  // ---------------------------
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [flowName, setFlowName] = useState("");
  const [description, setDescription] = useState("");
  const [templates, setTemplates] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);

  // ---------------------------
  // API Call: Fetch Templates
  // ---------------------------
  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const response = await axiosInstance.get("/api/templates");
        setTemplates(response.data.content || []);
      } catch (error) {
        console.error("Error fetching templates:", error);
        alert("Failed to fetch templates.");
      }
    };
    fetchTemplates();
  }, []);

  // ---------------------------
  // API Call: Fetch Flow
  // ---------------------------
  const fetchFlow = useCallback(async () => {
    if (!flowId) return;
    try {
      const response = await axiosInstance.get(`/api/v1/flows/${flowId}`);
      const { flow } = response.data;

      if (!flow) {
        alert("No flow data returned from server.");
        return;
      }

      const { name, description, nodes: fetchedNodes, edges: fetchedEdges } = flow;

      // Set metadata
      setFlowName(name || "");
      setDescription(description || "");

      // Process nodes
      const updatedNodes = (fetchedNodes || []).map((node) => ({
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

      // Process edges
      const updatedEdges = (fetchedEdges || []).map((edge) => ({
        ...edge,
        id: edge.id || `edge-${Date.now()}`,
        type: "animatedEdge",
        markerEnd: "url(#arrowhead)",
      }));

      setNodes(updatedNodes);
      setEdges(updatedEdges);
    } catch (error) {
      console.error("Error fetching flow:", error);
      alert("Failed to fetch flow data.");
    }
  }, [flowId, setNodes, setEdges]);

  useEffect(() => {
    fetchFlow();
 }, [flowId, fetchFlow]);

  // ---------------------------
  // Remove Orphaned Edges
  // ---------------------------
  useEffect(() => {
    // Clean orphan edges whenever nodes are updated
    setEdges((eds) => removeOrphanEdges(eds, nodes.map((node) => node.id)));
  }, [nodes, setEdges]);

  // ---------------------------
  // Handlers
  // ---------------------------
  const handleConnect = (params) => {
    setEdges((eds) =>
      addEdge(
        {
          ...params,
          id: `edge-${Date.now()}`,
          type: "animatedEdge",
          markerEnd: "url(#arrowhead)",
        },
        eds
      )
    );
  };

  const handleSave = async () => {
    const payload = {
      id: flowId || "",
      name: flowName,
      description,
      nodes: nodes.map((node) => ({
        id: node.id,
        type: node.type,
        position: node.position,
        data: node.data,
      })),
      edges: edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.type,
      })),
    };

    try {
      if (flowId) {
        // Update existing flow
        await axiosInstance.put(`/api/v1/flows/${flowId}`, payload);
        alert("Flow updated successfully!");
      } else {
        // Create new flow
        await axiosInstance.post("/api/v1/flows", payload);
        alert("Flow created successfully!");
      }
    } catch (error) {
      console.error("Error saving flow:", error);
      alert("Failed to save the flow.");
    }
  };

  // ---------------------------
  // Render Editor
  // ---------------------------
  return (
    <div style={{ display: "flex", height: "100vh" }}>
      {/* LEFT: Flow Editor */}
      <div style={{ flex: 2, display: "flex" }}>
        {/* Sidebar */}
        <Sidebar
          selectedNode={selectedNode}
          templates={templates}
          flowName={flowName}
          description={description}
          setFlowName={setFlowName}
          setDescription={setDescription}
          onUpdateNodeData={(updatedNode) =>
            setNodes((nds) =>
              nds.map((node) =>
                node.id === updatedNode.id ? { ...node, data: updatedNode.data } : node
              )
            )
          }
        />
        {/* ReactFlow Canvas */}
        <div style={{ flex: 1, position: "relative" }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={handleConnect}
            onNodeClick={(event, node) => setSelectedNode(node)}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
          >
            <Background gap={16} />
            <MiniMap />
            <Controls />
            <svg>
              <defs>
                <marker
                  id="arrowhead"
                  markerWidth="10"
                  markerHeight="7"
                  refX="10"
                  refY="3.5"
                  orient="auto"
                >
                  <polygon points="0 0, 10 3.5, 0 7" fill="#999" />
                </marker>
              </defs>
            </svg>
          </ReactFlow>
          {/* Toolbar */}
          <Toolbar
            onSave={handleSave}
            setNodes={setNodes}
            onDeleteNode={() => {
              if (selectedNode) {
                setNodes((nds) => nds.filter((node) => node.id !== selectedNode.id));
                setEdges((eds) => removeEdgesForNode(eds, selectedNode.id)); // Remove edges here
              }
            }}
            isDeleteDisabled={!selectedNode}
          />
        </div>
      </div>
      {/* RIGHT: Chat Panel */}
      <ChatPanel flowId={flowId} />
    </div>
  );
};

export default FlowEditor;