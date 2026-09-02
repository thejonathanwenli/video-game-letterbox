import { Outlet } from 'react-router-dom'
import Navigation from './Navigation.jsx'

function Layout() {
  return (
    <>
      <Navigation />
      <main className="container py-4">
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

