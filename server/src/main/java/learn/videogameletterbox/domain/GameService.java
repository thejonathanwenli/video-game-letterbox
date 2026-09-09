package learn.videogameletterbox.domain;

import learn.videogameletterbox.data.GameRepository;
import learn.videogameletterbox.data.GenreRepository;
import learn.videogameletterbox.data.ReviewRepository;
import learn.videogameletterbox.data.UserGameRepository;
import learn.videogameletterbox.dtos.GameDetailsResponse;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import learn.videogameletterbox.dtos.ReviewResponse;
import learn.videogameletterbox.external.igdb.IgdbClient;
import learn.videogameletterbox.external.igdb.IgdbApiException;
import learn.videogameletterbox.external.igdb.IgdbGame;
import learn.videogameletterbox.external.igdb.IgdbGenre;
import learn.videogameletterbox.models.Game;
import learn.videogameletterbox.models.Genre;
import learn.videogameletterbox.models.Review;
import learn.videogameletterbox.models.UserGame;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final IgdbClient igdbClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final UserGameRepository userGameRepository;
    private final ReviewRepository reviewRepository;

    public GameService(IgdbClient igdbClient,
                       GameRepository gameRepository,
                       GenreRepository genreRepository,
                       UserGameRepository userGameRepository,
                       ReviewRepository reviewRepository) {
        this.igdbClient = igdbClient;
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
        return igdbClient.search(title).stream()
                .map(this::buildSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<GameDetailsResponse> findByIgdbId(Integer igdbId) {
        Optional<IgdbGame> igdbGame;
        try {
            igdbGame = igdbClient.findById(igdbId);
        } catch (IgdbApiException ex) {
            Game savedGame = gameRepository.findByIgdbId(igdbId).orElseThrow(() -> ex);
            return Optional.of(buildDetails(savedGame));
        }
        if (igdbGame.isEmpty()) {
            return Optional.empty();
        }

        Optional<Game> savedGame = gameRepository.findByIgdbId(igdbId);
        CommunityStats stats = savedGame
                .map(this::getCommunityStats)
                .orElseGet(CommunityStats::new);
        List<ReviewResponse> reviews = savedGame
                .map(this::getReviews)
                .orElseGet(List::of);

        IgdbGame game = igdbGame.get();
        return Optional.of(new GameDetailsResponse(
                game.getId(),
                game.getName(),
                game.getSummary(),
                getImageUrl(game),
                getReleaseDate(game),
                game.getRating(),
                getGenreNames(game.getGenres()),
                stats.communityRating,
                stats.recommendationPercentage,
                reviews));
    }

    @Transactional
    public Game findOrImport(Integer igdbId) {
        Optional<Game> existing = gameRepository.findByIgdbId(igdbId);
        if (existing.isPresent()) {
            return existing.get();
        }

        IgdbGame igdbGame = igdbClient.findById(igdbId)
                .orElseThrow(() -> new IllegalArgumentException("Game was not found."));

        Game game = new Game();
        game.setIgdbId(igdbGame.getId());
        game.setTitle(igdbGame.getName());
        game.setDescription(igdbGame.getSummary());
        game.setImageUrl(getImageUrl(igdbGame));
        game.setReleaseDate(getReleaseDate(igdbGame));
        game.setIgdbRating(igdbGame.getRating());

        List<IgdbGenre> igdbGenres = igdbGame.getGenres() == null
                ? List.of()
                : igdbGame.getGenres();

        for (IgdbGenre igdbGenre : igdbGenres) {
            if (igdbGenre.getId() == null || igdbGenre.getName() == null) {
                continue;
            }

            Genre genre = genreRepository.findByIgdbId(igdbGenre.getId())
                    .or(() -> genreRepository.findByNameIgnoreCase(igdbGenre.getName()))
                    .orElseGet(() -> createGenre(igdbGenre));
            game.getGenres().add(genre);
        }

        return gameRepository.save(game);
    }

    private Genre createGenre(IgdbGenre igdbGenre) {
        Genre genre = new Genre();
        genre.setIgdbId(igdbGenre.getId());
        genre.setName(igdbGenre.getName());
        return genreRepository.save(genre);
    }

    private GameDetailsResponse buildDetails(Game game) {
        GameSummaryResponse summary = buildSummary(game);
        return new GameDetailsResponse(
                summary.getIgdbId(),
                summary.getTitle(),
                game.getDescription(),
                summary.getImageUrl(),
                summary.getReleaseDate(),
                summary.getIgdbRating(),
                summary.getGenres(),
                summary.getCommunityRating(),
                summary.getRecommendationPercentage(),
                getReviews(game));
    }

    private GameSummaryResponse buildSummary(IgdbGame igdbGame) {
        CommunityStats stats = gameRepository.findByIgdbId(igdbGame.getId())
                .map(this::getCommunityStats)
                .orElseGet(CommunityStats::new);

        return new GameSummaryResponse(
                igdbGame.getId(),
                igdbGame.getName(),
                getImageUrl(igdbGame),
                getReleaseDate(igdbGame),
                igdbGame.getRating(),
                getGenreNames(igdbGame.getGenres()),
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
                game.getIgdbId(),
                game.getTitle(),
                game.getImageUrl(),
                game.getReleaseDate(),
                game.getIgdbRating(),
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
        return new ReviewResponse(review);
    }

    private List<String> getGenreNames(List<IgdbGenre> genres) {
        if (genres == null) {
            return List.of();
        }

        ArrayList<String> names = new ArrayList<>();
        for (IgdbGenre genre : genres) {
            if (genre.getName() != null) {
                names.add(genre.getName());
            }
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    private String getImageUrl(IgdbGame game) {
        if (game.getCover() == null || game.getCover().getImageId() == null) {
            return null;
        }
        return "https://images.igdb.com/igdb/image/upload/t_cover_big/"
                + game.getCover().getImageId()
                + ".jpg";
    }

    private LocalDate getReleaseDate(IgdbGame game) {
        if (game.getFirstReleaseDate() == null) {
            return null;
        }
        return Instant.ofEpochSecond(game.getFirstReleaseDate())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
    }

    private static class CommunityStats {
        private Double communityRating;
        private Integer recommendationPercentage;
    }
}
