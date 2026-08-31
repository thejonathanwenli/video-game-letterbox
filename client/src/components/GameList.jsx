import GameCard from './GameCard.jsx'

function GameList({ games, emptyMessage }) {
  if (games.length === 0) {
    return <p>{emptyMessage}</p>
  }

  return (
    <div className="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4">
      {games.map((game) => (
        <div className="col" key={game.rawgId}>
          <GameCard game={game} />
        </div>
      ))}
    </div>
  )
}

export default GameList
