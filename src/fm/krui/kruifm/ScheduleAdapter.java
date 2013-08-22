package fm.krui.kruifm;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * ScheduleAdapter
 * Adapter used to display the KRUI show schedule.
 */

public class ScheduleAdapter extends BaseAdapter {
 
    private Activity activity;
    private static Context context;
    private ArrayList <HashMap <String,String>> data;
    private static LayoutInflater inflater=null;
    private ArrayList <Integer> posChanges;
 
    public ScheduleAdapter(Activity a, ArrayList <HashMap <String,String>> d, Context c, ArrayList<Integer> p) {
        activity=a;
    	data=d;
    	posChanges = p;
        context=c;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return data.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
    
    // Override to disable clicking the weekday banners 
    @Override
    public boolean isEnabled(int position) {
    	if (posChanges.contains(position)) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    @Override
    public int getViewTypeCount() {
    	return 2; 
    }
    
    // FIXME: Get rid of these magic numbers.
    @Override
    public int getItemViewType(int position) {
    	if (posChanges.contains(position)) {
    		// Display a banner
    		return 0;
    	} 
    	else {
    		// Display show infos
    		return 1;
    	}
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View vi = convertView;
        HashMap <String, String> entry = new HashMap<String, String>();
        entry = data.get(position);
        ViewHolder holder;
    	
        // TODO: Not quite satisfied with the flow here, it's messy.
        if (convertView == null) {
        	
        	// Cache views for performance
        	holder = new ViewHolder();
        	
        	// If this row starts a new day of the week, display a banner.
        	if (getItemViewType(position) == 0) {
        		vi = inflater.inflate(R.layout.schedule_dayofweek, null);
        		holder.dayOfWeekView = (TextView)vi.findViewById(R.id.list_day_of_week);
        	} 
        	
        	// Otherwise, display show information. 
        	else if (getItemViewType(position) == 1){
        		vi = inflater.inflate(R.layout.schedule_listrow, null);
        		holder.titleView = (TextView)vi.findViewById(R.id.list_show_title);
        		holder.startTimeView = (TextView)vi.findViewById(R.id.list_show_start_time);
        	}
    		vi.setTag(holder);
        }

        holder = (ViewHolder)vi.getTag();
        
        // Write day of the week on banners
        if (getItemViewType(position) == 0) {
        	int weekday = Integer.parseInt(entry.get("day_of_week"));
        	holder.dayOfWeekView.setText(Utils.weekdayIntToStr(weekday));
        } 
        
        // Write show info on all other rows
        else if (getItemViewType(position) == 1) {         
            holder.titleView.setText(entry.get("title"));
            holder.startTimeView.setText(entry.get("start_time"));
        }
		return vi;   
    }
    
    /*
     * Holds location of views to avoid expensive findViewById operations.
     */
    static class ViewHolder {
    	TextView titleView;
    	TextView startTimeView;
    	TextView dayOfWeekView;
    	
    }
}

