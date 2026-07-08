export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
  }
  
  export interface LoginRequest {
    email: string;
    password: string;
  }
  
  export interface UserResponse {
    id: string;
    name: string;
    email: string;
    storageUsedBytes: number;
    storageLimitBytes: number;
  }
  
  export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    user: UserResponse;
  }