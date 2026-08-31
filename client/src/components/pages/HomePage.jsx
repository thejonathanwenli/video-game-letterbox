import { useContext, useEffect, useState } from 'react'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import ErrorList from '../ErrorList.jsx'
import GameList from '../GameList.jsx'
import GameSearchForm from '../GameSearchForm.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'

function HomePage() {
  const { user } = useContext(AuthContext)
  const [games, setGames] = useState([])
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function loadGames() {
      try {
        const response = await fetch(`${API_BASE_URL}/games`)
        if (!response.ok) {
          setErrors(await readErrors(response))
          return
        }
        setGames(await response.json())
      } catch {
        setErrors(['Unable to connect to the game server.'])
      } finally {
        setIsLoading(false)
      }
    }

    loadGames()
  }, [])

  return (
    <section>
      <h1>Discover Games</h1>
      <p>
        {user
          ? `Welcome back, ${user.username}.`
          : 'Browse games, ratings, recommendations, and reviews.'}
      </p>
      <GameSearchForm />
      <ErrorList errors={errors} />
      <h2 className="h3">Recently Added</h2>
      {isLoading ? (
        <LoadingIndicator />
      ) : (
        <GameList games={games} emptyMessage="No games have been added yet. Search for a game to get started." />
      )}
    </section>
  )
}

export default HomePage

