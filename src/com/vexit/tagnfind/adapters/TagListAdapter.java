package com.vexit.tagnfind.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vexit.tagnfind.R;
import com.vexit.tagnfind.objects.TagResults;
import com.vexit.tagnfind.views.MainActivity;

public class TagListAdapter extends BaseAdapter {
	private static ArrayList<TagResults> arrayList;
	private LayoutInflater mInflater;
	private Context mContext;
	private int[] colors = new int[] { 0x30ffffff, 0x30808080 };

	public TagListAdapter(Context context, ArrayList<TagResults> results) {
		arrayList = results;
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.tag_row_item, null);
			holder = new ViewHolder();
			holder.txtTAG = (TextView) convertView.findViewById(R.id.txtTAG);
			holder.btnDeleteItem = (Button) convertView
					.findViewById(R.id.btn_delete_item);
			holder.btnNavigate = (ImageView) convertView
					.findViewById(R.id.imgvw_navigate);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txtTAG.setSelected(true);
		holder.txtTAG.setText(arrayList.get(position).getTagName());
		holder.btnDeleteItem.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.deleteEvent(arrayList.get(position).getTagName(),
						arrayList.get(position).getAddress(), position);
			}
		});
		holder.btnNavigate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*
				 * MainActivity.deleteEvent(arrayList.get(position).getTagName(),
				 * arrayList.get(position).getAddress(), position);
				 */
				String uri = "http://maps.google.com/maps?&daddr="
						+ arrayList.get(position).getLatitude() + ","
						+ arrayList.get(position).getLongitude();

				MainActivity.navigateLocation(uri);

			}
		});
		System.out.println(position);
		if ((position % 2) == 1) {
			convertView.setBackgroundColor(Color.WHITE);
			// convertView.setBackgroundResource(R.drawable.listselector_even);
		} else {
			// convertView.setBackgroundResource(R.drawable.listselector_odd);
			convertView.setBackgroundColor(Color.LTGRAY);
		}
		// convertView.refreshDrawableState();

		return convertView;
	}

	static class ViewHolder {
		TextView txtTAG;
		Button btnDeleteItem;
		ImageView btnNavigate;
	}

}
