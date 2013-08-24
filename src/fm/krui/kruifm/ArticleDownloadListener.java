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
