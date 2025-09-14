import {
  AcademicCapIcon,
  InformationCircleIcon,
  SparklesIcon,
} from "@heroicons/react/24/outline";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Navigation } from "../components/Navigation";
import { coursePathAPI } from "../services/api";

export const GenerateCoursePage: React.FC = () => {
  const [formData, setFormData] = useState({
    subject: "",
    difficulty: "beginner",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const difficultyOptions = [
    { value: "beginner", label: "Beginner", description: "New to the subject" },
    {
      value: "intermediate",
      label: "Intermediate",
      description: "Some experience",
    },
    {
      value: "advanced",
      label: "Advanced",
      description: "Experienced learner",
    },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const response = await coursePathAPI.generate(formData);

      if (response.data.success) {
        const coursePathId = response.data.data.id;
        navigate(`/courses/${coursePathId}`);
      } else {
        setError(response.data.message || "Failed to generate course path");
      }
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "An error occurred while generating the course path"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  return (
    <>
      <Navigation />
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-white rounded-lg shadow-lg p-8">
            {/* Header */}
            <div className="text-center mb-8">
              <div className="flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mx-auto mb-4">
                <SparklesIcon className="h-8 w-8 text-blue-600" />
              </div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">
                Generate AI-Powered Course Path
              </h1>
              <p className="text-gray-600 max-w-2xl mx-auto">
                Create a personalized learning journey tailored to your skill
                level and interests. Our AI will generate a comprehensive course
                structure with topics, resources, and progress tracking.
              </p>
            </div>

            {error && (
              <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                <div className="flex items-center">
                  <InformationCircleIcon className="h-5 w-5 mr-2" />
                  {error}
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-8">
              {/* Subject Input */}
              <div>
                <label
                  htmlFor="subject"
                  className="block text-lg font-semibold text-gray-900 mb-2"
                >
                  What would you like to learn?
                </label>
                <p className="text-gray-600 mb-4">
                  Enter a subject, skill, or topic you want to master. Be as
                  specific as possible for better results.
                </p>
                <input
                  id="subject"
                  name="subject"
                  type="text"
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg text-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  placeholder="e.g., React.js, Data Science, Digital Marketing, Python Programming..."
                  value={formData.subject}
                  onChange={handleChange}
                />
              </div>

              {/* Difficulty Selection */}
              <div>
                <label className="block text-lg font-semibold text-gray-900 mb-2">
                  What's your experience level?
                </label>
                <p className="text-gray-600 mb-4">
                  Choose your current knowledge level to get appropriately
                  structured content.
                </p>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {difficultyOptions.map((option) => (
                    <label
                      key={option.value}
                      className={`relative flex cursor-pointer rounded-lg border p-4 focus:outline-none ${
                        formData.difficulty === option.value
                          ? "border-blue-600 bg-blue-50"
                          : "border-gray-300 bg-white hover:bg-gray-50"
                      }`}
                    >
                      <input
                        type="radio"
                        name="difficulty"
                        value={option.value}
                        checked={formData.difficulty === option.value}
                        onChange={handleChange}
                        className="sr-only"
                      />
                      <div className="flex flex-col">
                        <div className="flex items-center">
                          <div className="text-sm">
                            <div
                              className={`font-semibold ${
                                formData.difficulty === option.value
                                  ? "text-blue-900"
                                  : "text-gray-900"
                              }`}
                            >
                              {option.label}
                            </div>
                            <div
                              className={`${
                                formData.difficulty === option.value
                                  ? "text-blue-700"
                                  : "text-gray-500"
                              }`}
                            >
                              {option.description}
                            </div>
                          </div>
                        </div>
                      </div>
                      {formData.difficulty === option.value && (
                        <div className="absolute -inset-px rounded-lg border-2 border-blue-600 pointer-events-none" />
                      )}
                    </label>
                  ))}
                </div>
              </div>

              {/* Features Info */}
              <div className="bg-blue-50 rounded-lg p-6">
                <h3 className="text-lg font-semibold text-blue-900 mb-3 flex items-center">
                  <AcademicCapIcon className="h-5 w-5 mr-2" />
                  What you'll get:
                </h3>
                <ul className="space-y-2 text-blue-800">
                  <li className="flex items-start">
                    <span className="w-1.5 h-1.5 bg-blue-600 rounded-full mt-2 mr-3 flex-shrink-0"></span>
                    Structured learning path with clear progression
                  </li>
                  <li className="flex items-start">
                    <span className="w-1.5 h-1.5 bg-blue-600 rounded-full mt-2 mr-3 flex-shrink-0"></span>
                    Curated topics and subtopics tailored to your level
                  </li>
                  <li className="flex items-start">
                    <span className="w-1.5 h-1.5 bg-blue-600 rounded-full mt-2 mr-3 flex-shrink-0"></span>
                    Progress tracking and completion status
                  </li>
                  <li className="flex items-start">
                    <span className="w-1.5 h-1.5 bg-blue-600 rounded-full mt-2 mr-3 flex-shrink-0"></span>
                    Recommended resources and learning materials
                  </li>
                </ul>
              </div>

              {/* Submit Button */}
              <div className="flex justify-end">
                <button
                  type="submit"
                  disabled={loading || !formData.subject.trim()}
                  className="flex items-center px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {loading ? (
                    <>
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                      Generating Course Path...
                    </>
                  ) : (
                    <>
                      <SparklesIcon className="h-5 w-5 mr-2" />
                      Generate Course Path
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};
