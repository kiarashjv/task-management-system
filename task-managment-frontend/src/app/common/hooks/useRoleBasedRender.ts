// src/hooks/useRoleBasedRender.ts
import { useAuth } from '@/app/common/contexts/AuthContext';

export const useRoleBasedRender = () => {
  const { user } = useAuth();

  const canRender = (allowedRoles: string[]) => {
    return user && allowedRoles.includes(user.role);
  };

  return { canRender };
};
