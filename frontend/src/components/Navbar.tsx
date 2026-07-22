import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = () => {
    signOut();
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-4">
      <div className="max-w-5xl mx-auto flex items-center justify-between">
        <Link to="/" className="flex items-center gap-3">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
            <span className="text-white text-sm font-bold">H</span>
          </div>
          <div>
            <div className="text-sm font-semibold text-gray-900 leading-tight">Hillingdon Hospital</div>
            <div className="text-xs text-gray-500">Patient Parking</div>
          </div>
        </Link>

        <div className="flex items-center gap-4">
          {user ? (
            <>
              {user.role === 'PATIENT' && (
                <>
                  <Link to="/book" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                    Book Parking
                  </Link>
                  <Link to="/my-bookings" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                    My Bookings
                  </Link>
                </>
              )}
              {(user.role === 'STAFF' || user.role === 'ADMIN') && (
                <>
                  <Link to="/dashboard" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                    Dashboard
                  </Link>
                  <Link to="/bookings" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                    Manage Bookings
                  </Link>
                  <Link to="/badges" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                    Badge Queue
                  </Link>
                </>
              )}
              {user.role === 'ADMIN' && (
                <Link to="/admin" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                  Admin
                </Link>
              )}
              <div className="flex items-center gap-3 ml-2 pl-4 border-l border-gray-200">
                <span className="text-sm text-gray-700">
                  {user.firstName} {user.lastName}
                </span>
                <button
                  onClick={handleSignOut}
                  className="text-sm text-gray-500 hover:text-red-600"
                >
                  Sign out
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="text-sm text-gray-600 hover:text-blue-600 font-medium">
                Sign in
              </Link>
              <Link
                to="/register"
                className="text-sm bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 font-medium"
              >
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
