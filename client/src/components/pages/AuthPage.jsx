import { useState } from 'react'
import LoginForm from '../auth/LoginForm.jsx'
import RegisterForm from '../auth/RegisterForm.jsx'

function AuthPage() {
  const [message, setMessage] = useState('')

  return (
    <section>
      <h1>Join or return</h1>
      <p>Log in to manage your library, or create a new account.</p>

      {message && (
        <div className="alert alert-success" role="status">
          {message}
        </div>
      )}

      <div className="row g-4">
        <LoginForm />
        <RegisterForm onRegistered={() => setMessage('Account created. You can now log in.')} />
      </div>
    </section>
  )
}

export default AuthPage
