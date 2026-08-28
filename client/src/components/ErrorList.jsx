function ErrorList({ errors }) {
  if (errors.length === 0) {
    return null
  }

  return (
    <div className="alert alert-danger" role="alert">
      <ul className="mb-0">
        {errors.map((error) => (
          <li key={error}>{error}</li>
        ))}
      </ul>
    </div>
  )
}

export default ErrorList
