import React, { useState } from "react";
import PropTypes from "prop-types";
import { Handle, Position } from "@xyflow/react";

const LogicNode = ({ data, onChange }) => {
  const [condition, setCondition] = useState(data.condition || "");
  const [error, setError] = useState("");

  const handleConditionChange = (e) => {
    const newCondition = e.target.value;

    try {
      // Optional: Add validation or parsing for Boolean expressions
      setCondition(newCondition);
      setError(""); // Clear errors if condition is valid

      if (onChange) {
        onChange({ ...data, condition: newCondition });
      }
    } catch (err) {
      setError("Invalid condition");
    }
  };

  return (
    <div className="custom-node logic-node">
      <strong>Logic Node</strong>
      <div>
        <label>Condition:</label>
        <input
          type="text"
          value={condition}
          placeholder="Enter condition (e.g., x > 10)"
          onChange={handleConditionChange}
          style={{ borderColor: error ? "red" : "#ccc" }}
        />
        {error && <p style={{ color: "red", fontSize: "0.8em" }}>{error}</p>}
        <p>Outputs:</p>
        <ul>
          <li><strong>True:</strong> Proceeds to the "True" path</li>
          <li><strong>False:</strong> Proceeds to the "False" path</li>
        </ul>
      </div>
      {/* Dynamic Handles */}
      <Handle
        id="true"
        type="source"
        position={Position.Right}
        style={{ background: "green" }}
      />
      <Handle
        id="false"
        type="source"
        position={Position.Left}
        style={{ background: "red" }}
      />
      <Handle type="target" position={Position.Top} />
    </div>
  );
};

LogicNode.propTypes = {
  data: PropTypes.shape({
    condition: PropTypes.string,
  }),
  onChange: PropTypes.func,
};

export default LogicNode;