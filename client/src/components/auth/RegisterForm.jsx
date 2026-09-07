import { useState } from 'react'
import { API_BASE_URL, readErrors } from '../../api.js'
import ErrorList from '../ErrorList.jsx'

const EMPTY_FORM = {
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
}

function RegisterForm({ onRegistered }) {
  const [formData, setFormData] = useState(EMPTY_FORM)
  const [errors, setErrors] = useState([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  function handleChange(event) {
    setFormData({
      ...formData,
      [event.target.name]: event.target.value,
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setErrors([])

    if (formData.password !== formData.confirmPassword) {
      setErrors(['Passwords must match.'])
      return
    }

    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.username,
          email: formData.email,
          password: formData.password,
        }),
      })

      if (response.status === 201) {
        setFormData(EMPTY_FORM)
        onRegistered()
      } else {
        setErrors(await readErrors(response))
      }
    } catch {
      setErrors(['Unable to connect to the server.'])
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="col-lg-6">
      <div className="card h-100 shadow-sm">
        <div className="card-body p-4">
          <h2 className="h4">Create an account</h2>
          <ErrorList errors={errors} />

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label" htmlFor="register-username">
                Username
              </label>
              <input
                className="form-control"
                id="register-username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                maxLength="50"
                required
              />
              <div className="form-text">
                Use letters, numbers, underscores, or hyphens.
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="register-email">
                Email
              </label>
              <input
                className="form-control"
                id="register-email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                maxLength="254"
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="register-password">
                Password
              </label>
              <input
                className="form-control"
                id="register-password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                required
              />
              <div className="form-text">
                Use at least 8 characters with a letter, number, and special character.
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="register-confirm-password">
                Confirm password
              </label>
              <input
                className="form-control"
                id="register-confirm-password"
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>

            <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Creating account...' : 'Create account'}
            </button>
          </form>
        </div>
      </div>
    </section>
  )
}

export default RegisterForm
