'use client';

import Link from 'next/link';
import LoginForm from './components/LoginForm';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Facebook, Github } from 'lucide-react';

export default function LoginPage() {
  return (
    <div className="min-h-screen flex flex-col sm:flex-row">
      {/* Login Section */}
      <div className="flex-1 flex items-center justify-center bg-gray-100 dark:bg-gray-800 p-8">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle className="text-3xl font-bold text-center">
              Welcome Back!
            </CardTitle>
            <CardDescription className="text-center">
              Sign in to your account
            </CardDescription>
          </CardHeader>
          <CardContent>
            <LoginForm />
            <div className="mt-6">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-background px-2 text-muted-foreground">
                    Or continue with
                  </span>
                </div>
              </div>
              <div className="mt-6 flex gap-4">
                <Button variant="outline" className="w-full">
                  <Github className="mr-2 h-4 w-4" />
                  Github
                </Button>
                <Button variant="outline" className="w-full">
                  <Facebook className="mr-2 h-4 w-4" />
                  Facebook
                </Button>
              </div>
            </div>
            <p className="mt-6 text-center text-sm text-gray-600 dark:text-gray-300">
              Don&apos;t have an account?{' '}
              <Link
                href="/signup"
                className="font-medium text-primary hover:underline"
              >
                Sign Up
              </Link>
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Information Section */}
      <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-center bg-white dark:bg-gray-900 p-8">
        <div className="max-w-lg">
          <h2 className="text-2xl font-bold mb-4 text-gray-800 dark:text-gray-100">
            Manage Your Tasks Efficiently
          </h2>
          <p className="text-gray-600 dark:text-gray-300">
            Our Task Management System helps you organize, track, and
            collaborate on your tasks seamlessly. Whether you&apos;re working
            solo or as part of a team, our platform provides the tools you need
            to stay productive and achieve your goals.
          </p>
        </div>
      </div>
    </div>
  );
}
