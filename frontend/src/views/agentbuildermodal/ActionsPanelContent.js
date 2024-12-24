import React from "react";
import { CTable, CTableHead, CTableRow, CTableHeaderCell, CTableBody, CTableDataCell } from "@coreui/react";

const ActionsPanelContent = ({ actions }) => {
  return (
    <CTable>
      <CTableHead>
        <CTableRow>
          <CTableHeaderCell>ID</CTableHeaderCell>
          <CTableHeaderCell>Action Label</CTableHeaderCell>
          <CTableHeaderCell>Description</CTableHeaderCell>
        </CTableRow>
      </CTableHead>
      <CTableBody>
        {actions.map((action) => (
          <CTableRow key={action.id}>
            <CTableDataCell>{action.id}</CTableDataCell>
            <CTableDataCell>{action.label}</CTableDataCell>
            <CTableDataCell>{action.description}</CTableDataCell>
          </CTableRow>
        ))}
      </CTableBody>
    </CTable>
  );
};

export default ActionsPanelContent;
