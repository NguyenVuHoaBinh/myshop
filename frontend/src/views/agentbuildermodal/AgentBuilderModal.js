import React, { useState, useEffect } from "react";
import { FaList, FaTasks, FaBook, FaClipboardList } from "react-icons/fa";
import TopicsPanel from "./TopicsPanel";
import ActionsPanelContent from "./ActionsPanel";
import KnowledgePanel from "./KnowledgePanel";
import LogsPanel from "./LogsPanel";
import ConversationPanel from "./ConversationPanel";
import CreateTopicModal from './CreateTopicModal';
import { CModal, CModalHeader, CModalBody, CModalFooter, CButton, CFormCheck, CTable, CTableHead, CTableRow, CTableHeaderCell, CTableBody, CTableDataCell } from "@coreui/react";
import axios from "axios";

const AgentBuilderModal = ({ agent }) => {
  const [activeTab, setActiveTab] = useState(1);
  const [topics, setTopics] = useState([]);
  const [actions, setActions] = useState([]);
  const [conversation, setConversation] = useState([]);
  const [userPrompt, setUserPrompt] = useState("");
  const [showLibraryModal, setShowLibraryModal] = useState(false);
  const [showCreateTopicModal, setShowCreateTopicModal] = useState(false);

  // Fetch Topics
  useEffect(() => {
    const fetchTopics = async () => {
      try {
        const response = await axios.get("/api/topics");
        setTopics(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error("Error fetching topics, using fallback data:", error);
        setTopics([
          { id: 1, label: "Order Assistance", description: "Helps with order-related inquiries.", scope: "Order related queries" },
          { id: 2, label: "General Inquiry", description: "Handles general user inquiries.", scope: "General queries" },
        ]);
      }
    };
    fetchTopics();
  }, []);

  // Fetch Actions
  useEffect(() => {
    const fetchActions = async () => {
      try {
        const response = await axios.get("/api/actions");
        setActions(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error("Error fetching actions, using fallback data:", error);
        setActions([
          { id: 1, label: "Create Order", description: "Create a new order for a customer." },
          { id: 2, label: "Cancel Order", description: "Cancel an existing order." },
        ]);
      }
    };
    fetchActions();
  }, []);

  // Handle Sending User Prompts
  const handleSendPrompt = () => {
    if (!userPrompt.trim()) return;
    setConversation((prev) => [...prev, { sender: "user", message: userPrompt }]);
    setTimeout(() => {
      setConversation((prev) => [...prev, { sender: "agent", message: `You said: ${userPrompt}` }]);
    }, 1000);
    setUserPrompt("");
  };

  return (
    <div className="agent-builder-page" style={{ display: 'flex', height: '100vh', backgroundColor: '#f0f2f5' }}>
      {/* Sidebar with Icons */}
      <div className="sidebar" style={{ width: '60px', backgroundColor: '#343a40', color: '#fff', padding: '10px', boxShadow: '2px 0 5px rgba(0, 0, 0, 0.1)' }}>
        {[
          { id: 1, icon: <FaList size={24} />, label: 'Topics' },
          { id: 2, icon: <FaTasks size={24} />, label: 'Actions' },
          { id: 3, icon: <FaBook size={24} />, label: 'Knowledge' },
          { id: 4, icon: <FaClipboardList size={24} />, label: 'Logs' },
        ].map(({ id, icon, label }) => (
          <div
            key={id}
            className={`sidebar-icon ${activeTab === id ? 'active' : ''}`}
            onClick={() => setActiveTab(id)}
            style={{ textAlign: 'center', marginBottom: '20px', cursor: 'pointer', padding: '10px', borderRadius: '8px', backgroundColor: activeTab === id ? '#007bff' : 'transparent' }}
          >
            {icon}
            <div style={{ fontSize: '10px', marginTop: '5px', color: activeTab === id ? '#fff' : '#adb5bd' }}>{label}</div>
          </div>
        ))}
      </div>

      {/* Main Content Area */}
      <div className="main-content" style={{ display: 'flex', flexGrow: 1, padding: '20px', gap: '20px' }}>
        {/* Dynamic Content Panel */}
        <div className="dynamic-panel" style={{ flex: 2, backgroundColor: '#ffffff', padding: '30px', borderRadius: '8px', boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.1)', overflowY: 'auto' }}>
          {activeTab === 1 && (
            <TopicsPanel
              topics={topics}
              handleShowLibraryModal={() => setShowLibraryModal(true)}
              handleShowCreateTopicModal={() => setShowCreateTopicModal(true)}
            />
          )}
          {activeTab === 2 && <ActionsPanelContent actions={actions} />}
          {activeTab === 3 && <KnowledgePanel />}
          {activeTab === 4 && <LogsPanel />}
        </div>

        {/* Static Panel (Agent Logic) */}
        <div className="static-panel" style={{ flex: 1.5, backgroundColor: '#ffffff', padding: '30px', borderRadius: '8px', boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.1)', overflowY: 'auto' }}>
          <h2>Agent Logic</h2>
          <p style={{ color: '#6c757d' }}>Coming soon: This section will display the logic and flow of how the agent handles different tasks.</p>
        </div>

        {/* Right Conversation Panel */}
        <div className="conversation-panel" style={{ width: '400px', backgroundColor: '#ffffff', padding: '20px', borderRadius: '8px', boxShadow: '0px 4px 8px rgba(0, 0, 0, 0.1)', display: 'flex', flexDirection: 'column', overflowY: 'auto' }}>
          <ConversationPanel
            conversation={conversation}
            userPrompt={userPrompt}
            setUserPrompt={setUserPrompt}
            handleSendPrompt={handleSendPrompt}
          />
        </div>
      </div>

      {/* Add From Library Modal */}
      <CModal visible={showLibraryModal} onClose={() => setShowLibraryModal(false)} size="lg">
        <CModalHeader>Add from Asset Library</CModalHeader>
        <CModalBody>
          <CTable>
            <CTableHead>
              <CTableRow>
                <CTableHeaderCell>Select</CTableHeaderCell>
                <CTableHeaderCell>Topic Label</CTableHeaderCell>
                <CTableHeaderCell>Classification Description</CTableHeaderCell>
                <CTableHeaderCell>Scope</CTableHeaderCell>
              </CTableRow>
            </CTableHead>
            <CTableBody>
              {topics.map((topic) => (
                <CTableRow key={topic.id}>
                  <CTableDataCell><CFormCheck /></CTableDataCell>
                  <CTableDataCell>{topic.label}</CTableDataCell>
                  <CTableDataCell>{topic.description}</CTableDataCell>
                  <CTableDataCell>{topic.scope || "General Scope"}</CTableDataCell>
                </CTableRow>
              ))}
            </CTableBody>
          </CTable>
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setShowLibraryModal(false)}>Cancel</CButton>
          <CButton color="primary">Finish</CButton>
        </CModalFooter>
      </CModal>

      {/* Create Topic Modal */}
      <CreateTopicModal visible={showCreateTopicModal} toggle={() => setShowCreateTopicModal(false)} />
    </div>
  );
};

export default AgentBuilderModal;
