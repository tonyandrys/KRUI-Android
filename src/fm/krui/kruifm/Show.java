package fm.krui.kruifm;

public class Show {
	
	private String id;
	private int station;
	private String title;

    private String startTimeUTC;
	private String endTimeUTC;
    private int startTimeMinutes;
    private int endTimeMinutes;
	private String htmlLink;
	private String description;
	private int dayOfWeek;

	// default constructor
	Show() {
		
	}

    public Show(String id, int station, String title, String startTimeUTC, String endTimeUTC, int startTimeMinutes, int endTimeMinutes, String htmlLink, String description, int dayOfWeek) {
        this.id = id;
        this.station = station;
        this.title = title;
        this.startTimeUTC = startTimeUTC;
        this.endTimeUTC = endTimeUTC;
        this.startTimeMinutes = startTimeMinutes;
        this.endTimeMinutes = endTimeMinutes;
        this.htmlLink = htmlLink;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStation() {
        return station;
    }

    public void setStation(int station) {
        this.station = station;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTimeUTC() {
        return startTimeUTC;
    }

    public void setStartTimeUTC(String startTimeUTC) {
        this.startTimeUTC = startTimeUTC;
    }

    public String getEndTimeUTC() {
        return endTimeUTC;
    }

    public void setEndTimeUTC(String endTimeUTC) {
        this.endTimeUTC = endTimeUTC;
    }

    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    public void setStartTimeMinutes(int startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }

    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public void setEndTimeMinutes(int endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    public String getHtmlLink() {
        return htmlLink;
    }

    public void setHtmlLink(String htmlLink) {
        this.htmlLink = htmlLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
