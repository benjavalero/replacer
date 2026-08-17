package es.bvalero.replacer.finder.immutable.finders;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
class LinkStructure {

    private final String text;
    private final int start;
    private int end;
    private int posColon = -1; // Position of ':' separating the namespace, or -1 if none
    private int posPipe = -1; // Position of the last '|' separating the alias, or -1 if none

    LinkStructure(String text, int start) {
        this.text = text;
        this.start = start;
    }

    void setEnd(int end) {
        this.end = end;
    }

    void setColonPos(int posColon) {
        this.posColon = posColon;
    }

    void setPipePos(int posPipe) {
        this.posPipe = posPipe;
    }
}
