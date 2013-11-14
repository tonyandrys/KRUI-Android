/*
 * fm.krui.kruifm.ArticleDownloadListener - ArticleDownloadListener.java
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

import java.util.ArrayList;

/**
 * fm.krui.kruifm - ArticleDownloadListener
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */

public interface ArticleDownloadListener {
    void onArticlesDownloaded(ArrayList<Article> downloadedList);
}
