import { Link } from 'react-router-dom'

function GameCard({ game }) {
  return (
    <article className="card h-100 shadow-sm">
      {game.imageUrl && (
        <img className="card-img-top game-cover" src={game.imageUrl} alt={`Cover art for ${game.title}`} />
      )}
      <div className="card-body d-flex flex-column">
        <h2 className="h5 card-title">{game.title}</h2>
        {game.releaseDate && <p className="text-secondary mb-2">Released {game.releaseDate}</p>}
        {game.genres.length > 0 && <p className="small mb-2">{game.genres.join(' · ')}</p>}
        <p className="mb-1">IGDB rating: {game.igdbRating == null ? 'Not rated' : `${Math.round(game.igdbRating)}/100`}</p>
        <p className="mb-1">
          Community rating: {game.communityRating == null ? 'Not rated' : `${game.communityRating}/10`}
        </p>
        <p>Recommended: {game.recommendationPercentage == null ? 'No votes yet' : `${game.recommendationPercentage}%`}</p>
        <Link className="btn btn-outline-primary mt-auto" to={`/games/${game.igdbId}`}>
          View details
        </Link>
      </div>
    </article>
  )
}

export default GameCard
