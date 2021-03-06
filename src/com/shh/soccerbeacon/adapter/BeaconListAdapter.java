package com.shh.soccerbeacon.adapter;

import java.util.ArrayList;

import com.shh.soccerbeacon.R;
import com.shh.soccerbeacon.dto.BeaconListItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BeaconListAdapter extends BaseAdapter {

	public static ArrayList<BeaconListItem> beaconListItems = null;

	Context mContext = null;
	LayoutInflater vi;
	LayoutInflater mInflater;
	Resources localResources;

	public BeaconListAdapter(Context context, ArrayList<BeaconListItem> list) 
	{
		mContext = context;
		beaconListItems = list;
		vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		localResources = mContext.getResources();		
	}

	@Override
	public int getCount() {
		return beaconListItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		return beaconListItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		
		if (convertView == null) 
		{
			convertView = vi.inflate(R.layout.row_beaconinfo, null);
		
			holder = new ViewHolder();
			holder.tvBeaconName = (TextView) convertView.findViewById(R.id.tvBeaconName);
			holder.tvBeaconMajor = (TextView) convertView.findViewById(R.id.tvBeaconMajor);
			holder.tvBeaconMinor = (TextView) convertView.findViewById(R.id.tvBeaconMinor);
			holder.tvBeaconRSSI = (TextView) convertView.findViewById(R.id.tvBeaconRSSI);
			convertView.setTag(holder);
		}
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvBeaconName.setText(beaconListItems.get(position).getBeaconName());
		holder.tvBeaconMajor.setText("" + beaconListItems.get(position).getMajor());
		holder.tvBeaconMinor.setText("" + beaconListItems.get(position).getMinor());
		holder.tvBeaconRSSI.setText("" + beaconListItems.get(position).getRSSI());
			
		return convertView;
	}

	static class ViewHolder 
	{
		TextView tvBeaconName;
		TextView tvBeaconMajor;
		TextView tvBeaconMinor;
		TextView tvBeaconRSSI;
	}

	public void updateAdapter(ArrayList<BeaconListItem> result)
	{
		beaconListItems = result;
		notifyDataSetChanged();
	}
}