package learn.videogameletterbox.external.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IgdbGame {

    private Integer id;
    private String name;
    private String summary;

    @JsonProperty("first_release_date")
    private Long firstReleaseDate;

    private BigDecimal rating;
    private IgdbCover cover;
    private List<IgdbGenre> genres = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getFirstReleaseDate() {
        return firstReleaseDate;
    }

    public void setFirstReleaseDate(Long firstReleaseDate) {
        this.firstReleaseDate = firstReleaseDate;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public IgdbCover getCover() {
        return cover;
    }

    public void setCover(IgdbCover cover) {
        this.cover = cover;
    }

    public List<IgdbGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<IgdbGenre> genres) {
        this.genres = genres;
    }
}
