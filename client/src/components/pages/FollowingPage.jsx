import { useContext, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import ErrorList from '../ErrorList.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'
import PublicLibraryCard from '../PublicLibraryCard.jsx'
import ReviewCard from '../ReviewCard.jsx'

function FollowingPage() {
  const { token } = useContext(AuthContext)
  const [following, setFollowing] = useState(null)
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let active = true

    async function loadFollowing() {
      setIsLoading(true)
      setErrors([])
      setFollowing(null)
      try {
        const response = await fetch(`${API_BASE_URL}/following`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        if (!response.ok) {
          const messages = await readErrors(response)
          if (active) setErrors(messages)
          return
        }
        const data = await response.json()
        if (active) setFollowing(data)
      } catch {
        if (active) setErrors(['Unable to connect to the game server.'])
      } finally {
        if (active) setIsLoading(false)
      }
    }

    loadFollowing()
    return () => { active = false }
  }, [token])

  return (
    <section>
      <h1>Following</h1>
      <p>Recommendations and recent reviews from the users you follow.</p>
      <ErrorList errors={errors} />
      {isLoading ? <LoadingIndicator /> : following && (
        <>
          <h2>Recommended Games</h2>
          {following.recommendations.length === 0 ? (
            <p>No recommended games yet. Follow users from their profiles to see their recommendations here.</p>
          ) : (
            following.recommendations.map((recommendation) => (
              <div key={recommendation.entry.userGameId}>
                <p className="mb-2">
                  Recommended by <Link to={`/users/${encodeURIComponent(recommendation.username)}`}>
                    {recommendation.username}
                  </Link>
                </p>
                <PublicLibraryCard entry={recommendation.entry} />
              </div>
            ))
          )}

          <h2 className="mt-4">Recent Reviews</h2>
          {following.reviews.length === 0 ? <p>No reviews from followed users yet.</p> : (
            following.reviews.map((review) => <ReviewCard key={review.reviewId} review={review} showGame />)
          )}
        </>
      )}
    </section>
  )
}

export default FollowingPage
