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

const FlowManager = () => {
  const [flows, setFlows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentFlow, setCurrentFlow] = useState(null);
  const [toast, setToast] = useState(null);

  const API_BASE_URL = "http://localhost:8888/api/v1/flows";
  const navigate = useNavigate(); // Initialize navigation

  const columns = React.useMemo(
    () => [
      { Header: "Name", accessor: "name" },
      { Header: "Role", accessor: "role" },
      { Header: "Purpose", accessor: "purpose" },
      { Header: "Created At", accessor: "createdAt" },
      {
        Header: "Actions",
        Cell: ({ row }) => (
          <div>
            <CButton
              color="info"
              size="sm"
              onClick={() => {
                setCurrentFlow(row.original);
                setModalVisible(true);
              }}
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
    useTable({ columns, data: flows });

  useEffect(() => {
    fetchFlows();
  }, []);

  const fetchFlows = async () => {
    setLoading(true);
    try {
      const response = await axios.get(API_BASE_URL);

      const fetchedFlows = Array.isArray(response.data)
        ? response.data
        : response.data?.data || [];

      setFlows(fetchedFlows);

      if (fetchedFlows.length === 0) {
        showToast("No flows available. Please create a new flow.", "warning");
      }
    } catch (error) {
      console.error("Error fetching flows:", error);
      showToast("Error fetching flows. Please try again later.", "danger");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateOrUpdate = async () => {
    try {
      if (currentFlow.id) {
        await axios.put(`${API_BASE_URL}/${currentFlow.id}`, currentFlow);
        showToast("Flow updated successfully", "success");
      } else {
        await axios.post(API_BASE_URL, currentFlow);
        showToast("Flow created successfully", "success");
      }
      fetchFlows();
      setModalVisible(false);
    } catch (error) {
      showToast("Error saving flow. Please try again.", "danger");
    }
  };

  const handleDelete = async (flowId) => {
    if (window.confirm("Are you sure you want to delete this flow?")) {
      try {
        await axios.delete(`${API_BASE_URL}/${flowId}`);
        showToast("Flow deleted successfully", "success");
        fetchFlows();
      } catch (error) {
        showToast("Error deleting flow. Please try again.", "danger");
      }
    }
  };

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
          onClick={() => navigate("/floweditor")} // Redirect to FlowEditor for creating a new flow
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

      {/* Modal for Create/Edit */}
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
