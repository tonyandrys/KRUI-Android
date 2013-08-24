package fm.krui.kruifm;

/**
 * fm.krui.kruifm - Article
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */

import java.io.Serializable;

/**
 * Represents the content of a Wordpress article.
 */
public class Article implements Serializable {

    final public String ARTICLE_INTENT = "article";

    private String title;
    private String excerpt;

    /* Since these are not shown unless the article is clicked, extract these objects
    only when an article is clicked. */
    private String JSONauthorObj;
    private String JSONcontentObj;

    public Article() {

    }

    public Article(String title, String excerpt) {
        this.title = title;
        this.excerpt = excerpt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getAuthorObj() {
        return JSONauthorObj;
    }

    public void setAuthorObj(String authorObj) {
        this.JSONauthorObj = authorObj;
    }

    public String getContentObj() {
        return JSONcontentObj;
    }

    public void setContentObj(String contentObj) {
        this.JSONcontentObj = contentObj;
    }
}