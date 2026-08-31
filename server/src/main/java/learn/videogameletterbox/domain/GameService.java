package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.GenreRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.GameDetailsResponse;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.external.rawg.RawgClient;
import learn.videogameletterbox.external.rawg.RawgGame;
import learn.videogameletterbox.external.rawg.RawgGenre;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.Genre;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserGame;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final RawgClient rawgClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final UserGameRepository userGameRepository;
    private final ReviewRepository reviewRepository;

    public GameService(RawgClient rawgClient,
                       GameRepository gameRepository,
                       GenreRepository genreRepository,
                       UserGameRepository userGameRepository,
                       ReviewRepository reviewRepository) {
        this.rawgClient = rawgClient;
        this.gameRepository = gameRepository;
        this.genreRepository = genreRepository;
        this.userGameRepository = userGameRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<GameSummaryResponse> findRecentlyImported() {
        return gameRepository.findAllByOrderByImportedAtDesc().stream()
                .map(this::buildSummary)
                .toList();
    }

    public List<GameSummaryResponse> search(String title) {
        return rawgClient.search(title).stream()
                .map(this::buildSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GameDetailsResponse> findByRawgId(Integer rawgId) {
        Optional<RawgGame> rawgGame = rawgClient.findById(rawgId);
        if (rawgGame.isEmpty()) {
            return Optional.empty();
        }

        Optional<Game> savedGame = gameRepository.findByRawgId(rawgId);
        CommunityStats stats = savedGame
                .map(this::getCommunityStats)
                .orElseGet(CommunityStats::new);
        List<ReviewResponse> reviews = savedGame
                .map(this::getReviews)
                .orElseGet(List::of);

        RawgGame game = rawgGame.get();
        return Optional.of(new GameDetailsResponse(
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getBackgroundImage(),
                game.getReleased(),
                game.getRating(),
                getGenreNames(game.getGenres()),
                stats.communityRating,
                stats.recommendationPercentage,
                reviews));
    }

    @Transactional
    public Game findOrImport(Integer rawgId) {
        Optional<Game> existing = gameRepository.findByRawgId(rawgId);
        if (existing.isPresent()) {
            return existing.get();
        }

        RawgGame rawgGame = rawgClient.findById(rawgId)
                .orElseThrow(() -> new IllegalArgumentException("Game was not found."));

        Game game = new Game();
        game.setRawgId(rawgGame.getId());
        game.setTitle(rawgGame.getName());
        game.setDescription(rawgGame.getDescription());
        game.setImageUrl(rawgGame.getBackgroundImage());
        game.setReleaseDate(rawgGame.getReleased());
        game.setRawgRating(rawgGame.getRating());

        List<RawgGenre> rawgGenres = rawgGame.getGenres() == null
                ? List.of()
                : rawgGame.getGenres();

        for (RawgGenre rawgGenre : rawgGenres) {
            if (rawgGenre.getId() == null || rawgGenre.getName() == null) {
                continue;
            }

            Genre genre = genreRepository.findByRawgId(rawgGenre.getId())
                    .or(() -> genreRepository.findByNameIgnoreCase(rawgGenre.getName()))
                    .orElseGet(() -> createGenre(rawgGenre));
            game.getGenres().add(genre);
        }

        return gameRepository.save(game);
    }

    private Genre createGenre(RawgGenre rawgGenre) {
        Genre genre = new Genre();
        genre.setRawgId(rawgGenre.getId());
        genre.setName(rawgGenre.getName());
        return genreRepository.save(genre);
    }

    private GameSummaryResponse buildSummary(RawgGame rawgGame) {
        CommunityStats stats = gameRepository.findByRawgId(rawgGame.getId())
                .map(this::getCommunityStats)
                .orElseGet(CommunityStats::new);

        return new GameSummaryResponse(
                rawgGame.getId(),
                rawgGame.getName(),
                rawgGame.getBackgroundImage(),
                rawgGame.getReleased(),
                rawgGame.getRating(),
                getGenreNames(rawgGame.getGenres()),
                stats.communityRating,
                stats.recommendationPercentage);
    }

    private GameSummaryResponse buildSummary(Game game) {
        CommunityStats stats = getCommunityStats(game);
        List<String> genres = game.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();

        return new GameSummaryResponse(
                game.getRawgId(),
                game.getTitle(),
                game.getImageUrl(),
                game.getReleaseDate(),
                game.getRawgRating(),
                genres,
                stats.communityRating,
                stats.recommendationPercentage);
    }

    private CommunityStats getCommunityStats(Game game) {
        List<UserGame> entries = userGameRepository.findByGame_GameId(game.getGameId());

        double averageRating = entries.stream()
                .filter(entry -> entry.getPersonalRating() != null)
                .mapToInt(entry -> entry.getPersonalRating())
                .average()
                .orElse(Double.NaN);

        long answeredRecommendations = entries.stream()
                .filter(entry -> entry.getRecommended() != null)
                .count();
        long recommendations = entries.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getRecommended()))
                .count();

        CommunityStats stats = new CommunityStats();
        if (!Double.isNaN(averageRating)) {
            stats.communityRating = BigDecimal.valueOf(averageRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        if (answeredRecommendations > 0) {
            stats.recommendationPercentage = (int) Math.round(
                    recommendations * 100.0 / answeredRecommendations);
        }
        return stats;
    }

    private List<ReviewResponse> getReviews(Game game) {
        return reviewRepository.findByUserGame_Game_GameIdOrderByCreatedAtDesc(game.getGameId()).stream()
                .map(this::buildReview)
                .toList();
    }

    private ReviewResponse buildReview(Review review) {
        UserGame userGame = review.getUserGame();
        return new ReviewResponse(
                review.getReviewId(),
                userGame.getUser().getDisplayUsername(),
                review.getBody(),
                userGame.getPersonalRating(),
                userGame.getRecommended(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private List<String> getGenreNames(List<RawgGenre> genres) {
        if (genres == null) {
            return List.of();
        }

        ArrayList<String> names = new ArrayList<>();
        for (RawgGenre genre : genres) {
            if (genre.getName() != null) {
                names.add(genre.getName());
            }
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    private static class CommunityStats {
        private Double communityRating;
        private Integer recommendationPercentage;
    }
}
