import { useEffect, useState } from 'react'
import 'bootstrap/dist/css/bootstrap.min.css'
import { API_BASE_URL } from './api.js'
import AppRouter from './components/AppRouter.jsx'
import AuthContext from './context/AuthContext.jsx'

const TOKEN_STORAGE_KEY = 'videoGameLetterboxToken'
const SESSION_EXPIRED_MESSAGE = 'Your session expired. Please log in again.'

function getExpirationTime(token) {
  try {
    // This only controls the UI. The backend still verifies the JWT signature.
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const { exp } = JSON.parse(atob(payload))
    return Number.isFinite(exp) ? exp * 1000 : 0
  } catch {
    return 0
  }
}

function App() {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [sessionMessage, setSessionMessage] = useState('')
  const [isRestoringLogin, setIsRestoringLogin] = useState(true)

  useEffect(() => {
    let active = true

    async function restoreLogin() {
      const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY)

      if (!storedToken) {
        setIsRestoringLogin(false)
        return
      }

      if (getExpirationTime(storedToken) <= Date.now()) {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
        setSessionMessage(SESSION_EXPIRED_MESSAGE)
        setIsRestoringLogin(false)
        return
      }

      try {
        const response = await fetch(`${API_BASE_URL}/auth/me`, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        })

        if (response.ok) {
          const restoredUser = await response.json()
          if (!active) return
          setUser(restoredUser)
          setToken(storedToken)
        } else if (active) {
          localStorage.removeItem(TOKEN_STORAGE_KEY)
          if (response.status === 401 || response.status === 403) {
            setSessionMessage(SESSION_EXPIRED_MESSAGE)
          }
        }
      } catch {
        if (active) localStorage.removeItem(TOKEN_STORAGE_KEY)
      } finally {
        if (active) setIsRestoringLogin(false)
      }
    }

    restoreLogin()
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!token) return

    const expirationTime = getExpirationTime(token)

    function checkExpiration() {
      if (Date.now() >= expirationTime) {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
        setToken(null)
        setUser(null)
        setSessionMessage(SESSION_EXPIRED_MESSAGE)
      }
    }

    const timer = setTimeout(checkExpiration, Math.max(0, expirationTime - Date.now()))
    window.addEventListener('focus', checkExpiration)

    return () => {
      clearTimeout(timer)
      window.removeEventListener('focus', checkExpiration)
    }
  }, [token])

  function login(authResponse) {
    setSessionMessage('')
    localStorage.setItem(TOKEN_STORAGE_KEY, authResponse.token)
    setToken(authResponse.token)
    setUser(authResponse.user)
  }

  function logout() {
    setSessionMessage('')
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken(null)
    setUser(null)
  }

  if (isRestoringLogin) {
    return <p className="container py-4">Loading...</p>
  }

  const auth = {
    user,
    token,
    login,
    logout,
    sessionMessage,
  }

  return (
    <AuthContext.Provider value={auth}>
      <AppRouter />
    </AuthContext.Provider>
  )
}

export default App
