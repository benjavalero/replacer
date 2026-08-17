package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.util.ReplacerUtils;
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
    private int startName = -1;
    private int endName = -1;
    private String name;
    private String normalizedName;
    private final List<TemplateStructure> nested = new ArrayList<>(4);

    TemplateStructure(String text, int start) {
        this.text = text;
        this.start = start;
    }

    String name() {
        if (name == null) {
            name = startName >= 0 && endName > startName ? text.substring(startName, endName) : "";
        }
        return name;
    }

    String normalizedName() {
        if (normalizedName == null) {
            normalizedName = ReplacerUtils.toLowerCase(name()).replace('_', ' ');
        }
        return normalizedName;
    }

    String group() {
        return text.substring(start, end);
    }

    void setEnd(int end) {
        this.end = end;
    }

    void setNameStart(int startName) {
        this.startName = startName;
    }

    void setNameEnd(int endName) {
        this.endName = endName;
    }

    void addNested(TemplateStructure nested) {
        this.nested.add(nested);
    }

    boolean containsNested(int position) {
        for (TemplateStructure m : this.nested) {
            if (position >= m.start() && position < m.end()) {
                return true;
            }
        }
        return false;
    }
}
