import React from 'react';

const TaskList = () => {
  // Placeholder task data
  const tasks = [
    { id: 1, title: 'Complete project proposal', status: 'In Progress' },
    { id: 2, title: 'Review team submissions', status: 'Pending' },
    { id: 3, title: 'Prepare for client meeting', status: 'Completed' },
  ];

  return (
    <div className="bg-white dark:bg-gray-800 shadow overflow-hidden sm:rounded-md">
      <ul className="divide-y divide-gray-200 dark:divide-gray-700">
        {tasks.map((task) => (
          <li
            key={task.id}
            className="px-6 py-4 hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            <div className="flex items-center justify-between">
              <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                {task.title}
              </p>
              <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                {task.status}
              </span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TaskList;
