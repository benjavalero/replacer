package es.bvalero.replacer.finder.immutable.finders;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
class TemplateStructure {

    private final String text;
    private final int start;
    private int end;
    private String name;
    private String normalizedName;
    private final List<TemplateStructure> nested = new ArrayList<>(4);

    TemplateStructure(String text, int start) {
        this.text = text;
        this.start = start;
    }

    String group() {
        return text.substring(start, end);
    }

    void setEnd(int end) {
        this.end = end;
    }

    void setName(String name) {
        this.name = name;
    }

    void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    void addNested(TemplateStructure nested) {
        this.nested.add(nested);
    }

    boolean containsNested(int position) {
        for (TemplateStructure m : this.nested) {
            if (m != null && position >= m.start() && position < m.end()) {
                return true;
            }
        }
        return false;
    }
}
