import { useContext } from 'react'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import AuthContext from '../context/AuthContext.jsx'
import Layout from './Layout.jsx'
import AuthPage from './pages/AuthPage.jsx'
import GameDetailsPage from './pages/GameDetailsPage.jsx'
import HomePage from './pages/HomePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import SearchResultsPage from './pages/SearchResultsPage.jsx'

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
        {
          path: 'search',
          element: <SearchResultsPage />,
        },
        {
          path: 'games/:rawgId',
          element: <GameDetailsPage />,
        },
        {
          path: '*',
          element: <NotFoundPage />,
        },
      ],
    },
  ])

  return <RouterProvider router={router} />
}

export default AppRouter

