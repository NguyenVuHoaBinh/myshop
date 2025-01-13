import axios from 'axios';

// Create an axios instance
const axiosInstance = axios.create({
  baseURL:  'http://localhost:8888', // Change to your API URL
  timeout: 10000, // Optional: Set request timeout in milliseconds
});

// Add a request interceptor, if needed
axiosInstance.interceptors.request.use(
  (config) => {
    // Add authorization token to headers if needed
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor, if needed
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle errors globally
    console.error('API Error:', error);
    alert('An error occurred while making a request.');
    return Promise.reject(error);
  }
);

export default axiosInstance;