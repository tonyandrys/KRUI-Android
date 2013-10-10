package fm.krui.kruifm;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * fm.krui.kruifm - ArticleViewActivity
 *
 * @author Tony Andrys
 *         Created: 08/23/2013
 *         (C) 2013 - Tony Andrys
 */

public class ArticleViewActivity extends Activity {

    final private String TAG = ArticleViewActivity.class.getName();

    final public String KEY_CONTENT = "content";
    final public String KEY_FIRST_NAME = "first_name";
    final public String KEY_LAST_NAME = "last_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_view_layout);

        // Show loading view
        showLoadingScreen(true);

        // Prepare ActionBar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Retrieve article object from intent
        Article article = new Article();
        article = (Article)getIntent().getSerializableExtra(article.ARTICLE_INTENT);

        // Instantiate views
        TextView articleTitleTextView = (TextView)findViewById(R.id.view_article_title_textview);
        TextView authorTextView = (TextView)findViewById(R.id.view_article_author_textview);
        TextView articleContentTextView = (TextView)findViewById(R.id.view_article_text_textview);

        // Apply extracted info to views
        articleTitleTextView.setText(article.getTitle());
        authorTextView.setText(getAuthor(article));
        articleContentTextView.setText(Html.fromHtml(getArticleText(article)));

        // Hide loading view
        showLoadingScreen(false);
    }

    /**
     * Extracts author values from embedded JSON object in an Article
     * @param article Article object
     * @return Author full name as a String
     */
    private String getAuthor(Article article) {

        String firstName = "";
        String lastName = "";

        // Get the JSON Object and extact first and last name elements
        try {
            JSONObject authorObj = new JSONObject(article.getAuthorObj());
            firstName = authorObj.getString(KEY_FIRST_NAME);
            lastName = authorObj.getString(KEY_LAST_NAME);
        } catch (JSONException e) {
            Log.e(TAG, "Caught JSONException - Could not extract author name from JSON Object!");
            e.printStackTrace();
        }

        // Concatenate the two values and return
        return "By: " + firstName + " " + lastName;
    }

    /**
     * Extracts article text from embedded JSON object in an Article.
     * @param article Article object
     * @return full article text as a String
     */
    private String getArticleText(Article article) {

        String articleText = "";

        // Get the content JSON Object from Article object and extract articleText from it.
        try {
            JSONObject contentObj = new JSONObject(article.getContentObj());
            articleText = contentObj.getString(KEY_CONTENT);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException caught - could not extract article content!");
            e.printStackTrace();
        }

        return articleText;
    }

    /**
     * Toggles the loading view on and off.
     * @param isLoading true to display, false to hide
     */
    private void showLoadingScreen(boolean isLoading) {
        FrameLayout loadingView = (FrameLayout)findViewById(R.id.article_view_loading_framelayout);

        if (isLoading) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.INVISIBLE);
        }
    }

}
