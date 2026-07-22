import { Component, type ErrorInfo, type ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('Unhandled UI error:', error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <div className="min-h-[calc(100vh-65px)] flex items-center justify-center px-4">
          <div className="w-full max-w-sm text-center bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
            <h1 className="text-lg font-semibold text-gray-900">Something went wrong</h1>
            <p className="text-sm text-gray-500 mt-2">
              This page hit an unexpected error. Try reloading — if it keeps happening, let staff know.
            </p>
            <button
              onClick={() => (window.location.href = '/')}
              className="mt-6 w-full bg-blue-600 text-white text-sm font-medium rounded-lg px-4 py-2.5 hover:bg-blue-700"
            >
              Reload
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
