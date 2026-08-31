import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

function GameSearchForm({ initialTitle = '' }) {
  const [title, setTitle] = useState(initialTitle)
  const navigate = useNavigate()

  function handleSubmit(event) {
    event.preventDefault()

    const trimmedTitle = title.trim()
    if (trimmedTitle) {
      navigate(`/search?title=${encodeURIComponent(trimmedTitle)}`)
    }
  }

  return (
    <form className="row g-2 mb-4" onSubmit={handleSubmit}>
      <div className="col-sm-9">
        <label className="visually-hidden" htmlFor="game-title">
          Game title
        </label>
        <input
          className="form-control"
          id="game-title"
          type="search"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          placeholder="Search for a game"
          required
        />
      </div>
      <div className="col-sm-3 d-grid">
        <button className="btn btn-primary" type="submit">
          Search
        </button>
      </div>
    </form>
  )
}

export default GameSearchForm
