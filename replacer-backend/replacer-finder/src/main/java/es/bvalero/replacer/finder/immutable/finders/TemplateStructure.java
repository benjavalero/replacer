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
    private int nameStart = -1;
    private int nameEnd = -1;
    private String name;
    private String normalizedName;
    private final List<TemplateStructure> nested = new ArrayList<>(4);

    TemplateStructure(String text, int start) {
        this.text = text;
        this.start = start;
    }

    String name() {
        if (name == null) {
            name = nameStart >= 0 && nameEnd > nameStart ? text.substring(nameStart, nameEnd) : "";
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

    void setNameStart(int nameStart) {
        this.nameStart = nameStart;
    }

    void setNameEnd(int nameEnd) {
        this.nameEnd = nameEnd;
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
