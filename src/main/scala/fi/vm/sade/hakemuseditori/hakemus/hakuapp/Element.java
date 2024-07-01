package fi.vm.sade.hakemuseditori.hakemus.hakuapp;

import com.google.common.base.Objects;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Element implements Serializable {
    private static final long serialVersionUID = 3485937447100055723L;

    protected final String type = this.getClass().getSimpleName();

    protected final String id;

    protected final List<Element> children;
    protected final Map<String, String> attributes;
    protected I18nText help;
    protected Element popup;
    private boolean inline;


    protected Element(final String id) {
        this.id = id;
        this.help = null;
        this.children = new ArrayList<Element>();
        this.attributes = new HashMap<String, String>();
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public I18nText getHelp() {
        return help;
    }

    public void setHelp(final I18nText help) {
        this.help = help;
    }

    public Element getPopup() {
        return this.popup;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public Element addChild(Element... children) {
        Collections.addAll(this.children, children);
        return this;
    }

    public void addAttribute(final String key, final String value) {
        checkNotNull(key, "Attribute's key cannot be null");
        checkNotNull(value, "Attribute's value cannot be null");
        if (!attributes.containsKey(key)) {
            this.attributes.put(key, value);
        } else
            throw new UnsupportedOperationException("Attribute \"" + key + "\" already set");
    }

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    /*
     * Required for fi.vm.sade.haku.oppija.lomake.domain.rule.RelatedQuestionRule to work
     */
    public List<Element> getChildren(Map<String, String> values) {
        return getChildren();
    }

    public List<Element> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @JsonIgnore
    public List<Element> getAllChildren() {
        List<Element> allChildren = new ArrayList();
        for (Element child : children) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    public String getType() {
        return type;
    }

    public final boolean hasChildren() {
        return this.children.size() > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Element element = (Element) o;

        return Objects.equal(this.id, element.id);
    }

    public final String getAttributeString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            builder.append(attr.getKey());
            builder.append("=\"");
            builder.append(attr.getValue());
            builder.append("\" ");
        }
        return builder.toString();
    }

}


