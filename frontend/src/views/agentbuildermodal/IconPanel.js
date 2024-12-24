import React from "react";
import { FaList, FaTasks, FaBook, FaClipboardList } from "react-icons/fa";

const IconPanel = ({ activeTab, setActiveTab }) => (
  <div className="icons-panel">
    <div className={`icon-button ${activeTab === 1 ? 'active' : ''}`} onClick={() => setActiveTab(1)}>
      <FaList size={24} />
    </div>
    <div className={`icon-button ${activeTab === 2 ? 'active' : ''}`} onClick={() => setActiveTab(2)}>
      <FaTasks size={24} />
    </div>
    <div className={`icon-button ${activeTab === 3 ? 'active' : ''}`} onClick={() => setActiveTab(3)}>
      <FaBook size={24} />
    </div>
    <div className={`icon-button ${activeTab === 4 ? 'active' : ''}`} onClick={() => setActiveTab(4)}>
      <FaClipboardList size={24} />
    </div>
  </div>
);

export default IconPanel;
