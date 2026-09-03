import { useState } from 'react'
import { API_BASE_URL, readErrors } from '../api.js'
import ErrorList from './ErrorList.jsx'

function ReviewForm({ userGameId, review, token, onSaved, onCancel }) {
  const [body, setBody] = useState(review?.body ?? '')
  const [errors, setErrors] = useState([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setErrors([])
    setIsSubmitting(true)

    const isEditing = review != null
    const url = isEditing
      ? `${API_BASE_URL}/reviews/${review.reviewId}`
      : `${API_BASE_URL}/reviews`
    const payload = isEditing ? { body } : { userGameId, body }

    try {
      const response = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }

      setBody('')
      onSaved()
    } catch {
      setErrors(['Unable to connect to the game server.'])
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="card card-body mb-3" onSubmit={handleSubmit}>
      <h3 className="h5">{review ? 'Edit Your Review' : 'Write a Review'}</h3>
      <ErrorList errors={errors} />
      <div className="mb-3">
        <label className="form-label" htmlFor={`review-body-${review?.reviewId ?? 'new'}`}>
          Review
        </label>
        <textarea
          className="form-control"
          id={`review-body-${review?.reviewId ?? 'new'}`}
          rows="5"
          maxLength="5000"
          value={body}
          onChange={(event) => setBody(event.target.value)}
          required
        />
      </div>
      <div className="d-flex gap-2">
        <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Saving...' : 'Save Review'}
        </button>
        {onCancel && (
          <button className="btn btn-outline-secondary" type="button" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}

export default ReviewForm
