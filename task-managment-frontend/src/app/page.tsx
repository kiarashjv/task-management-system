import { redirect } from 'next/navigation';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Task Management System',
  description: 'Manage your tasks efficiently',
};

export default function Home() {
  redirect('/login');
}
