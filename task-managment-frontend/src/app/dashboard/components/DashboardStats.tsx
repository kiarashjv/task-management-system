import React from 'react';

const DashboardStats = () => {
  // Placeholder stats data
  const stats = [
    { name: 'Total Tasks', stat: '12' },
    { name: 'Completed', stat: '4' },
    { name: 'In Progress', stat: '8' },
  ];

  return (
    <div className="bg-white dark:bg-gray-800 shadow sm:rounded-lg">
      <div className="px-4 py-5 sm:p-6">
        <h3 className="text-lg leading-6 font-medium text-gray-900 dark:text-white">
          Task Statistics
        </h3>
        <dl className="mt-5 grid grid-cols-1 gap-5 sm:grid-cols-3">
          {stats.map((item) => (
            <div
              key={item.name}
              className="px-4 py-5 bg-gray-50 dark:bg-gray-700 shadow rounded-lg overflow-hidden sm:p-6"
            >
              <dt className="text-sm font-medium text-gray-500 dark:text-gray-400 truncate">
                {item.name}
              </dt>
              <dd className="mt-1 text-3xl font-semibold text-gray-900 dark:text-white">
                {item.stat}
              </dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
};

export default DashboardStats;
