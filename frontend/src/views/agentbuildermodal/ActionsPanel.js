import React from "react";
import { CTable, CTableHead, CTableRow, CTableHeaderCell, CTableBody, CTableDataCell } from "@coreui/react";

const ActionsPanel = ({ actions }) => (
  <div className="actions-panel">
    <h3>Agent Actions</h3>
    <p>These actions are assigned to your agent. You can add or remove actions from the Topics panel.</p>
    <CTable>
      <CTableHead>
        <CTableRow>
          <CTableHeaderCell>Agent Action Label</CTableHeaderCell>
        </CTableRow>
      </CTableHead>
      <CTableBody>
        {actions.map((action) => (
          <CTableRow key={action.id}>
            <CTableDataCell>{action.label}</CTableDataCell>
          </CTableRow>
        ))}
      </CTableBody>
    </CTable>
  </div>
);

export default ActionsPanel;
