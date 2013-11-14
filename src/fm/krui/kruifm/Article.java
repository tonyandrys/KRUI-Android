/*
 * fm.krui.kruifm.Article - Article.java
 *
 * (C) 2013 - Tony Andrys
 * http://www.tonyandrys.com
 *
 * Created: 11/14/2013
 *
 * ---
 *
 * This file is part of KRUI.FM.
 *
 * KRUI.FM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * KRUI.FM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KRUI.FM.  If not, see <http://www.gnu.org/licenses/>.
 */

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