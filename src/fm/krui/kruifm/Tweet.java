package fm.krui.kruifm;

/**
 * fm.krui.kruifm - Tweet
 *
 * @author Tony Andrys
 *         Created: 08/30/2013
 *         (C) 2013 - Tony Andrys
 */

import java.util.HashMap;

/**
 * Object represents a single tweet.
 */
public class Tweet {

    HashMap<String, String> createdAt;
    String text;
    String screenName;
    String profileImageUrl;
    String dateOfTweet;
    String timeOfTweet;
    String timeZoneOffset;

    public Tweet() {
    }

    public Tweet(String screenName, String text, String profileImageUrl, HashMap<String, String> createdAt) {
        this.screenName = screenName;
        this.text = text;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
    }

    public HashMap<String, String> getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(HashMap<String, String> createdAt) {
        this.createdAt = createdAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDateOfTweet() {
        return dateOfTweet;
    }

    public void setDateOfTweet(String dateOfTweet) {
        this.dateOfTweet = dateOfTweet;
    }

    public String getTimeOfTweet() {
        return timeOfTweet;
    }

    public void setTimeOfTweet(String timeOfTweet) {
        this.timeOfTweet = timeOfTweet;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

}
