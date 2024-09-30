export interface User {
  id: number;
  username: string;
  email: string;
  role: 'admin' | 'user';
}

export interface Task {
  id: string;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo: User;
}
