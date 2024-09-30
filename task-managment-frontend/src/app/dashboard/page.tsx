'use client';

import React from 'react';
import Image from 'next/image';
import TaskList from './components/TaskList';
import QuickAddTask from './components/QuickAddTask';
import DashboardStats from './components/DashboardStats';
import { Button } from '@/components/ui/button';

export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="bg-card shadow">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-3xl font-bold text-foreground">Dashboard</h1>
          <div className="flex items-center space-x-4">
            <Button>New Task</Button>
            <Image
              src="/images/avatar-placeholder.png"
              alt="User Avatar"
              width={40}
              height={40}
              className="rounded-full"
            />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Task List */}
            <div className="md:col-span-2">
              <TaskList />
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              <QuickAddTask />
              <DashboardStats />
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-card shadow mt-8">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
          <p className="text-center text-muted-foreground">
            © 2023 Task Management System. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
