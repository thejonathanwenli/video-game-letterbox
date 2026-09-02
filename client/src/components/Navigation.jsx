import { useContext } from 'react'
import { Link, NavLink } from 'react-router-dom'
import AuthContext from '../context/AuthContext.jsx'

function Navigation() {
  const { user, logout } = useContext(AuthContext)

  return (
    <nav className="navbar navbar-expand navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand" to="/">
          Video Game Letterbox
        </Link>

        <ul className="navbar-nav ms-auto align-items-center">
          <li className="nav-item">
            <NavLink className="nav-link" to="/">
              Discover
            </NavLink>
          </li>

          {user ? (
            <>
              <li className="nav-item">
                <NavLink className="nav-link" to="/library">
                  My Library
                </NavLink>
              </li>
              <li className="nav-item">
                <span className="navbar-text me-3">{user.username}</span>
              </li>
              <li className="nav-item">
                <button className="btn btn-outline-light btn-sm" type="button" onClick={logout}>
                  Log out
                </button>
              </li>
            </>
          ) : (
            <li className="nav-item">
              <NavLink className="nav-link" to="/auth">
                Log in or sign up
              </NavLink>
            </li>
          )}
        </ul>
      </div>
    </nav>
  )
}

export default Navigation

