import React, { useEffect, useState } from "react";
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CModal,
  CModalHeader,
  CModalBody,
  CModalFooter,
  CToast,
  CToastBody,
  CToaster,
} from "@coreui/react";
import { useNavigate } from "react-router-dom"; // For navigation
import { useTable } from "react-table";
import axios from "axios";

/**
 * FlowManager:
 * 1) Fetch minimal flow summaries from "/api/v1/flows/summary"
 * 2) Display them in a table (only ID, Name, Description, or whichever minimal fields).
 * 3) On Edit, fetch the full flow from "/api/v1/flows/{flowId}" before showing the modal.
 * 4) On Save, either create or update the flow, then re-fetch the summaries.
 */
const FlowManager = () => {
  const [flowSummaries, setFlowSummaries] = useState([]);  // minimal data: id, name, description
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentFlow, setCurrentFlow] = useState(null);    // full flow when editing
  const [toast, setToast] = useState(null);

  // Adjust these to match your real endpoints
  const API_BASE_URL = "http://localhost:8888/api/v1/flows";
  const navigate = useNavigate(); // Initialize navigation

  // Table columns: display only minimal info (e.g., name, description)
  const columns = React.useMemo(
    () => [
      { Header: "Name", accessor: "name" },
      { Header: "Description", accessor: "description" },
      {
        Header: "Actions",
        Cell: ({ row }) => (
          <div>
            <CButton
              color="info"
              size="sm"
              onClick={() => handleEditClick(row.original.id)}
            >
              Edit
            </CButton>
            <CButton
              color="warning"
              size="sm"
              style={{ marginLeft: "5px" }}
              onClick={() => navigate(`/floweditor/${row.original.id}`)} // Redirect to FlowEditor
            >
              Edit Visually
            </CButton>
            <CButton
              color="danger"
              size="sm"
              style={{ marginLeft: "5px" }}
              onClick={() => handleDelete(row.original.id)}
            >
              Delete
            </CButton>
          </div>
        ),
      },
    ],
    [navigate]
  );

  const { getTableProps, getTableBodyProps, headerGroups, rows, prepareRow } =
    useTable({ columns, data: flowSummaries });

  // Fetch the minimal flow summaries on mount
  useEffect(() => {
    fetchFlowSummaries();
  }, []);

  /**
   * Fetch minimal flows (summaries) from /api/v1/flows/summary
   */
  const fetchFlowSummaries = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/summary`);
      // Expecting something like:  { flows: [ {id, name, description}, ... ], timestamp: "..." }
      const fetched = response.data?.flows || [];

      setFlowSummaries(fetched);
      if (fetched.length === 0) {
        showToast("No flows available. Please create a new flow.", "warning");
      }
    } catch (error) {
      console.error("Error fetching flow summaries:", error);
      showToast("Error fetching flows. Please try again later.", "danger");
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handle user clicking "Edit": fetch the full flow from /api/v1/flows/{flowId},
   * then open the modal.
   */
  const handleEditClick = async (flowId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/${flowId}`);
      // The response structure might be { flow: {id, name, description, role, purpose, ...}, timestamp: "..." }
      const fullFlow = response.data?.flow;
      setCurrentFlow(fullFlow);
      setModalVisible(true);
    } catch (error) {
      console.error("Error fetching full flow:", error);
      showToast("Error fetching flow details. Please try again.", "danger");
    }
  };

  /**
   * Create or update the flow
   */
  const handleCreateOrUpdate = async () => {
    try {
      // If currentFlow.id exists, we do PUT, otherwise POST
      if (currentFlow?.id) {
        await axios.put(`${API_BASE_URL}/${currentFlow.id}`, currentFlow);
        showToast("Flow updated successfully", "success");
      } else {
        await axios.post(API_BASE_URL, currentFlow);
        showToast("Flow created successfully", "success");
      }
      // Refresh the summary list and close modal
      fetchFlowSummaries();
      setModalVisible(false);
    } catch (error) {
      console.error("Error saving flow:", error);
      showToast("Error saving flow. Please try again.", "danger");
    }
  };

  /**
   * Delete a flow by ID
   */
  const handleDelete = async (flowId) => {
    if (window.confirm("Are you sure you want to delete this flow?")) {
      try {
        await axios.delete(`${API_BASE_URL}/${flowId}`);
        showToast("Flow deleted successfully", "success");
        fetchFlowSummaries();
      } catch (error) {
        console.error("Error deleting flow:", error);
        showToast("Error deleting flow. Please try again.", "danger");
      }
    }
  };

  /**
   * Show a toast for messages
   */
  const showToast = (message, color) => {
    setToast(
      <CToast color={color}>
        <CToastBody>{message}</CToastBody>
      </CToast>
    );
    setTimeout(() => setToast(null), 3000);
  };

  return (
    <CCard>
      <CCardHeader>
        <h5>Flow Manager</h5>
        <CButton
          color="primary"
          onClick={() => {
            // Clear currentFlow to create a brand new one
            setCurrentFlow({ name: "", description: "", role: "", purpose: "" });
            setModalVisible(true);
          }}
        >
          Create New Flow
        </CButton>
      </CCardHeader>
      <CCardBody>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <table {...getTableProps()} className="table table-bordered">
            <thead>
              {headerGroups.map((headerGroup, headerIndex) => (
                <tr
                  {...headerGroup.getHeaderGroupProps()}
                  key={headerGroup.id || headerIndex}
                >
                  {headerGroup.headers.map((column, columnIndex) => {
                    const { key, ...rest } = column.getHeaderProps();
                    return (
                      <th {...rest} key={key || columnIndex}>
                        {column.render("Header")}
                      </th>
                    );
                  })}
                </tr>
              ))}
            </thead>
            <tbody {...getTableBodyProps()}>
              {rows.map((row, rowIndex) => {
                prepareRow(row);
                return (
                  <tr
                    {...row.getRowProps()}
                    key={row.id || row.original.id || rowIndex}
                  >
                    {row.cells.map((cell, cellIndex) => (
                      <td
                        {...cell.getCellProps()}
                        key={cell.column.id || cellIndex}
                      >
                        {cell.render("Cell")}
                      </td>
                    ))}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </CCardBody>

      {/* Modal for Create/Edit (full flow details) */}
      <CModal show={modalVisible} onClose={() => setModalVisible(false)}>
        <CModalHeader>
          {currentFlow?.id ? "Edit Flow" : "Create Flow"}
        </CModalHeader>
        <CModalBody>
          <form>
            <div className="mb-3">
              <label className="form-label">Name</label>
              <input
                type="text"
                className="form-control"
                value={currentFlow?.name || ""}
                onChange={(e) =>
                  setCurrentFlow({ ...currentFlow, name: e.target.value })
                }
              />
            </div>

            {/* If you want to show "Description" in the form, include it here */}
            <div className="mb-3">
              <label className="form-label">Description</label>
              <input
                type="text"
                className="form-control"
                value={currentFlow?.description || ""}
                onChange={(e) =>
                  setCurrentFlow({ ...currentFlow, description: e.target.value })
                }
              />
            </div>

            {/* Role field */}
            <div className="mb-3">
              <label className="form-label">Role</label>
              <input
                type="text"
                className="form-control"
                value={currentFlow?.role || ""}
                onChange={(e) =>
                  setCurrentFlow({ ...currentFlow, role: e.target.value })
                }
              />
            </div>

            {/* Purpose field */}
            <div className="mb-3">
              <label className="form-label">Purpose</label>
              <textarea
                className="form-control"
                value={currentFlow?.purpose || ""}
                onChange={(e) =>
                  setCurrentFlow({ ...currentFlow, purpose: e.target.value })
                }
              />
            </div>
          </form>
        </CModalBody>
        <CModalFooter>
          <CButton color="primary" onClick={handleCreateOrUpdate}>
            Save
          </CButton>
          <CButton color="secondary" onClick={() => setModalVisible(false)}>
            Cancel
          </CButton>
        </CModalFooter>
      </CModal>

      {/* Toast Notifications */}
      <CToaster>{toast}</CToaster>
    </CCard>
  );
};

export default FlowManager;
