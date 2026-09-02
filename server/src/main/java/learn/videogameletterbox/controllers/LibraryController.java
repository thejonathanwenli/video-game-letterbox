package learn.videogameletterbox.controllers;

import learn.videogameletterbox.domain.LibraryService;
import learn.videogameletterbox.domain.Result;
import learn.videogameletterbox.domain.ResultType;
import learn.videogameletterbox.dtos.AddLibraryEntryRequest;
import learn.videogameletterbox.dtos.LibraryEntryResponse;
import learn.videogameletterbox.dtos.UpdateLibraryEntryRequest;
import learn.videogameletterbox.models.AppUser;
import learn.videogameletterbox.models.GameStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public List<LibraryEntryResponse> findAll(@AuthenticationPrincipal AppUser appUser,
                                              @RequestParam(required = false) GameStatus status,
                                              @RequestParam(required = false) String genre) {
        return libraryService.findAll(appUser.getUserId(), status, genre);
    }

    @GetMapping("/{userGameId}")
    public ResponseEntity<LibraryEntryResponse> findById(@AuthenticationPrincipal AppUser appUser,
                                                         @PathVariable Long userGameId) {
        return libraryService.findById(appUser.getUserId(), userGameId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@AuthenticationPrincipal AppUser appUser,
                                 @RequestBody AddLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = libraryService.add(appUser.getUserId(), request);
        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), getErrorStatus(result));
        }
        return new ResponseEntity<>(result.getPayload(), HttpStatus.CREATED);
    }

    @PutMapping("/{userGameId}")
    public ResponseEntity<?> update(@AuthenticationPrincipal AppUser appUser,
                                    @PathVariable Long userGameId,
                                    @RequestBody UpdateLibraryEntryRequest request) {
        Result<LibraryEntryResponse> result = libraryService.update(
                appUser.getUserId(), userGameId, request);
        if (!result.isSuccess()) {
            return new ResponseEntity<>(result.getMessages(), getErrorStatus(result));
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userGameId}")
    public ResponseEntity<Void> deleteById(@AuthenticationPrincipal AppUser appUser,
                                           @PathVariable Long userGameId) {
        if (libraryService.deleteById(appUser.getUserId(), userGameId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private HttpStatus getErrorStatus(Result<?> result) {
        return result.getType() == ResultType.NOT_FOUND
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
    }
}
