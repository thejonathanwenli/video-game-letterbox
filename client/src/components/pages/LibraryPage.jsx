import { useContext, useEffect, useState } from 'react'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import ErrorList from '../ErrorList.jsx'
import LibraryFilters from '../LibraryFilters.jsx'
import LibraryGameCard from '../LibraryGameCard.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'

function LibraryPage() {
  const { token } = useContext(AuthContext)
  const [entries, setEntries] = useState([])
  const [filters, setFilters] = useState({ status: '', genre: '' })
  const [refreshCount, setRefreshCount] = useState(0)
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function loadLibrary() {
      setIsLoading(true)
      setErrors([])

      const searchParams = new URLSearchParams()
      if (filters.status) {
        searchParams.set('status', filters.status)
      }
      if (filters.genre) {
        searchParams.set('genre', filters.genre)
      }

      try {
        const query = searchParams.toString()
        const response = await fetch(`${API_BASE_URL}/library${query ? `?${query}` : ''}`, {
          headers: { Authorization: `Bearer ${token}` },
        })

        if (!response.ok) {
          setErrors(await readErrors(response))
          return
        }
        setEntries(await response.json())
      } catch {
        setErrors(['Unable to connect to the game server.'])
      } finally {
        setIsLoading(false)
      }
    }

    loadLibrary()
  }, [filters, refreshCount, token])

  return (
    <section>
      <h1>My Library</h1>
      <LibraryFilters onApply={setFilters} />
      <ErrorList errors={errors} />
      {isLoading ? (
        <LoadingIndicator />
      ) : entries.length === 0 ? (
        <p>No games match your library filters.</p>
      ) : (
        entries.map((entry) => (
          <LibraryGameCard
            key={entry.userGameId}
            entry={entry}
            token={token}
            onChanged={() => setRefreshCount((current) => current + 1)}
          />
        ))
      )}
    </section>
  )
}

export default LibraryPage
