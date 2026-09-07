import { useState } from 'react'
import { API_BASE_URL, readErrors } from '../api.js'
import ErrorList from './ErrorList.jsx'

function EditProfileForm({ bio, token, onSaved, onCancel }) {
  const [draft, setDraft] = useState(bio ?? '')
  const [errors, setErrors] = useState([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setErrors([])
    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_BASE_URL}/users/me`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ bio: draft }),
      })
      if (!response.ok) {
        setErrors(await readErrors(response))
        return
      }
      onSaved()
    } catch {
      setErrors(['Unable to connect to the game server.'])
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <form className="mb-4" onSubmit={handleSubmit}>
      <ErrorList errors={errors} />
      <label className="form-label" htmlFor="profile-bio">Biography (optional)</label>
      <textarea
        id="profile-bio"
        className="form-control mb-3"
        rows="4"
        maxLength="500"
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
      />
      <div className="d-flex gap-2">
        <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Saving...' : 'Save Biography'}
        </button>
        <button className="btn btn-outline-secondary" type="button" onClick={onCancel} disabled={isSubmitting}>
          Cancel
        </button>
      </div>
    </form>
  )
}

export default EditProfileForm
