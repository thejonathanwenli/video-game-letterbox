package learn.videogameletterbox.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "genre",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_genre_igdb_id", columnNames = "igdb_id"),
                @UniqueConstraint(name = "uq_genre_name", columnNames = "name")
        }
)
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Long genreId;

    @Column(name = "igdb_id", nullable = false)
    private Integer igdbId;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    public Genre() {
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public Integer getIgdbId() {
        return igdbId;
    }

    public void setIgdbId(Integer igdbId) {
        this.igdbId = igdbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

