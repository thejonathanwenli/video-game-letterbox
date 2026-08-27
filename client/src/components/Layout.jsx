import { Outlet } from 'react-router-dom'
import Navigation from './Navigation.jsx'

function Layout() {
  return (
    <>
      <Navigation />
      <main className="container py-4">
        <Outlet />
      </main>
    </>
  )
}

export default Layout

