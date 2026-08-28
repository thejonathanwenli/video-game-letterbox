import { useContext, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { API_BASE_URL, readErrors } from '../../api.js'
import AuthContext from '../../context/AuthContext.jsx'
import ErrorList from '../ErrorList.jsx'

function LoginForm() {
  const [credentials, setCredentials] = useState({
    email: '',
    password: '',
  })
  const [errors, setErrors] = useState([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const auth = useContext(AuthContext)
  const navigate = useNavigate()

  function handleChange(event) {
    setCredentials({
      ...credentials,
      [event.target.name]: event.target.value,
    })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setErrors([])
    setIsSubmitting(true)

    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      })

      if (response.ok) {
        const payload = await response.json()
        auth.login(payload)
        navigate('/')
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
          <h2 className="h4">Log in</h2>
          <ErrorList errors={errors} />

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label" htmlFor="login-email">
                Email
              </label>
              <input
                className="form-control"
                id="login-email"
                name="email"
                type="email"
                value={credentials.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="login-password">
                Password
              </label>
              <input
                className="form-control"
                id="login-password"
                name="password"
                type="password"
                value={credentials.password}
                onChange={handleChange}
                required
              />
            </div>

            <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
              {isSubmitting ? 'Logging in...' : 'Log in'}
            </button>
          </form>
        </div>
      </div>
    </section>
  )
}

export default LoginForm
