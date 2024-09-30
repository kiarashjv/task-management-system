// src/app/admin/users/page.tsx
'use client';

import { useAuth } from '@/app/common/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

const AdminUserManagement = () => {
  const { isAdmin } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isAdmin()) {
      router.push('/login');
    }
  }, [isAdmin, router]);

  // Implement user management UI here

  return (
    <div>
      <h1>User Management</h1>
      {/* Add user creation form, user list, etc. */}
    </div>
  );
};

export default AdminUserManagement;
