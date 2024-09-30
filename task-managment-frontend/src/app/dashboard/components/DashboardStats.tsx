import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

const DashboardStats = () => {
  // Placeholder stats data
  const stats = [
    { name: 'Total Tasks', stat: '12' },
    { name: 'Completed', stat: '4' },
    { name: 'In Progress', stat: '8' },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Task Statistics</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          {stats.map((item) => (
            <div key={item.name} className="bg-muted p-4 rounded-lg">
              <p className="text-sm font-medium text-muted-foreground">
                {item.name}
              </p>
              <p className="text-2xl font-bold">{item.stat}</p>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default DashboardStats;
