import { v4 as uuidv4 } from 'uuid';

// Utility to add a new node
export const addNode = (nodes, type, position = { x: 250, y: 150 }) => {
  const newNode = {
    id: uuidv4(),
    type,
    position,
    data: { label: `${type} Node` },
  };
  return [...nodes, newNode];
};

// Utility to update a node's data
export const updateNodeData = (nodes, nodeId, updatedData) =>
  nodes.map((node) =>
    node.id === nodeId
      ? { ...node, data: { ...node.data, ...updatedData } }
      : node
  );

// Utility to remove edges related to a specific node
export const removeEdgesForNode = (edges, nodeId) =>
  edges.filter((edge) => edge.source !== nodeId && edge.target !== nodeId);

// Utility to clean up orphan edges
export const cleanEdges = (edges, nodeIds) =>
  edges.filter((edge) =>
    nodeIds.includes(edge.source) && nodeIds.includes(edge.target)
  );