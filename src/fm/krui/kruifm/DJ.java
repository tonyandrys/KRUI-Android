

package fm.krui.kruifm;

/**
 * fm.krui.kruifm - DJ
 * Created by Tony Andrys on 07/11/{2013}.
 */

public class DJ {

    String firstName;
    String lastName;
    String url;
    String bio;
    String twitter;
    String imageURL;

    // JSON Keys
    public static final String KEY_FIRST_NAME = "firstname";
    public static final String KEY_LAST_NAME = "lastname";
    public static final String KEY_URL = "url";
    public static final String KEY_BIO = "bio";
    public static final String KEY_TWITTER = "twitter";
    public static final String KEY_IMAGE = "image";

    // Default constructor, returns a blank DJ object.
    public DJ() {

    }

    /* Partially parameterized constuctor, used for convenience as names are always in the playlist JSON. Social media
    resources, if not set by the individual DJ, are not passed. */
    public DJ(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Fully parameterized constructor.
    public DJ(String firstName, String lastName, String url, String bio, String twitter, String image) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.url = url;
        this.bio = bio;
        this.twitter = twitter;
        this.imageURL = image;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String image) {
        this.imageURL = image;
    }
}
