import { useContext } from 'react'
import AuthContext from '../../context/AuthContext.jsx'

function HomePage() {
  const { user } = useContext(AuthContext)

  return (
    <section>
      <h1>Discover Games</h1>
      <p>
        {user
          ? `Welcome back, ${user.username}.`
          : 'Browse games, ratings, recommendations, and reviews.'}
      </p>
    </section>
  )
}

export default HomePage

