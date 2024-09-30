'use client';

import React from 'react';
import Image from 'next/image';

// Placeholder components - you'll want to create these separately
import TaskList from './components/TaskList';
import QuickAddTask from './components/QuickAddTask';
import DashboardStats from './components/DashboardStats';

export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-gray-100 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
            Dashboard
          </h1>
          <div className="flex items-center">
            <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
              New Task
            </button>
            <div className="ml-4">
              <Image
                src="/images/avatar-placeholder.png"
                alt="User Avatar"
                width={40}
                height={40}
                className="rounded-full"
              />
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Task List */}
            <div className="md:col-span-2">
              <h2 className="text-2xl font-semibold mb-4 text-gray-800 dark:text-white">
                Your Tasks
              </h2>
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
      <footer className="bg-white dark:bg-gray-800 shadow mt-8">
        <div className="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
          <p className="text-center text-gray-500 dark:text-gray-400">
            © 2023 Task Management System. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
