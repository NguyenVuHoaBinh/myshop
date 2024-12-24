import React, { useState, useEffect } from 'react';
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CFormInput,
  CFormTextarea,
  CFormSelect,
  CRow,
  CCol,
} from '@coreui/react';
import axios from 'axios';

const CreateAgentPage = ({ onCancel, onFinish }) => {
  const [step, setStep] = useState(1);
  const [agentData, setAgentData] = useState({
    type: '',
    topics: [],
    name: '',
    description: '',
    role: '',
    company: '',
    user: '',
    library: '',
  });

  const [types, setTypes] = useState([]);
  const [topics, setTopics] = useState([]);
  const [users, setUsers] = useState([]);
  const [libraries, setLibraries] = useState([]);
  const [loading, setLoading] = useState(false);

  // Utility to fetch data with fallback to mock
  const fetchData = async (url, setState, fallbackData) => {
    try {
      setLoading(true);
      const response = await axios.get(url);
      if (Array.isArray(response.data)) {
        setState(response.data);
      } else {
        console.warn(`Invalid response for ${url}. Using fallback data.`);
        setState(fallbackData);
      }
    } catch (error) {
      console.error(`Error fetching data from ${url}. Using fallback data:`, error);
      setState(fallbackData);
    } finally {
      setLoading(false);
    }
  };

  // Fetch types
  useEffect(() => {
    fetchData(
      '/api/agent-types',
      setTypes,
      ['Copilot', 'Service Agent', 'Digital Channel']
    );
  }, []);

  // Fetch topics
  useEffect(() => {
    fetchData(
      '/api/agent-topics',
      setTopics,
      ['Case Management', 'Account Management', 'Reservation Management']
    );
  }, []);

  // Fetch users
  useEffect(() => {
    fetchData('/api/users', setUsers, ['User1', 'User2', 'User3']);
  }, []);

  // Fetch libraries
  useEffect(() => {
    fetchData('/api/libraries', setLibraries, ['Library1', 'Library2', 'Library3']);
  }, []);

  // Handle next step
  const handleNext = () => {
    if (step === 4) {
      console.log('Agent Data:', agentData); // Debugging output
      alert('Agent created successfully!');
      onFinish();
    } else {
      setStep(step + 1);
    }
  };

  // Handle input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setAgentData((prev) => ({ ...prev, [name]: value }));
  };

  // Handle adding or removing topics
  const toggleTopic = (topic) => {
    setAgentData((prev) => {
      const updatedTopics = prev.topics.includes(topic)
        ? prev.topics.filter((t) => t !== topic)
        : [...prev.topics, topic];
      return { ...prev, topics: updatedTopics };
    });
  };

  return (
    <div className="container mt-4">
      <CRow className="justify-content-center">
        <CCol md={8}>
          <CCard>
            <CCardHeader>
              <h2 className="text-center">Create New Agent</h2>
            </CCardHeader>
            <CCardBody>
              {loading ? (
                <p className="text-center">Loading data...</p>
              ) : (
                <>
                  {step === 1 && (
                    <div>
                      <h3 className="text-center mb-4">Select Type</h3>
                      <CRow className="justify-content-center">
                        {types.map((type, index) => (
                          <CCol xs="auto" key={index} className="mb-3">
                            <CButton
                              color={agentData.type === type ? 'primary' : 'secondary'}
                              onClick={() => setAgentData((prev) => ({ ...prev, type }))}
                              className="px-4"
                            >
                              {type}
                            </CButton>
                          </CCol>
                        ))}
                      </CRow>
                    </div>
                  )}
                  {step === 2 && (
                    <div>
                      <h3 className="text-center mb-4">Select Topics</h3>
                      {topics.map((topic, index) => (
                        <CRow
                          key={index}
                          className="align-items-center mb-3"
                        >
                          <CCol>{topic}</CCol>
                          <CCol xs="auto">
                            <CButton
                              color={agentData.topics.includes(topic) ? 'danger' : 'success'}
                              onClick={() => toggleTopic(topic)}
                            >
                              {agentData.topics.includes(topic) ? 'Remove' : 'Add'}
                            </CButton>
                          </CCol>
                        </CRow>
                      ))}
                    </div>
                  )}
                  {step === 3 && (
                    <div>
                      <h3 className="text-center mb-4">Define Settings</h3>
                      <CFormInput
                        name="name"
                        placeholder="Name"
                        value={agentData.name}
                        onChange={handleInputChange}
                        className="mb-3"
                      />
                      <CFormTextarea
                        name="description"
                        placeholder="Description"
                        value={agentData.description}
                        onChange={handleInputChange}
                        className="mb-3"
                      />
                      <CFormTextarea
                        name="role"
                        placeholder="Role"
                        value={agentData.role}
                        onChange={handleInputChange}
                        className="mb-3"
                      />
                      <CFormTextarea
                        name="company"
                        placeholder="Company"
                        value={agentData.company}
                        onChange={handleInputChange}
                        className="mb-3"
                      />
                      <CFormSelect
                        name="user"
                        value={agentData.user}
                        onChange={handleInputChange}
                        className="mb-3"
                      >
                        <option value="">Select User</option>
                        {users.map((user, index) => (
                          <option key={index} value={user}>
                            {user}
                          </option>
                        ))}
                      </CFormSelect>
                    </div>
                  )}
                  {step === 4 && (
                    <div>
                      <h3 className="text-center mb-4">Select Data</h3>
                      <CFormSelect
                        name="library"
                        value={agentData.library}
                        onChange={handleInputChange}
                      >
                        <option value="">Select Library</option>
                        {libraries.map((library, index) => (
                          <option key={index} value={library}>
                            {library}
                          </option>
                        ))}
                      </CFormSelect>
                    </div>
                  )}
                  <div className="d-flex justify-content-between mt-4">
                    <CButton color="secondary" onClick={onCancel}>
                      Cancel
                    </CButton>
                    <CButton color="primary" onClick={handleNext}>
                      {step === 4 ? 'Create' : 'Next'}
                    </CButton>
                  </div>
                </>
              )}
            </CCardBody>
          </CCard>
        </CCol>
      </CRow>
    </div>
  );
};

export default CreateAgentPage;
