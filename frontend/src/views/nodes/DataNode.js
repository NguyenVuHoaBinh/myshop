import React, { useState, useEffect } from "react";
import { Handle, Position } from "@xyflow/react";
import axios from "../../api/axiosInstance";

/**
 * DataNode (Auto-run)
 *
 * - No user inputs or button.
 * - On mount (or data change), automatically executes the request.
 * - Displays status (loading, success, error).
 * - If success or error, we notify the parent (onChange) so the flow can decide
 *   to move forward or revert to a previous node.
 */
const DataNode = ({ data, onChange }) => {
  const [status, setStatus] = useState("idle");
  const [errorMsg, setErrorMsg] = useState("");

  const {
    nodeName = "Data Node",
    requestUrl = "",
    requestBody = "",
    // You might also have onSuccessNextNode, onErrorNextNode, etc.
  } = data;

  /**
   * Helper to update node data and notify parent/flow engine.
   */
  const updateData = (changes) => {
    if (onChange) {
      onChange({ ...data, ...changes });
    }
  };

  /**
   * Auto-run whenever this component mounts or the relevant props change.
   * In practice, you might only want to run once. If so, condition on a "firstMount".
   */
  useEffect(() => {
    let isCancelled = false;

    const runRequest = async () => {
      setStatus("loading");
      setErrorMsg("");
      try {
        // If requestBody is JSON text, parse it
        let parsedBody = {};
        if (typeof requestBody === "string" && requestBody.trim()) {
          try {
            parsedBody = JSON.parse(requestBody);
          } catch (parseErr) {
            throw new Error("Invalid JSON in requestBody.");
          }
        }

        const response = await axios.post(requestUrl, parsedBody);
        if (!isCancelled) {
          setStatus("success");
          // Optionally store response in data or context
          updateData({ lastResponse: response.data });
          // A real flow might do: flowEngine.moveNext(); or onChange({ status: 'success' });
        }
      } catch (err) {
        if (!isCancelled) {
          console.error("DataNode request error:", err);
          setStatus("error");
          setErrorMsg(err?.message || "Unknown error");
          // e.g. flowEngine.goTo(nodeData.onErrorNextNode);
        }
      }
    };

    // Only run if we have at least a URL
    if (requestUrl) {
      runRequest();
    } else {
      console.warn("DataNode: Missing requestUrl. Skipping request.");
    }

    return () => {
      isCancelled = true;
    };
  }, [requestUrl, requestBody]); 
  // Depend on requestUrl/requestBody so if they change, we run again.
  // If you only want to run once, remove the dependencies and add an indicator.

  return (
    <div className="custom-node data-node">
      <strong>{nodeName}</strong>

      <div style={{ marginTop: "0.5rem" }}>
        {status === "idle" && <p>Idle (awaiting auto-run)...</p>}
        {status === "loading" && <p>Loading...</p>}
        {status === "success" && (
          <p style={{ color: "green" }}>Success! (Data posted)</p>
        )}
        {status === "error" && (
          <p style={{ color: "red" }}>Error: {errorMsg}</p>
        )}
      </div>

      <Handle type="target" position={Position.Top} />
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default DataNode;
