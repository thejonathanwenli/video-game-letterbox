# Video Game Letterbox

Video Game Letterbox is a full-stack app for discovering games, tracking a personal backlog, and sharing ratings and reviews. Follow other players to see their recommendations and recent reviews.

The frontend uses React and Bootstrap. The Java backend uses Spring Boot, Spring Security, and JPA/Hibernate with MySQL. Game data comes from IGDB. When someone adds a game to their library, the app saves its details in MySQL so that its game page can still load if the IGDB API is unavailable. Cover images are served by IGDB.

## Run locally with Docker Compose

1. Start Docker. Copy `.env.example` to `.env` and fill in the local database password, a random JWT secret of at least 32 characters, and your Twitch application credentials for IGDB. Keep `DB_USERNAME` set to `root` for this local setup.
2. Run these commands from the repository root:

   ```sh
   docker volume create video-game-letterbox-mysql-data
   docker compose up --build -d
   ```

3. Open [http://localhost:3000](http://localhost:3000). Run `docker compose down` to stop the app. The named volume keeps your local database between runs.

The [server](server) includes the database schema, API request examples, and tests. The [planning folder](planning) contains the schema diagram and endpoint notes.

## AWS deployment

The app was deployed to AWS Lightsail using [`compose.aws.yaml`](compose.aws.yaml). This configuration runs the same app services with Caddy handling HTTPS. It uses the [AWS environment template](deploy/aws/env.example) and a domain pointing to the server.
