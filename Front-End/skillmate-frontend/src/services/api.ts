import axios from "axios";

// API Base Configuration
const API_BASE_URL = "http://localhost:8080/api/v1";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Add token to requests if available
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName?: string;
}

export interface GenerateCoursePathRequest {
  subject: string;
  difficulty: string;
}

export interface EnrollCoursePathRequest {
  coursePathId: string;
}

export interface AddReviewRequest {
  coursePathId: string;
  rating: number;
  comment?: string;
}

export interface UpdateNameRequest {
  newFirstName: string;
  newLastName?: string;
}

export interface ResetPasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// Authentication API
export const authAPI = {
  login: (data: LoginRequest) => api.post("/auth/login", data),
  signup: (data: SignupRequest) => api.post("/auth/signup", data),
};

// User API
export const userAPI = {
  getInfo: () => api.get("/user/info"),
  getProfile: () => api.get("/user/profile"),
  updateName: (data: UpdateNameRequest) => api.put("/user/update/name", data),
  updatePassword: (data: ResetPasswordRequest) =>
    api.put("/user/update/password", data),
  deleteAccount: () => api.delete("/user/delete"),
};

// Course Path API
export const coursePathAPI = {
  generate: (data: GenerateCoursePathRequest) =>
    api.post("/content/course-path/generate", data),
  getMine: () => api.get("/content/course-path/mine"),
  getById: (id: string) => api.get(`/content/course-path/${id}`),
  enroll: (data: EnrollCoursePathRequest) =>
    api.post("/content/course-path/enroll", data),
  addReview: (data: AddReviewRequest) =>
    api.post("/content/course-path/review", data),
  getProgress: (coursePathId: string) =>
    api.get(`/content/course-path/progress/${coursePathId}`),
};

export default api;
