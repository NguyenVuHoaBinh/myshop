import React, { useState } from 'react';
import { Handle, Position } from '@xyflow/react';
import axios from '../../api/axiosInstance';

const DataNode = ({ data, onChange }) => {
  const [status, setStatus] = useState('idle');

  const fetchData = async () => {
    setStatus('loading');
    try {
      const response = await axios.get(`/api/data/${data.key}`);
      if (onChange) {
        onChange({ ...data, value: response.data.value });
      }
      setStatus('success');
    } catch (error) {
      console.error('Error fetching data:', error);
      alert('Failed to fetch data');
      setStatus('error');
    }
  };

  return (
    <div className="custom-node data-node">
      <strong>Data Node</strong>
      <div>
        <label>Key:</label>
        <input
          type="text"
          value={data.key || ''}
          onChange={(e) =>
            onChange({ ...data, key: e.target.value })
          }
        />
        <label>Value:</label>
        <p>{data.value || 'N/A'}</p>
        <button onClick={fetchData} disabled={status === 'loading'}>
          {status === 'loading' ? 'Fetching...' : 'Fetch Data'}
        </button>
        {status === 'error' && (
          <p style={{ color: 'red' }}>Error fetching data!</p>
        )}
      </div>
      {/* Handles */}
      <Handle type="target" position={Position.Top} />
      <Handle type="source" position={Position.Bottom} />
    </div>
  );
};

export default DataNode;