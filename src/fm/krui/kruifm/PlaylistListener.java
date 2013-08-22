package fm.krui.kruifm;

import java.util.ArrayList;
import java.util.HashMap;

public interface PlaylistListener {

    void onPlaylistFinish(ArrayList<HashMap<String, Track>> result);

}
