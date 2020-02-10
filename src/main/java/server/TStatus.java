package server;

public class TStatus {
    private String screenName;
    private String createDate;
    private String text;
    private String userLocation;
    private String userProfileImage;

    public TStatus(String screenName, String createDate, String text, String userLocation, String userProfileImage) {
        this.screenName = screenName;
        this.createDate = createDate;
        this.text = text;
        this.userLocation = userLocation;
        this.userProfileImage = userProfileImage;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public String getText() {
        return text;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }
}
