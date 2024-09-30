import api from '@/app/common/utils/api';
import { User } from '@/app/common/types';

export const userService = {
  getUsers: async (): Promise<User[]> => {
    const response = await api.get('/api/users');
    return response.data;
  },

  createUser: async (userData: Omit<User, 'id'>): Promise<User> => {
    const response = await api.post('/api/users', userData);
    return response.data;
  },

  // Add other user-related API calls here (e.g., updateUser, deleteUser)
};
