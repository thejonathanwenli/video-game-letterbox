import { useContext, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import EditProfileForm from '../EditProfileForm.jsx'
import ErrorList from '../ErrorList.jsx'
import LoadingIndicator from '../LoadingIndicator.jsx'
import PublicLibraryCard from '../PublicLibraryCard.jsx'
import ReviewCard from '../ReviewCard.jsx'

function ProfilePage() {
  const { username } = useParams()
  const { user, token } = useContext(AuthContext)
  const [profile, setProfile] = useState(null)
  const [errors, setErrors] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isEditing, setIsEditing] = useState(false)
  const [isFollowingSaving, setIsFollowingSaving] = useState(false)
  const [refreshCount, setRefreshCount] = useState(0)

  useEffect(() => {
    let active = true

    async function loadProfile() {
      setIsLoading(true)
      setErrors([])
      setProfile(null)
      setIsEditing(false)

      try {
        const response = await fetch(`${API_BASE_URL}/users/${encodeURIComponent(username)}`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        })
        if (!response.ok) {
          const messages = response.status === 404 ? ['User was not found.'] : await readErrors(response)
          if (active) setErrors(messages)
          return
        }
        const data = await response.json()
        if (active) setProfile(data)
      } catch {
        if (active) setErrors(['Unable to connect to the game server.'])
      } finally {
        if (active) setIsLoading(false)
      }
    }

    loadProfile()
    return () => { active = false }
  }, [username, token, refreshCount])

  function refreshProfile() {
    setRefreshCount((current) => current + 1)
  }

  async function handleFollow() {
    setErrors([])
    setIsFollowingSaving(true)
    try {
      const response = await fetch(`${API_BASE_URL}/follows/${profile.userId}`, {
        method: profile.following ? 'DELETE' : 'PUT',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }
      refreshProfile()
    } catch {
      setErrors(['Unable to connect to the game server.'])
    } finally {
      setIsFollowingSaving(false)
    }
  }

  if (isLoading) return <LoadingIndicator />
  if (!profile) return <ErrorList errors={errors} />

  const isOwnProfile = user?.userId === profile.userId

  return (
    <section>
      <h1>{profile.username}</h1>
      <ErrorList errors={errors} />
      <p className="text-secondary">Member since {profile.createdAt.slice(0, 10)}</p>
      <p className="text-break" style={{ whiteSpace: 'pre-wrap' }}>{profile.bio || 'No biography yet.'}</p>

      {isOwnProfile ? (
        isEditing ? (
          <EditProfileForm
            key={profile.userId}
            bio={profile.bio}
            token={token}
            onSaved={refreshProfile}
            onCancel={() => setIsEditing(false)}
          />
        ) : (
          <button className="btn btn-outline-primary mb-4" type="button" onClick={() => setIsEditing(true)}>
            Edit Biography
          </button>
        )
      ) : user ? (
        <button className="btn btn-primary mb-4" type="button" onClick={handleFollow} disabled={isFollowingSaving}>
          {isFollowingSaving ? 'Saving...' : profile.following ? 'Unfollow' : 'Follow'}
        </button>
      ) : (
        <p><Link to="/auth">Log in</Link> to follow this user.</p>
      )}

      <h2>Library</h2>
      {profile.library.length === 0 ? <p>No games in this library yet.</p> : (
        profile.library.map((entry) => <PublicLibraryCard key={entry.userGameId} entry={entry} />)
      )}

      <h2 className="mt-4">Reviews</h2>
      {profile.reviews.length === 0 ? <p>No reviews yet.</p> : (
        profile.reviews.map((review) => (
          <ReviewCard
            key={review.reviewId}
            review={review}
            canManage={isOwnProfile}
            token={token}
            onChanged={refreshProfile}
            showGame
          />
        ))
      )}
    </section>
  )
}

export default ProfilePage
