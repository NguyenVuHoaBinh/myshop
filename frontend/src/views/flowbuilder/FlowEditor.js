import React, {
  useState,
  useEffect,
  useCallback,
  useRef
} from "react";
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
import axiosInstance from "../../api/axiosInstance"; // Preconfigured Axios instance
import Sidebar from "./Sidebar";
import Toolbar from "./Toolbar";
import nodeTypes from "../nodes";
import CustomAnimatedEdge from "./CustomAnimatedEdge";
import "./FlowEditor.css";
import { useParams } from "react-router-dom";

const edgeTypes = {
  animatedEdge: CustomAnimatedEdge,
};

const FlowEditor = () => {
  const { flowId } = useParams();

  // ---------------------------
  // 1) Flow Editor State
  // ---------------------------
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  const [flowName, setFlowName] = useState("");
  const [description, setDescription] = useState("");
  const [templates, setTemplates] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);

  /**
   * Fetch templates on mount (optional)
   */
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

  /**
   * Fetch full flow from "/api/v1/flows/{flowId}" when flowId is provided
   */
  const fetchFlow = useCallback(async () => {
    if (!flowId) return; // If no flowId, skip fetch (maybe user is creating a new flow)

    try {
      // New backend returns { flow: { ... }, timestamp: ... }
      const response = await axiosInstance.get(`/api/v1/flows/${flowId}`);
      const { flow } = response.data; // extract the flow object

      if (!flow) {
        alert("No flow data returned from server.");
        return;
      }

      const { name, description, nodes: fetchedNodes, edges: fetchedEdges } = flow;

      // Set top-level metadata
      setFlowName(name || "");
      setDescription(description || "");

      // Prepare nodes
      const updatedNodes = (fetchedNodes || []).map((node) => ({
        ...node,
        // Attach onChange logic for dynamic updates
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

      // Prepare edges
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

  // Fetch flow on component mount or when flowId changes
  useEffect(() => {
    fetchFlow();
  }, [flowId, fetchFlow]);

  /**
   * Handle edge creation in ReactFlow
   */
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

  /**
   * Handle saving the flow:
   * - If flowId is set, do PUT /api/v1/flows/{flowId} to update
   * - Otherwise POST /api/v1/flows to create a new flow
   */
  const handleSave = async () => {
    const payload = {
      id: flowId || "", // If updating, must match existing ID; if creating, can omit or set empty
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

    console.log("Saving flow with payload:", payload);

    try {
      let response;
      if (flowId) {
        // Update existing flow
        response = await axiosInstance.put(`/api/v1/flows/${flowId}`, payload);
        console.log("Flow updated successfully:", response.data);
        alert("Flow updated successfully!");
      } else {
        // Create new flow
        response = await axiosInstance.post("/api/v1/flows", payload);
        console.log("Flow created successfully:", response.data);
        alert("Flow created successfully!");
      }
    } catch (error) {
      console.error("Error saving flow:", error);
      alert("Failed to save the flow.");
    }
  };

  // ---------------------------
  // 2) Chat Panel State
  // ---------------------------
  const [chatMessages, setChatMessages] = useState([
    { sender: "bot", text: "Hello! How can I assist you?" },
  ]);
  const [userInput, setUserInput] = useState("");
  const chatContainerRef = useRef(null);

  // We'll store the WebSocket instance in a ref
  const wsRef = useRef(null);

  // Establish the WebSocket connection on mount
  useEffect(() => {
    // Replace with your actual WebSocket URL, e.g. "ws://localhost:8080/ws/chat"
    const wsUrl = "ws://localhost:8888/ws/chat";
    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("Chat WebSocket connection established:", wsUrl);
    };

    ws.onmessage = (evt) => {
      try {
        const data = JSON.parse(evt.data);
        // Expected shape: { sender: "bot", message: "..." }
        const newMessage = {
          sender: data.sender || "bot",
          text: data.message || "",
        };
        setChatMessages((prev) => [...prev, newMessage]);
      } catch (err) {
        console.error("Failed to parse chat WebSocket message:", err);
      }
    };

    ws.onclose = () => {
      console.log("Chat WebSocket connection closed.");
    };

    ws.onerror = (error) => {
      console.error("Chat WebSocket error:", error);
    };

    // Cleanup on unmount
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, []);

  // Auto-scroll to bottom when new chat messages appear
  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
    }
  }, [chatMessages]);

  // Function to send a message to the server
  const handleChatSend = () => {
    if (!userInput.trim()) return;
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      console.error("WebSocket is not open. Cannot send message.");
      return;
    }

    // 1) Add user message to local chat
    const userMsg = { sender: "user", text: userInput };
    setChatMessages((prev) => [...prev, userMsg]);

    // 2) Send JSON to the server
    const payload = {
      flowId: flowId || "defaultFlow",
      userResponse: userInput,
    };
    ws.send(JSON.stringify(payload));

    // 3) Clear input
    setUserInput("");
  };

  // ---------------------------
  // 3) Render the Layout
  // ---------------------------
  return (
    <div style={{ display: "flex", height: "100vh" }}>
      {/* LEFT (2/3): Flow Editor */}
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
            onDeleteNode={() =>
              setNodes((nds) => nds.filter((node) => node.id !== selectedNode?.id))
            }
            isDeleteDisabled={!selectedNode}
          />
        </div>
      </div>

      {/* RIGHT (1/3): Chat Panel */}
      <div
        style={{
          flex: 1,
          borderLeft: "1px solid #ccc",
          display: "flex",
          flexDirection: "column",
        }}
      >
        {/* Chat header */}
        <div style={{ padding: "10px", borderBottom: "1px solid #ccc" }}>
          <h4>Chat Panel</h4>
        </div>

        {/* Chat messages area */}
        <div
          ref={chatContainerRef}
          style={{
            flex: 1,
            overflowY: "auto",
            padding: "10px",
            backgroundColor: "#f7f7f7",
          }}
        >
          {chatMessages.map((msg, idx) => (
            <div
              key={idx}
              style={{
                display: "flex",
                justifyContent:
                  msg.sender === "user" ? "flex-end" : "flex-start",
                marginBottom: "8px",
              }}
            >
              <div
                style={{
                  maxWidth: "60%",
                  borderRadius: "8px",
                  padding: "8px 12px",
                  backgroundColor:
                    msg.sender === "user" ? "#0d6efd" : "#e2e2e2",
                  color: msg.sender === "user" ? "#fff" : "#000",
                  whiteSpace: "pre-wrap",
                }}
              >
                {msg.text}
              </div>
            </div>
          ))}
        </div>

        {/* Chat input */}
        <div
          style={{
            display: "flex",
            borderTop: "1px solid #ccc",
            padding: "10px",
          }}
        >
          <input
            type="text"
            placeholder="Type your message..."
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            style={{
              flex: 1,
              marginRight: "10px",
              borderRadius: "4px",
              border: "1px solid #ccc",
              padding: "8px",
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleChatSend();
            }}
          />
          <button
            onClick={handleChatSend}
            style={{
              padding: "8px 16px",
              backgroundColor: "#0d6efd",
              color: "#fff",
              border: "none",
              borderRadius: "4px",
            }}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
};

export default FlowEditor;
