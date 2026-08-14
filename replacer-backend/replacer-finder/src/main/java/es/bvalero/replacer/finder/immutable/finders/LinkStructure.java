package es.bvalero.replacer.finder.immutable.finders;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
class LinkStructure {

    private final String text;
    private final int start;
    private int end;
    private int colonPos = -1; // Position of ':' separating the namespace, or -1 if none
    private int pipePos = -1; // Position of the last '|' separating the alias, or -1 if none
    private final List<LinkStructure> nested = new ArrayList<>(4);

    LinkStructure(String text, int start) {
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

    void addNested(LinkStructure nested) {
        this.nested.add(nested);
    }
}
