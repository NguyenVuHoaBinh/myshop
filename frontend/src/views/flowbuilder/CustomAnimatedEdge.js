import React, { useRef, useEffect, useState } from 'react';
import { getBezierPath, getEdgeCenter } from '@xyflow/react';

const CustomAnimatedEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  data,
}) => {
  const globalAnimationDuration = 5; // Global duration (in seconds)
  const [edgePath] = getBezierPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
    sourcePosition,
    targetPosition,
  });

  const pathRef = useRef(null);
  const [pathLength, setPathLength] = useState(0);

  useEffect(() => {
    if (pathRef.current) {
      const length = pathRef.current.getTotalLength();
      setPathLength(length);
    }
  }, [edgePath]); // Update when path changes

  const [centerX, centerY] = getEdgeCenter({
    sourceX,
    sourceY,
    targetX,
    targetY,
  });

  return (
    <g>
      <path
        ref={pathRef}
        className="react-flow__edge-path"
        d={edgePath}
        markerEnd={markerEnd}
        style={{
          ...style,
          stroke: '#888',
          strokeWidth: 3,
        }}
      />
      {pathLength > 0 && (
        <circle r="5" fill="#00bfff">
          <animateMotion
            path={edgePath} // Follow the edge path
            dur={`${globalAnimationDuration}s`} // Uniform time for all edges
            repeatCount="indefinite"
            key={id} // Ensure reset for new edges
          />
        </circle>
      )}
      <foreignObject x={centerX - 10} y={centerY - 10} width={20} height={20}>
        <div
          style={{
            background: 'red',
            borderRadius: '50%',
            color: 'white',
            cursor: 'pointer',
            height: '20px',
            width: '20px',
            textAlign: 'center',
            lineHeight: '20px',
            fontSize: '12px',
          }}
          onClick={(evt) => {
            evt.stopPropagation();
            if (data?.onEdgeDelete) {
              data.onEdgeDelete(id);
            }
          }}
        >
          ×
        </div>
      </foreignObject>
    </g>
  );
};

export default CustomAnimatedEdge;