import { useState } from 'react'
import { Link } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../api.js'
import ErrorList from './ErrorList.jsx'

function LibraryGameCard({ entry, token, onChanged }) {
  const [status, setStatus] = useState(entry.status)
  const [personalRating, setPersonalRating] = useState(entry.personalRating ?? '')
  const [recommended, setRecommended] = useState(
    entry.recommended == null ? '' : String(entry.recommended),
  )
  const [errors, setErrors] = useState([])
  const [message, setMessage] = useState('')

  function handleStatusChange(event) {
    const nextStatus = event.target.value
    setStatus(nextStatus)
    if (nextStatus === 'WANT_TO_PLAY') {
      setPersonalRating('')
      setRecommended('')
    }
  }

  async function handleSave(event) {
    event.preventDefault()
    setErrors([])
    setMessage('')

    try {
      const response = await fetch(`${API_BASE_URL}/library/${entry.userGameId}`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          status,
          personalRating: personalRating === '' ? null : Number(personalRating),
          recommended: recommended === '' ? null : recommended === 'true',
        }),
      })

      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }

      setMessage('Library entry updated.')
      onChanged()
    } catch {
      setErrors(['Unable to connect to the game server.'])
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Remove ${entry.title} from your library?`)) {
      return
    }

    try {
      const response = await fetch(`${API_BASE_URL}/library/${entry.userGameId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })

      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }

      onChanged()
    } catch {
      setErrors(['Unable to connect to the game server.'])
    }
  }

  const ratingDisabled = status === 'WANT_TO_PLAY'

  return (
    <article className="card mb-3 shadow-sm">
      <div className="row g-0">
        {entry.imageUrl && (
          <div className="col-md-3">
            <img className="img-fluid rounded-start library-cover" src={entry.imageUrl} alt={`Cover art for ${entry.title}`} />
          </div>
        )}
        <div className="col">
          <div className="card-body">
            <h2 className="h4 card-title">
              <Link to={`/games/${entry.igdbId}`}>{entry.title}</Link>
            </h2>
            {entry.genres.length > 0 && <p>{entry.genres.join(' · ')}</p>}
            <ErrorList errors={errors} />
            {message && <div className="alert alert-success">{message}</div>}

            <form className="row g-3" onSubmit={handleSave}>
              <div className="col-md-4">
                <label className="form-label" htmlFor={`status-${entry.userGameId}`}>
                  Status
                </label>
                <select
                  className="form-select"
                  id={`status-${entry.userGameId}`}
                  value={status}
                  onChange={handleStatusChange}
                >
                  <option value="WANT_TO_PLAY">Want to Play</option>
                  <option value="PLAYING">Playing</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="DROPPED">Dropped</option>
                </select>
              </div>
              <div className="col-md-3">
                <label className="form-label" htmlFor={`rating-${entry.userGameId}`}>
                  Rating
                </label>
                <input
                  className="form-control"
                  id={`rating-${entry.userGameId}`}
                  type="number"
                  min="1"
                  max="10"
                  value={personalRating}
                  onChange={(event) => setPersonalRating(event.target.value)}
                  disabled={ratingDisabled}
                />
              </div>
              <div className="col-md-3">
                <label className="form-label" htmlFor={`recommend-${entry.userGameId}`}>
                  Recommend
                </label>
                <select
                  className="form-select"
                  id={`recommend-${entry.userGameId}`}
                  value={recommended}
                  onChange={(event) => setRecommended(event.target.value)}
                  disabled={ratingDisabled}
                >
                  <option value="">No answer</option>
                  <option value="true">Yes</option>
                  <option value="false">No</option>
                </select>
              </div>
              <div className="col-md-2 d-flex gap-2 align-items-end">
                <button className="btn btn-primary" type="submit">
                  Save
                </button>
                <button className="btn btn-outline-danger" type="button" onClick={handleDelete}>
                  Remove
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </article>
  )
}

export default LibraryGameCard
