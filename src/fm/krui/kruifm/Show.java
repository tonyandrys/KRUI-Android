package fm.krui.kruifm;

public class Show {
	
	private String _id;
	private int _station;
	private String _title;
	private String _startTime;
	private String _endTime;
	private String _htmlLink;
	private String _description;
	private int _dayOfWeek;
	
	// Stored as int since SQLite cannot store boolean values natively. 1 = true, 0 = false.
	private int _music;
	private int _talk;
	private int _sports;
	private int _special;
	
	// default constructor
	Show() {
		
	}
	
	// fully parameterized constructor
	Show (String id, int station, String title, int dayOfWeek, String startTime, String endTime, String htmlLink, String description, int music, int talk, int sports, int special) {
		_id = id;
		_station = station;
		_title = title;
		_dayOfWeek = dayOfWeek;
		_startTime = startTime;
		_endTime = endTime;
		_htmlLink = htmlLink;
		_description = description;
		_music = music;
		_talk = talk;
		_sports = sports;
		_special = special;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
	
	public int get_station() {
		return _station;
	}
	
	public void set_station(int station) {
		this._station = station;
	}
	
	public String get_title() {
		return _title;
	}

	public void set_title(String _title) {
		this._title = _title;
	}
	
	public void set_dayOfWeek(int dayOfWeek) {
		this._dayOfWeek = dayOfWeek;
	}
	
	public int get_dayOfWeek() {
		return _dayOfWeek;
	}

	public String get_startTime() {
		return _startTime;
	}

	public void set_startTime(String _startTime) {
		this._startTime = _startTime;
	}

	public String get_endTime() {
		return _endTime;
	}

	public void set_endTime(String _endTime) {
		this._endTime = _endTime;
	}

	public String get_htmlLink() {
		return _htmlLink;
	}

	public void set_htmlLink(String _htmlLink) {
		this._htmlLink = _htmlLink;
	}

	public String get_description() {
		return _description;
	}

	public void set_description(String _description) {
		this._description = _description;
	}

	public int get_music() {
		return _music;
	}

	public void set_music(int _music) {
		this._music = _music;
	}

	public int get_talk() {
		return _talk;
	}

	public void set_talk(int _talk) {
		this._talk = _talk;
	}

	public int get_sports() {
		return _sports;
	}

	public void set_sports(int _sports) {
		this._sports = _sports;
	}

	public int get_special() {
		return _special;
	}

	public void set_special(int _special) {
		this._special = _special;
	}

}
