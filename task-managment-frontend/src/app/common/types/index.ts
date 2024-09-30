export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface Task {
  id: string;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo: User;
}
