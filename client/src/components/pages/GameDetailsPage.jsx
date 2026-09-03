import { useContext, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import ErrorList from '../ErrorList.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'
import ReviewCard from '../ReviewCard.jsx'
import ReviewForm from '../ReviewForm.jsx'

function GameDetailsPage() {
  const { igdbId } = useParams()
  const { user, token } = useContext(AuthContext)
  const [game, setGame] = useState(null)
  const [libraryEntry, setLibraryEntry] = useState(null)
  const [status, setStatus] = useState('WANT_TO_PLAY')
  const [message, setMessage] = useState('')
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isLibraryLoading, setIsLibraryLoading] = useState(false)
  const [refreshCount, setRefreshCount] = useState(0)

  useEffect(() => {
    async function loadGame() {
      try {
        const response = await fetch(`${API_BASE_URL}/games/${igdbId}`)
        if (!response.ok) {
          setErrors(await readErrors(response))
          return
        }
        setGame(await response.json())
      } catch {
        setErrors(['Unable to connect to the game server.'])
      } finally {
        setIsLoading(false)
      }
    }

    loadGame()
  }, [igdbId, refreshCount])

  useEffect(() => {
    async function loadLibraryEntry() {
      if (!user || !token) {
        setLibraryEntry(null)
        return
      }

      setIsLibraryLoading(true)
      try {
        const response = await fetch(`${API_BASE_URL}/library`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        if (!response.ok) {
          setErrors(await readErrors(response))
          return
        }

        const entries = await response.json()
        setLibraryEntry(entries.find((entry) => entry.igdbId === Number(igdbId)) ?? null)
      } catch {
        setErrors(['Unable to connect to the game server.'])
      } finally {
        setIsLibraryLoading(false)
      }
    }

    loadLibraryEntry()
  }, [igdbId, token, user])

  async function handleAddToLibrary(event) {
    event.preventDefault()
    setErrors([])
    setMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/library`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          igdbId: Number(igdbId),
          status,
          personalRating: null,
          recommended: null,
        }),
      })

      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }

      setLibraryEntry(await response.json())
      setMessage('Game added to your library.')
    } catch {
      setErrors(['Unable to connect to the game server.'])
    }
  }

  if (isLoading) {
    return <LoadingIndicator />
  }

  if (!game) {
    return <ErrorList errors={errors.length > 0 ? errors : ['Game was not found.']} />
  }

  const ownReview = user
    ? game.reviews.find((review) => review.userId === user.userId)
    : null

  function refreshReviews() {
    setRefreshCount((current) => current + 1)
  }

  return (
    <section>
      <ErrorList errors={errors} />
      <div className="row g-4 mb-5">
        {game.imageUrl && (
          <div className="col-md-5">
            <img className="img-fluid rounded" src={game.imageUrl} alt={`Cover art for ${game.title}`} />
          </div>
        )}
        <div className="col">
          <h1>{game.title}</h1>
          {game.releaseDate && <p className="text-secondary">Released {game.releaseDate}</p>}
          {game.genres.length > 0 && <p>{game.genres.join(' · ')}</p>}
          <p>IGDB rating: {game.igdbRating == null ? 'Not rated' : `${Math.round(game.igdbRating)}/100`}</p>
          <p>Community rating: {game.communityRating == null ? 'Not rated' : `${game.communityRating}/10`}</p>
          <p>
            Recommended: {game.recommendationPercentage == null ? 'No votes yet' : `${game.recommendationPercentage}%`}
          </p>
          <p>{game.description || 'No description is available.'}</p>
          {user && isLibraryLoading ? (
            <p>Loading your library entry...</p>
          ) : user && !libraryEntry ? (
            <form className="row g-2" onSubmit={handleAddToLibrary}>
              <div className="col-sm-7">
                <label className="form-label" htmlFor="new-library-status">
                  Add with status
                </label>
                <select
                  className="form-select"
                  id="new-library-status"
                  value={status}
                  onChange={(event) => setStatus(event.target.value)}
                >
                  <option value="WANT_TO_PLAY">Want to Play</option>
                  <option value="PLAYING">Playing</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="DROPPED">Dropped</option>
                </select>
              </div>
              <div className="col-sm-5 d-grid align-self-end">
                <button className="btn btn-primary" type="submit">
                  Add to My Library
                </button>
              </div>
            </form>
          ) : !user ? (
            <p>
              <Link to="/auth">Log in</Link> to add this game to your library.
            </p>
          ) : null}
          {libraryEntry?.status === 'WANT_TO_PLAY' && (
            <p className="alert alert-info mt-3">
              Change this game out of Want to Play in My Library before writing a review.
            </p>
          )}
          {libraryEntry && libraryEntry.status !== 'WANT_TO_PLAY' && !ownReview && (
            <ReviewForm
              userGameId={libraryEntry.userGameId}
              token={token}
              onSaved={refreshReviews}
            />
          )}
          {message && <div className="alert alert-success mt-3">{message}</div>}
        </div>
      </div>

      <h2>Community Reviews</h2>
      {game.reviews.length === 0 ? (
        <p>No reviews yet.</p>
      ) : (
        game.reviews.map((review) => (
          <ReviewCard
            key={review.reviewId}
            review={review}
            canManage={user?.userId === review.userId}
            token={token}
            onChanged={refreshReviews}
          />
        ))
      )}
    </section>
  )
}

export default GameDetailsPage
