import { useEffect, useState } from 'react'
import 'bootstrap/dist/css/bootstrap.min.css'
import { API_BASE_URL } from './api.js'
import AppRouter from './components/AppRouter.jsx'
import AuthContext from './context/AuthContext.jsx'

const TOKEN_STORAGE_KEY = 'videoGameLetterboxToken'

function App() {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [isRestoringLogin, setIsRestoringLogin] = useState(true)

  useEffect(() => {
    async function restoreLogin() {
      const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY)

      if (!storedToken) {
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
          setUser(await response.json())
          setToken(storedToken)
        } else {
          localStorage.removeItem(TOKEN_STORAGE_KEY)
        }
      } catch {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
      } finally {
        setIsRestoringLogin(false)
      }
    }

    restoreLogin()
  }, [])

  function login(authResponse) {
    localStorage.setItem(TOKEN_STORAGE_KEY, authResponse.token)
    setToken(authResponse.token)
    setUser(authResponse.user)
  }

  function logout() {
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
  }

  return (
    <AuthContext.Provider value={auth}>
      <AppRouter />
    </AuthContext.Provider>
  )
}

export default App
