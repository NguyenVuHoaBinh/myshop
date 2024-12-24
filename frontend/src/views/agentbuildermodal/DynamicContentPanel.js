import React from "react";

const DynamicContentPanel = ({ activeTab }) => {
  switch (activeTab) {
    case 1:
      return <div><h2>Topics</h2><p>Manage topics for your agent here.</p></div>;
    case 2:
      return <div><h2>Actions</h2><p>Manage actions available to your agent here.</p></div>;
    case 3:
      return <div><h2>Knowledge</h2><p>Manage knowledge articles and resources here.</p></div>;
    case 4:
      return <div><h2>Logs</h2><p>View activity logs of the agent here.</p></div>;
    default:
      return <div><h2>Welcome</h2><p>Select an option from the sidebar.</p></div>;
  }
};

export default DynamicContentPanel;
