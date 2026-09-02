import { useState } from 'react'

function LibraryFilters({ onApply }) {
  const [status, setStatus] = useState('')
  const [genre, setGenre] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    onApply({ status, genre: genre.trim() })
  }

  return (
    <form className="row g-2 mb-4" onSubmit={handleSubmit}>
      <div className="col-md-5">
        <label className="form-label" htmlFor="library-status-filter">
          Status
        </label>
        <select
          className="form-select"
          id="library-status-filter"
          value={status}
          onChange={(event) => setStatus(event.target.value)}
        >
          <option value="">All statuses</option>
          <option value="WANT_TO_PLAY">Want to Play</option>
          <option value="PLAYING">Playing</option>
          <option value="COMPLETED">Completed</option>
          <option value="DROPPED">Dropped</option>
        </select>
      </div>
      <div className="col-md-5">
        <label className="form-label" htmlFor="library-genre-filter">
          Genre
        </label>
        <input
          className="form-control"
          id="library-genre-filter"
          value={genre}
          onChange={(event) => setGenre(event.target.value)}
          placeholder="Example: Adventure"
        />
      </div>
      <div className="col-md-2 d-grid align-self-end">
        <button className="btn btn-outline-primary" type="submit">
          Apply
        </button>
      </div>
    </form>
  )
}

export default LibraryFilters
