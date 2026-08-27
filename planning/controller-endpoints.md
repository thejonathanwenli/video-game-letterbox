# Controller Endpoints

## Authentication

- `POST` `/api/auth/register`
- `POST` `/api/auth/login`
- `GET` `/api/auth/me`

## Games

- `GET` `/api/games`
- `GET` `/api/games/search?title={title}`
- `GET` `/api/games/{rawgId}`

## Personal Library

- `GET` `/api/library?status={status}&genre={genre}`
- `GET` `/api/library/{userGameId}`
- `POST` `/api/library`
- `PUT` `/api/library/{userGameId}`
- `DELETE` `/api/library/{userGameId}`

## Reviews

- `POST` `/api/reviews`
- `PUT` `/api/reviews/{reviewId}`
- `DELETE` `/api/reviews/{reviewId}`

## Users

- `GET` `/api/users/{username}`
- `PUT` `/api/users/me`

## Following

- `PUT` `/api/follows/{userId}`
- `DELETE` `/api/follows/{userId}`
- `GET` `/api/following`

## Statistics

- `GET` `/api/statistics`
