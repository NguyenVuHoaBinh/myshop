import React, { useState } from 'react';
import {
  CModal,
  CModalHeader,
  CModalBody,
  CModalFooter,
  CButton,
  CForm,
  CFormLabel,
  CFormInput,
  CFormTextarea,
  CFormCheck,
  CRow,
  CCol,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
} from '@coreui/react';
import { FaSearch } from 'react-icons/fa';
import '@coreui/coreui/dist/css/coreui.min.css';

const CreateTopicModal = ({ visible, toggle }) => {
  const [step, setStep] = useState(1); // Step management: 1 for details, 2 for actions
  const [instructions, setInstructions] = useState(['']);
  const [userInputs, setUserInputs] = useState(['']);
  const [selectedActions, setSelectedActions] = useState([]);
  const [actionSearch, setActionSearch] = useState('');
  const availableActions = [
    { id: 1, label: 'Add Case Comment', description: 'Let a customer add a comment to an existing case.' },
    { id: 2, label: 'Answer Questions with Knowledge', description: 'Answers questions about company policies and procedures.' },
    { id: 3, label: 'Cancel Order', description: 'Cancels a customer’s order.' },
    { id: 4, label: 'Check Weather', description: 'Check weather at Coral Cloud Resorts at a specific time.' },
    { id: 5, label: 'Create a Label', description: 'Create a label with the specified label name.' },
    { id: 6, label: 'Create a To Do', description: 'Create a task record based on user input.' },
    { id: 7, label: 'Create Case', description: 'Let a customer create a case.' },
    // Add more actions as needed...
  ];

  const addInstruction = () => {
    setInstructions([...instructions, '']);
  };

  const addUserInput = () => {
    setUserInputs([...userInputs, '']);
  };

  const handleInstructionChange = (index, value) => {
    const newInstructions = [...instructions];
    newInstructions[index] = value;
    setInstructions(newInstructions);
  };

  const handleUserInputChange = (index, value) => {
    const newUserInputs = [...userInputs];
    newUserInputs[index] = value;
    setUserInputs(newUserInputs);
  };

  const handleActionSelect = (actionId) => {
    setSelectedActions((prevSelectedActions) =>
      prevSelectedActions.includes(actionId)
        ? prevSelectedActions.filter((id) => id !== actionId)
        : [...prevSelectedActions, actionId]
    );
  };

  const handleSubmit = () => {
    console.log({ instructions, userInputs, selectedActions });
    toggle();
  };

  const filteredActions = availableActions.filter((action) =>
    action.label.toLowerCase().includes(actionSearch.toLowerCase())
  );

  return (
    <CModal visible={visible} onClose={toggle} size="lg">
      <CModalHeader onClose={toggle}>Create a Topic</CModalHeader>
      <CModalBody>
        <CForm>
          {step === 1 && (
            <>
              <div className="mb-3">
                <CFormLabel>Topic Label</CFormLabel>
                <CFormInput type="text" placeholder="Enter a topic label..." />
              </div>

              <div className="mb-3">
                <CFormLabel>Classification Description</CFormLabel>
                <CFormTextarea placeholder="In 1-3 sentences, describe what your topic does..." />
              </div>

              <div className="mb-3">
                <CFormLabel>Scope</CFormLabel>
                <CFormTextarea placeholder="Give your topic a job description, be specific..." />
              </div>

              <div className="mb-3">
                <CFormLabel>Instructions</CFormLabel>
                {instructions.map((instruction, index) => (
                  <CRow key={index} className="mb-2">
                    <CCol>
                      <CFormInput
                        type="text"
                        value={instruction}
                        onChange={(e) => handleInstructionChange(index, e.target.value)}
                        placeholder="Define a topic-specific instruction..."
                      />
                    </CCol>
                  </CRow>
                ))}
                <CButton color="primary" onClick={addInstruction}>
                  Add Instruction
                </CButton>
              </div>

              <div className="mb-3">
                <CFormLabel>Example User Input</CFormLabel>
                {userInputs.map((userInput, index) => (
                  <CRow key={index} className="mb-2">
                    <CCol>
                      <CFormInput
                        type="text"
                        value={userInput}
                        onChange={(e) => handleUserInputChange(index, e.target.value)}
                        placeholder="Example User Input"
                      />
                    </CCol>
                  </CRow>
                ))}
                <CButton color="primary" onClick={addUserInput}>
                  Add Example Input
                </CButton>
              </div>
            </>
          )}

          {step === 2 && (
            <>
              <div className="mb-3">
                <CFormLabel>Select Actions</CFormLabel>
                <div className="mb-3" style={{ display: 'flex', alignItems: 'center' }}>
                  <CFormInput
                    type="text"
                    placeholder="Search actions..."
                    value={actionSearch}
                    onChange={(e) => setActionSearch(e.target.value)}
                  />
                  <FaSearch style={{ marginLeft: '8px' }} />
                </div>

                <CTable hover>
                  <CTableHead>
                    <CTableRow>
                      <CTableHeaderCell>Select</CTableHeaderCell>
                      <CTableHeaderCell>Agent Action Label</CTableHeaderCell>
                      <CTableHeaderCell>Instructions</CTableHeaderCell>
                    </CTableRow>
                  </CTableHead>
                  <CTableBody>
                    {filteredActions.map((action) => (
                      <CTableRow key={action.id}>
                        <CTableDataCell>
                          <CFormCheck
                            type="checkbox"
                            id={`action-${action.id}`}
                            checked={selectedActions.includes(action.id)}
                            onChange={() => handleActionSelect(action.id)}
                          />
                        </CTableDataCell>
                        <CTableDataCell>{action.label}</CTableDataCell>
                        <CTableDataCell>{action.description}</CTableDataCell>
                      </CTableRow>
                    ))}
                  </CTableBody>
                </CTable>
              </div>
            </>
          )}
        </CForm>
      </CModalBody>
      <CModalFooter>
        {step > 1 && (
          <CButton color="secondary" onClick={() => setStep(step - 1)}>
            Back
          </CButton>
        )}
        {step < 2 && (
          <CButton color="primary" onClick={() => setStep(step + 1)}>
            Next
          </CButton>
        )}
        {step === 2 && (
          <CButton color="primary" onClick={handleSubmit}>
            Finish
          </CButton>
        )}
      </CModalFooter>
    </CModal>
  );
};

export default CreateTopicModal;
