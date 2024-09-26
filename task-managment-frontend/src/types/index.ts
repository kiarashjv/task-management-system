export interface User {
  id: number;
  username: string;
  email: string;
}

export interface Task {
  id: string;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  assignedTo: User;
}
