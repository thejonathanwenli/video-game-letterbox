package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.GameService;
import learn.videogameletterbox.dtos.GameDetailsResponse;
import learn.videogameletterbox.dtos.GameSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public List<GameSummaryResponse> findRecentlyImported() {
        return gameService.findRecentlyImported();
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String title) {
        if (title == null || title.isBlank()) {
            return new ResponseEntity<>(
                    List.of("Search title is required."),
                    HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(gameService.search(title.trim()));
    }

    @GetMapping("/{rawgId}")
    public ResponseEntity<GameDetailsResponse> findByRawgId(@PathVariable Integer rawgId) {
        return gameService.findByRawgId(rawgId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
