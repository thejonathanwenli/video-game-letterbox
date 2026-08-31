package learn.videogameletterbox.external.rawg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawgSearchResponse {

    private List<RawgGame> results = new ArrayList<>();

    public List<RawgGame> getResults() {
        return results;
    }

    public void setResults(List<RawgGame> results) {
        this.results = results;
    }
}
