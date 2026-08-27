import { Link } from 'react-router-dom'

function Navigation() {
  return (
    <nav className="navbar navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand" to="/">
          Video Game Letterbox
        </Link>
      </div>
    </nav>
  )
}

export default Navigation

