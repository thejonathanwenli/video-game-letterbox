import { useContext } from 'react'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import AuthContext from '../context/AuthContext.jsx'
import Layout from './Layout.jsx'
import AuthPage from './pages/AuthPage.jsx'
import HomePage from './pages/HomePage.jsx'

function AppRouter() {
  const { user } = useContext(AuthContext)

  const router = createBrowserRouter([
    {
      path: '/',
      element: <Layout />,
      children: [
        {
          index: true,
          element: <HomePage />,
        },
        {
          path: 'auth',
          element: user ? <Navigate to="/" replace /> : <AuthPage />,
        },
      ],
    },
  ])

  return <RouterProvider router={router} />
}

export default AppRouter

