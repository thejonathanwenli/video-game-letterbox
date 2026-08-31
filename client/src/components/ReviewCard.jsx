function ReviewCard({ review }) {
  let recommendation = 'No recommendation'
  if (review.recommended === true) {
    recommendation = 'Recommended'
  } else if (review.recommended === false) {
    recommendation = 'Not recommended'
  }

  return (
    <article className="card mb-3">
      <div className="card-body">
        <h3 className="h6">{review.username}</h3>
        <p>{review.body}</p>
        <p className="mb-0 small text-secondary">
          Rating: {review.personalRating == null ? 'Not rated' : `${review.personalRating}/10`} · {recommendation}
        </p>
      </div>
    </article>
  )
}

export default ReviewCard
