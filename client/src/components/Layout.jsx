import { useContext } from 'react'
import { Link, Outlet } from 'react-router-dom'
import AuthContext from '../context/AuthContext.jsx'
import Navigation from './Navigation.jsx'

function Layout() {
  const { sessionMessage } = useContext(AuthContext)

  return (
    <>
      <Navigation />
      <main className="container py-4">
        {sessionMessage && (
          <div className="alert alert-warning" role="alert">
            {sessionMessage} <Link className="alert-link" to="/auth">Log in</Link>
          </div>
        )}
        <Outlet />
      </main>
      <footer className="container border-top py-3 text-secondary">
        Game data provided by{' '}
        <a href="https://www.igdb.com/" target="_blank" rel="noreferrer">
          IGDB
        </a>
        .
      </footer>
    </>
  )
}

export default Layout

