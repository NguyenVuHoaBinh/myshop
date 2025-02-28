// TopicEditorPage.jsx
import React, { useEffect, useState } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CForm,
  CFormLabel,
  CFormInput,
  CFormTextarea,
  CListGroup,
  CListGroupItem,
  CModal,
  CModalHeader,
  CModalTitle,
  CModalBody,
  CModalFooter,
  CSpinner,
} from '@coreui/react'
import { useNavigate, useParams } from 'react-router-dom'
import axiosInstance from '../../api/axiosInstance'; // Adjust the path as needed

const TopicEditorPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const isNew = id === 'new'

  // Topic state holds basic metadata and flow IDs (to be sent to the backend)
  const [topic, setTopic] = useState({
    name: '',
    description: '',
    flowIds: [],
  })

  // selectedFlows holds flow objects (with id and name) for display purposes.
  const [selectedFlows, setSelectedFlows] = useState([])

  // State for available flows in the modal.
  const [availableFlows, setAvailableFlows] = useState([])
  const [showFlowModal, setShowFlowModal] = useState(false)
  const [loadingFlows, setLoadingFlows] = useState(false)

  // Load topic if editing an existing one.
  useEffect(() => {
    if (!isNew) {
      axiosInstance
        .get(`/api/topics/${id}`)
        .then((response) => {
          setTopic(response.data)
        })
        .catch((error) => console.error(error))
    }
  }, [id, isNew])

  // Load available flows as soon as the component mounts.
  useEffect(() => {
    loadAvailableFlows()
  }, [])

  // Once available flows are loaded, if editing, update selectedFlows based on topic.flowIds.
  useEffect(() => {
    if (!isNew && availableFlows.length > 0 && topic.flowIds) {
      const flows = availableFlows.filter(flow => topic.flowIds.includes(flow.id))
      setSelectedFlows(flows)
    }
  }, [isNew, availableFlows, topic.flowIds])

  // Function to load available flows from the backend.
  const loadAvailableFlows = () => {
    setLoadingFlows(true)
    axiosInstance
      .get('/api/v1/flows/summary?page=0&size=100')
      .then((response) => {
        setAvailableFlows(response.data.flows || [])
        setLoadingFlows(false)
      })
      .catch((error) => {
        console.error(error)
        setLoadingFlows(false)
      })
  }

  const handleChange = (e) => {
    setTopic({ ...topic, [e.target.name]: e.target.value })
  }

  const handleOpenFlowModal = () => {
    setShowFlowModal(true)
  }

  const handleCloseFlowModal = () => {
    setShowFlowModal(false)
  }

  // When a flow is selected, add it if not already selected.
  const handleSelectFlow = (flow) => {
    if (!selectedFlows.find(sf => sf.id === flow.id)) {
      const newSelectedFlows = [...selectedFlows, flow]
      setSelectedFlows(newSelectedFlows)
      // Update topic.flowIds with the selected flow IDs.
      setTopic({ ...topic, flowIds: newSelectedFlows.map(f => f.id) })
    }
    setShowFlowModal(false)
  }

  const handleRemoveFlow = (flowId) => {
    const newSelectedFlows = selectedFlows.filter(flow => flow.id !== flowId)
    setSelectedFlows(newSelectedFlows)
    setTopic({ ...topic, flowIds: newSelectedFlows.map(f => f.id) })
  }

  const handleSave = () => {
    if (isNew) {
      axiosInstance
        .post('/api/topics', topic)
        .then(() => {
          window.alert('Saved Completed')
          navigate('/topics')
        })
        .catch((error) => console.error(error))
    } else {
      axiosInstance
        .put(`/api/topics/${id}`, topic)
        .then(() => {
          window.alert('Saved Completed')
          navigate('/topics')
        })
        .catch((error) => console.error(error))
    }
  }

  return (
    <>
      <CCard>
        <CCardHeader>
          <h3>{isNew ? 'Create Topic' : 'Edit Topic'}</h3>
        </CCardHeader>
        <CCardBody>
          <CForm>
            <div className="mb-3">
              <CFormLabel htmlFor="name">Name</CFormLabel>
              <CFormInput
                type="text"
                id="name"
                name="name"
                value={topic.name}
                onChange={handleChange}
              />
            </div>
            <div className="mb-3">
              <CFormLabel htmlFor="description">Description</CFormLabel>
              <CFormTextarea
                id="description"
                name="description"
                value={topic.description}
                onChange={handleChange}
              />
            </div>
            <div className="mb-3">
              <CFormLabel>Flows</CFormLabel>
              <CListGroup className="mb-2">
                {selectedFlows.map((flow, index) => (
                  <CListGroupItem
                    key={index}
                    className="d-flex justify-content-between align-items-center"
                  >
                    {flow.name}
                    <CButton color="danger" size="sm" onClick={() => handleRemoveFlow(flow.id)}>
                      Remove
                    </CButton>
                  </CListGroupItem>
                ))}
              </CListGroup>
              <CButton color="primary" onClick={handleOpenFlowModal}>
                Add Flow
              </CButton>
            </div>
            <div className="mt-4">
              <CButton color="success" onClick={handleSave} className="me-2">
                Save
              </CButton>
              <CButton color="secondary" onClick={() => navigate('/topics')}>
                Cancel
              </CButton>
            </div>
          </CForm>
        </CCardBody>
      </CCard>

      <CModal visible={showFlowModal} onClose={handleCloseFlowModal}>
        <CModalHeader onClose={handleCloseFlowModal}>
          <CModalTitle>Select Flow</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {loadingFlows ? (
            <div className="text-center">
              <CSpinner color="primary" />
            </div>
          ) : (
            <CListGroup>
              {availableFlows.map((flow) => (
                <CListGroupItem
                  key={flow.id}
                  className="d-flex justify-content-between align-items-center"
                >
                  <div>
                    <strong>{flow.name}</strong>
                    <div>{flow.description}</div>
                  </div>
                  <CButton color="primary" size="sm" onClick={() => handleSelectFlow(flow)}>
                    Add
                  </CButton>
                </CListGroupItem>
              ))}
            </CListGroup>
          )}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={handleCloseFlowModal}>
            Close
          </CButton>
        </CModalFooter>
      </CModal>
    </>
  )
}

export default TopicEditorPage
