package com.bloatit.web.html;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bloatit.web.server.Text;

/**
 * Class used to describe all preconstructed HtmlElements
 */
public abstract class HtmlElement extends HtmlNode {

    private final List<HtmlNode> children = new ArrayList<HtmlNode>();
    private final Tag tag;

    public HtmlElement(final String tag) {
        super();
        this.tag = new Tag(tag);
    }

    public HtmlElement() {
        super();
        this.tag = null;
    }

    public HtmlElement addAttribute(final String name, final String value) {
        tag.addAttribute(name, value);
        return this;
    }

    protected HtmlElement add(final HtmlNode html) {
        children.add(html);
        return this;
    }

    protected HtmlElement addText(final String text) {
        children.add(new HtmlText(text));
        return this;
    }
    
    /**
     * <p>
     * Sets the id of the html element :
     * 
     * <pre>
     * <element id="..." />
     * </pre>
     * 
     * </p>
     * <p>
     * Shortcut to element.addAttribute("id",value)
     * </p>
     * 
     * @param id the value of the id
     * @return the element
     */
    public HtmlElement setId(final String id) {
        addAttribute("id", id);
        return this;
    }

    /**
     * Finds the id of the element
     * 
     * <pre>
     * <element id="value" />
     * </pre>
     * 
     * @return The value contained in the attribute id of the element
     */
    public String getId() {
        return this.tag.getId();
    }

    /**
     * Sets the css class of the element
     * <p>
     * Shortcut for element.addattribute("class",cssClass)
     * </p>
     * 
     * @param cssClass
     * @return
     */
    public HtmlElement setCssClass(final String cssClass) {
        addAttribute("class", cssClass);
        return this;
    }

    @Override
    public Iterator<HtmlNode> iterator() {
        return children.iterator();
    }

    @Override
    public final void write(final Text txt) {
        if (tag != null) {
            if (iterator().hasNext()) {
                txt.writeLine(tag.getOpenTag());
                for (final HtmlNode html : this) {
                    txt.indent();
                    html.write(txt);
                    txt.unindent();
                }
                txt.writeLine(tag.getCloseTag());
            } else {
                txt.writeLine(tag.getClosedTag());
            }
        } else {
            for (final HtmlNode html : this) {
                txt.indent();
                html.write(txt);
                txt.unindent();
            }
        }
    }

}
