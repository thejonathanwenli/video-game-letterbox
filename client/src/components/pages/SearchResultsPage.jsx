import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import ErrorList from '../ErrorList.jsx'
import GameList from '../GameList.jsx'
import GameSearchForm from '../GameSearchForm.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'

function SearchResultsPage() {
  const [searchParams] = useSearchParams()
  const title = searchParams.get('title') ?? ''
  const [games, setGames] = useState([])
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(Boolean(title.trim()))

  useEffect(() => {
    async function searchGames() {
      setIsLoading(true)
      setErrors([])

      try {
        const response = await fetch(`${API_BASE_URL}/games/search?title=${encodeURIComponent(title)}`)
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

    if (!title.trim()) {
      return
    }

    searchGames()
  }, [title])

  return (
    <section>
      <h1>Search Games</h1>
      <GameSearchForm key={title} initialTitle={title} />
      <ErrorList errors={errors} />
      {!title.trim() ? (
        <p>Enter a game title to search.</p>
      ) : isLoading ? (
        <LoadingIndicator />
      ) : (
        <GameList games={games} emptyMessage={`No games found for “${title}”.`} />
      )}
    </section>
  )
}

export default SearchResultsPage
