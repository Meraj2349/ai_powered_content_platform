import {
  AcademicCapIcon,
  ClockIcon,
  PlusIcon,
  StarIcon,
  UserGroupIcon,
} from "@heroicons/react/24/outline";
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Navigation } from "../components/Navigation";
import { useAuth } from "../contexts/AuthContext";
import { coursePathAPI } from "../services/api";

interface CoursePath {
  id: string;
  subject: string;
  difficulty: string;
  createdAt: string;
  enrollmentCount?: number;
  averageRating?: number;
  progress?: number;
}

interface UserCoursePaths {
  createdCoursePaths: CoursePath[];
  enrolledCoursePaths: CoursePath[];
}

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [coursePaths, setCoursePaths] = useState<UserCoursePaths>({
    createdCoursePaths: [],
    enrolledCoursePaths: [],
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchCoursePaths();
  }, []);

  const fetchCoursePaths = async () => {
    try {
      const response = await coursePathAPI.getMine();
      if (response.data.success) {
        setCoursePaths(response.data.userCoursePaths);
      }
    } catch (err: any) {
      setError("Failed to load course paths");
      console.error("Error fetching course paths:", err);
    } finally {
      setLoading(false);
    }
  };

  const CourseCard: React.FC<{
    course: CoursePath;
    type: "created" | "enrolled";
  }> = ({ course, type }) => (
    <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-2">
          <AcademicCapIcon className="h-6 w-6 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">
            {course.subject}
          </h3>
        </div>
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${
            course.difficulty === "beginner"
              ? "bg-green-100 text-green-800"
              : course.difficulty === "intermediate"
              ? "bg-yellow-100 text-yellow-800"
              : "bg-red-100 text-red-800"
          }`}
        >
          {course.difficulty}
        </span>
      </div>

      <div className="space-y-3">
        {type === "enrolled" && course.progress !== undefined && (
          <div>
            <div className="flex justify-between text-sm text-gray-600 mb-1">
              <span>Progress</span>
              <span>{course.progress}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full"
                style={{ width: `${course.progress}%` }}
              ></div>
            </div>
          </div>
        )}

        <div className="flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center space-x-4">
            {course.averageRating && (
              <div className="flex items-center space-x-1">
                <StarIcon className="h-4 w-4 text-yellow-400" />
                <span>{course.averageRating.toFixed(1)}</span>
              </div>
            )}
            {course.enrollmentCount && (
              <div className="flex items-center space-x-1">
                <UserGroupIcon className="h-4 w-4" />
                <span>{course.enrollmentCount} enrolled</span>
              </div>
            )}
          </div>

          <div className="flex items-center space-x-1">
            <ClockIcon className="h-4 w-4" />
            <span>{new Date(course.createdAt).toLocaleDateString()}</span>
          </div>
        </div>

        <div className="pt-4 border-t border-gray-200">
          <Link
            to={`/courses/${course.id}`}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors text-center block"
          >
            {type === "created" ? "Manage Course" : "Continue Learning"}
          </Link>
        </div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <>
        <Navigation />
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>
      </>
    );
  }

  return (
    <>
      <Navigation />
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Welcome Section */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">
              Welcome back, {user?.firstName}!
            </h1>
            <p className="mt-2 text-gray-600">
              Continue your learning journey or create new course paths.
            </p>
          </div>

          {error && (
            <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}

          {/* Quick Actions */}
          <div className="mb-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Link
              to="/generate"
              className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow border-2 border-dashed border-blue-200 hover:border-blue-400"
            >
              <div className="flex items-center justify-center w-12 h-12 bg-blue-100 rounded-lg mb-4">
                <PlusIcon className="h-6 w-6 text-blue-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Create New Course Path
              </h3>
              <p className="text-gray-600">
                Generate personalized learning paths with AI assistance
              </p>
            </Link>

            <div className="bg-white p-6 rounded-lg shadow-md">
              <div className="flex items-center justify-center w-12 h-12 bg-green-100 rounded-lg mb-4">
                <AcademicCapIcon className="h-6 w-6 text-green-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Total Courses
              </h3>
              <p className="text-3xl font-bold text-green-600">
                {coursePaths.createdCoursePaths.length +
                  coursePaths.enrolledCoursePaths.length}
              </p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-md">
              <div className="flex items-center justify-center w-12 h-12 bg-purple-100 rounded-lg mb-4">
                <UserGroupIcon className="h-6 w-6 text-purple-600" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 mb-2">
                Created Courses
              </h3>
              <p className="text-3xl font-bold text-purple-600">
                {coursePaths.createdCoursePaths.length}
              </p>
            </div>
          </div>

          {/* Enrolled Courses Section */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-gray-900">My Learning</h2>
              <Link
                to="/courses"
                className="text-blue-600 hover:text-blue-700 font-medium"
              >
                View all →
              </Link>
            </div>

            {coursePaths.enrolledCoursePaths.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {coursePaths.enrolledCoursePaths.slice(0, 3).map((course) => (
                  <CourseCard key={course.id} course={course} type="enrolled" />
                ))}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-md p-8 text-center">
                <AcademicCapIcon className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No enrolled courses yet
                </h3>
                <p className="text-gray-600 mb-4">
                  Start your learning journey by creating or enrolling in a
                  course.
                </p>
                <Link
                  to="/generate"
                  className="bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
                >
                  Create Your First Course
                </Link>
              </div>
            )}
          </div>

          {/* Created Courses Section */}
          <div>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-gray-900">
                Courses I Created
              </h2>
            </div>

            {coursePaths.createdCoursePaths.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {coursePaths.createdCoursePaths.slice(0, 3).map((course) => (
                  <CourseCard key={course.id} course={course} type="created" />
                ))}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow-md p-8 text-center">
                <PlusIcon className="h-12 w-12 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  No created courses yet
                </h3>
                <p className="text-gray-600 mb-4">
                  Share your knowledge by creating course paths for others.
                </p>
                <Link
                  to="/generate"
                  className="bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition-colors"
                >
                  Create a Course Path
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};
