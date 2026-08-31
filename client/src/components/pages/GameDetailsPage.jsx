import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import ErrorList from '../ErrorList.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'
import ReviewCard from '../ReviewCard.jsx'

function GameDetailsPage() {
  const { rawgId } = useParams()
  const [game, setGame] = useState(null)
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    async function loadGame() {
      try {
        const response = await fetch(`${API_BASE_URL}/games/${rawgId}`)
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
  }, [rawgId])

  if (isLoading) {
    return <LoadingIndicator />
  }

  if (!game) {
    return <ErrorList errors={errors.length > 0 ? errors : ['Game was not found.']} />
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
          <p>RAWG rating: {game.rawgRating ?? 'Not rated'}</p>
          <p>Community rating: {game.communityRating == null ? 'Not rated' : `${game.communityRating}/10`}</p>
          <p>
            Recommended: {game.recommendationPercentage == null ? 'No votes yet' : `${game.recommendationPercentage}%`}
          </p>
          <p>{game.description || 'No description is available.'}</p>
        </div>
      </div>

      <h2>Community Reviews</h2>
      {game.reviews.length === 0 ? (
        <p>No reviews yet.</p>
      ) : (
        game.reviews.map((review) => <ReviewCard key={review.reviewId} review={review} />)
      )}
    </section>
  )
}

export default GameDetailsPage
