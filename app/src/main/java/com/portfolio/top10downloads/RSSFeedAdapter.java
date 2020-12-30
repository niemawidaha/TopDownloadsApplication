package com.portfolio.top10downloads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RSSFeedAdapter extends ArrayAdapter {

    // private members:
    private static final String TAG = "RSSFeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<RSSFeedEntry> applications;

    // inflate xml resource to display view widgets:
    public RSSFeedAdapter(@NonNull Context context, int resource, List<RSSFeedEntry> applications) {
        super(context, resource);

        // assign the resource to our resource field:
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }

    // override this method to display the view:
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        // use convertView: to make this method more efficient
        // before a new view was being created every single time
        // by utilizing convertView, im re-using one view
        if(convertView == null){
            // parent: sent back from the Android framework
            convertView = layoutInflater.inflate(layoutResource,parent,false);

            // creating a new view
            // .setTag() is an object thats casted to what's needed
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // LEAST EFFICIENT METHOD TO DISPLAYING THE RETRIEVED RSS FEED DATA:
        // find the widgets from the view retrieved from the view
        //        TextView tvName = convertView.findViewById(R.id.tv_Name);
        //        TextView tvArtist = convertView.findViewById(R.id.tv_Artist);
        //        TextView tvSummary = convertView.findViewById(R.id.tv_Summary);

        // retrieve the current application from the list
        RSSFeedEntry currentApp = applications.get(position);

        // update the view on the screen
        viewHolder.tv_Name.setText(currentApp.getName());
        viewHolder.tv_Artist.setText(currentApp.getArtist());
        viewHolder.tv_Summary.setText(currentApp.getSummary());

        // display the data within the current App
        return convertView;
    }

    // returns the number of entries that are in the list:
    @Override
    public int getCount() {
        return applications.size();
    }

    // CREATE INNER CLASS: VIEWHOLDER
    private class ViewHolder{

        // members:
        private TextView tv_Name;
        private TextView tv_Artist;
        private TextView tv_Summary;

        ViewHolder(View view){
            this.tv_Name = view.findViewById(R.id.tv_Name);
            this.tv_Artist = view.findViewById(R.id.tv_Artist);
            this.tv_Summary = view.findViewById(R.id.tv_Summary);
        }
    }
}
