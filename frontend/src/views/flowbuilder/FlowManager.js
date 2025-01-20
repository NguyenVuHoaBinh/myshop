import React, { useEffect, useState } from "react";
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CToast,
  CToastBody,
  CToaster,
} from "@coreui/react";
import { useNavigate } from "react-router-dom";
import { useTable } from "react-table";
import axios from "axios";

const FlowManager = () => {
  const [flowSummaries, setFlowSummaries] = useState([]); // minimal data: id, name, description
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState(null);
  const API_BASE_URL = "http://localhost:8888/api/v1/flows";
  const navigate = useNavigate();

  // Table columns: display minimal info (e.g., name, description)
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

  /**
   * Fetch flow summaries from /api/v1/flows/summary
   */
  const fetchFlowSummaries = async () => {
    setLoading(true);
    try {
      const response = await axios.get(`${API_BASE_URL}/summary`);
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

  useEffect(() => {
    fetchFlowSummaries();
  }, []);

  /**
   * Create a new flow and navigate to its editor
   */
  const handleCreateNewFlow = async () => {
    try {
      const defaultFlow = {
        name: "Untitled Flow",
        description: "",
        role: "default-role",
        purpose: "",
      };

      // Make POST request to create a new flow
      const response = await axios.post(API_BASE_URL, defaultFlow);
      const newFlowId = response.data?.flow?.id;

      if (newFlowId) {
        showToast("Flow created successfully!", "success");
        navigate(`/floweditor/${newFlowId}`); // Redirect to the FlowEditor
      } else {
        throw new Error("Failed to retrieve the ID of the created flow");
      }
    } catch (error) {
      console.error("Error creating new flow:", error);
      showToast("Error creating new flow. Please try again.", "danger");
    }
  };

  /**
   * Handle 'Edit' Flow: Fetch full flow details and navigate to editor
   */
  const handleEditClick = async (flowId) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/${flowId}`);
      const fullFlow = response.data?.flow;

      if (fullFlow) {
        navigate(`/floweditor/${flowId}`);
      } else {
        throw new Error("Flow not found");
      }
    } catch (error) {
      console.error("Error fetching full flow details:", error);
      showToast("Error fetching flow details. Please try again.", "danger");
    }
  };

  /**
   * Handle 'Delete' Flow: Deletes by ID
   */
  const handleDelete = async (flowId) => {
    if (window.confirm("Are you sure you want to delete this flow?")) {
      try {
        await axios.delete(`${API_BASE_URL}/${flowId}`);
        showToast("Flow deleted successfully!", "success");
        fetchFlowSummaries();
      } catch (error) {
        console.error("Error deleting flow:", error);
        showToast("Error deleting flow. Please try again.", "danger");
      }
    }
  };

  /**
   * Show a toast notification
   */
  const showToast = (message, color) => {
    setToast(
      <CToast color={color}>
        <CToastBody>{message}</CToastBody>
      </CToast>
    );
    setTimeout(() => setToast(null), 3000);
  };

  /**
   * Render the FlowManager component
   */
  return (
    <CCard>
      <CCardHeader>
        <h5>Flow Manager</h5>
        <CButton color="primary" onClick={handleCreateNewFlow}>
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
                <tr {...headerGroup.getHeaderGroupProps()} key={headerIndex}>
                  {headerGroup.headers.map((column, columnIndex) => {
                    const { key, ...rest } = column.getHeaderProps();
                    return (
                      <th key={key || columnIndex} {...rest}>
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
                  <tr {...row.getRowProps()} key={rowIndex}>
                    {row.cells.map((cell, cellIndex) => {
                      const { key, ...rest } = cell.getCellProps();
                      return (
                        <td key={key || cellIndex} {...rest}>
                          {cell.render("Cell")}
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </CCardBody>
      {/* Toast Notifications */}
      <CToaster>{toast}</CToaster>
    </CCard>
  );
};

export default FlowManager;