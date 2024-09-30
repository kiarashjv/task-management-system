import { useState, useEffect } from 'react';
import { User } from '@/app/common/types';
import { userService } from '@/app/settings/usermanagment/services/userService';
import { useAuth } from '@/app/common/contexts/AuthContext';
import { useRouter } from 'next/navigation';

export function useUsers() {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { isAdmin } = useAuth();
  const router = useRouter();

  const fetchUsers = async () => {
    if (!isAdmin()) {
      router.push('/dashboard');
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const fetchedUsers = await userService.getUsers();
      setUsers(fetchedUsers);
    } catch (err) {
      setError('Failed to fetch users');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const createUser = async (userData: Omit<User, 'id'>) => {
    if (!isAdmin()) {
      router.push('/dashboard');
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const newUser = await userService.createUser(userData);
      setUsers((prevUsers) => [...prevUsers, newUser]);
      return newUser;
    } catch (err) {
      setError('Failed to create user');
      console.error(err);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return { users, isLoading, error, fetchUsers, createUser };
}
