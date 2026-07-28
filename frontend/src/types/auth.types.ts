export interface User {
  id: string;
  fullName: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: String;
}

export interface LoginPayload {
  email: string;
  password: String;
}

export interface RefreshTokenPayload {
  refreshToken: string;
}

export interface ApiErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  validationErrors?: Record<string, string>;
}
