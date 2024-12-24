import React from "react";
import { CTable, CTableHead, CTableRow, CTableHeaderCell, CTableBody, CTableDataCell, CDropdown, CDropdownToggle, CDropdownMenu, CDropdownItem } from "@coreui/react";

const TopicsPanel = ({ topics, handleShowLibraryModal, handleShowCreateTopicModal }) => (
  <div className="topics-info" style={{ flex: 1, padding: '20px', borderRight: '1px solid #ccc' }}>
    <header className="agent-header">
      <h1>Topics Overview</h1>
      <div style={{ marginTop: '20px' }}>
        <CDropdown>
          <CDropdownToggle color="primary">New Topic</CDropdownToggle>
          <CDropdownMenu>
            <CDropdownItem onClick={handleShowLibraryModal}>Add from Library</CDropdownItem>
            <CDropdownItem onClick={handleShowCreateTopicModal}>Create New Topic</CDropdownItem>
          </CDropdownMenu>
        </CDropdown>
      </div>
    </header>
    <CTable style={{ marginTop: '20px' }}>
      <CTableHead>
        <CTableRow>
          <CTableHeaderCell>ID</CTableHeaderCell>
          <CTableHeaderCell>Label</CTableHeaderCell>
          <CTableHeaderCell>Description</CTableHeaderCell>
        </CTableRow>
      </CTableHead>
      <CTableBody>
        {topics.map((topic) => (
          <CTableRow key={topic.id}>
            <CTableDataCell>{topic.id}</CTableDataCell>
            <CTableDataCell>{topic.label}</CTableDataCell>
            <CTableDataCell>{topic.description}</CTableDataCell>
          </CTableRow>
        ))}
      </CTableBody>
    </CTable>
  </div>
);

export default TopicsPanel;
