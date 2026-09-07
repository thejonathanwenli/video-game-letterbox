import { Link } from 'react-router-dom'

const STATUS_LABELS = {
  WANT_TO_PLAY: 'Want to Play',
  PLAYING: 'Playing',
  COMPLETED: 'Completed',
  DROPPED: 'Dropped',
}

function PublicLibraryCard({ entry }) {
  let recommendation = 'No recommendation'
  if (entry.recommended === true) {
    recommendation = 'Recommended'
  } else if (entry.recommended === false) {
    recommendation = 'Not recommended'
  }

  return (
    <article className="card mb-3">
      <div className="card-body">
        <div className="row g-3">
          {entry.imageUrl && (
            <div className="col-3 col-md-2">
              <img className="img-fluid rounded" src={entry.imageUrl} alt={`Cover art for ${entry.title}`} />
            </div>
          )}
          <div className="col">
            <h3 className="h5"><Link to={`/games/${entry.igdbId}`}>{entry.title}</Link></h3>
            {entry.genres.length > 0 && <p className="small text-secondary">{entry.genres.join(' · ')}</p>}
            <p>{STATUS_LABELS[entry.status]}</p>
            <p className="mb-0">
              Rating: {entry.personalRating == null ? 'Not rated' : `${entry.personalRating}/10`} · {recommendation}
            </p>
          </div>
        </div>
      </div>
    </article>
  )
}

export default PublicLibraryCard
