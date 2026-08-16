package es.bvalero.replacer.finder.immutable.finders;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
class LinkStructureOld {

    private final String text;
    private final int start;
    private int end;
    private int colonPos = -1; // Position of ':' separating the namespace, or -1 if none
    private int pipePos = -1; // Position of the last '|' separating the alias, or -1 if none

    LinkStructureOld(String text, int start) {
        this.text = text;
        this.start = start;
    }

    void setEnd(int end) {
        this.end = end;
    }

    void setColonPos(int colonPos) {
        this.colonPos = colonPos;
    }

    void setPipePos(int pipePos) {
        this.pipePos = pipePos;
    }
}
