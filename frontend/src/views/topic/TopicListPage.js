import React, { useEffect, useState } from 'react'
import {
  CCard,
  CCardBody,
  CCardHeader,
  CButton,
  CTable,
  CTableHead,
  CTableRow,
  CTableHeaderCell,
  CTableBody,
  CTableDataCell,
} from '@coreui/react'
import { useNavigate } from 'react-router-dom'
import axiosInstance from '../../api/axiosInstance'; // Adjust the path as needed

const TopicListPage = () => {
  const [topics, setTopics] = useState([])
  const navigate = useNavigate()

  // Fetch topics from backend API
  useEffect(() => {
    axiosInstance
      .get('/api/topics')
      .then((response) => setTopics(response.data))
      .catch((error) => console.error(error))
  }, [])

  const handleEdit = (id) => {
    navigate(`/topic-editor/${id}`)
  }

  const handleDelete = (id) => {
    if (window.confirm('Are you sure you want to delete this topic?')) {
      axiosInstance
        .delete(`/api/topics/${id}`)
        .then(() => {
          // Remove the deleted topic from local state
          setTopics(topics.filter((topic) => topic.id !== id))
        })
        .catch((error) => console.error(error))
    }
  }

  const handleCreate = () => {
    navigate('/topic-editor/new')
  }

  return (
    <CCard>
      <CCardHeader>
        <div className="d-flex justify-content-between align-items-center">
          <h3>Topics</h3>
          <CButton color="primary" onClick={handleCreate}>
            Create New Topic
          </CButton>
        </div>
      </CCardHeader>
      <CCardBody>
        <CTable hover responsive>
          <CTableHead>
            <CTableRow>
              <CTableHeaderCell>Name</CTableHeaderCell>
              <CTableHeaderCell>Description</CTableHeaderCell>
              <CTableHeaderCell>Actions</CTableHeaderCell>
            </CTableRow>
          </CTableHead>
          <CTableBody>
            {topics.map((topic) => (
              <CTableRow key={topic.id}>
                <CTableDataCell>{topic.name}</CTableDataCell>
                <CTableDataCell>{topic.description}</CTableDataCell>
                <CTableDataCell>
                  <CButton
                    color="info"
                    size="sm"
                    onClick={() => handleEdit(topic.id)}
                    className="me-2"
                  >
                    Edit
                  </CButton>
                  <CButton
                    color="danger"
                    size="sm"
                    onClick={() => handleDelete(topic.id)}
                  >
                    Delete
                  </CButton>
                </CTableDataCell>
              </CTableRow>
            ))}
          </CTableBody>
        </CTable>
      </CCardBody>
    </CCard>
  )
}

export default TopicListPage
