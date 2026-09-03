import { useState } from 'react'
import { API_BASE_URL, readErrors } from '../api.js'
import ErrorList from './ErrorList.jsx'
import ReviewForm from './ReviewForm.jsx'

function ReviewCard({ review, canManage, token, onChanged }) {
  const [isEditing, setIsEditing] = useState(false)
  const [errors, setErrors] = useState([])

  async function handleDelete() {
    if (!window.confirm('Delete this review?')) {
      return
    }

    setErrors([])
    try {
      const response = await fetch(`${API_BASE_URL}/reviews/${review.reviewId}`, {
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

  if (isEditing) {
    return (
      <ReviewForm
        review={review}
        token={token}
        onSaved={() => {
          setIsEditing(false)
          onChanged()
        }}
        onCancel={() => setIsEditing(false)}
      />
    )
  }

  let recommendation = 'No recommendation'
  if (review.recommended === true) {
    recommendation = 'Recommended'
  } else if (review.recommended === false) {
    recommendation = 'Not recommended'
  }

  return (
    <article className="card mb-3">
      <div className="card-body">
        <h3 className="h6">{review.username}</h3>
        <ErrorList errors={errors} />
        <p>{review.body}</p>
        <p className="mb-0 small text-secondary">
          Rating: {review.personalRating == null ? 'Not rated' : `${review.personalRating}/10`} · {recommendation}
        </p>
        {canManage && (
          <div className="d-flex gap-2 mt-3">
            <button className="btn btn-sm btn-outline-primary" type="button" onClick={() => setIsEditing(true)}>
              Edit
            </button>
            <button className="btn btn-sm btn-outline-danger" type="button" onClick={handleDelete}>
              Delete
            </button>
          </div>
        )}
      </div>
    </article>
  )
}

export default ReviewCard
