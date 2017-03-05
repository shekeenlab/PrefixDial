package jp.co.shekeenlab.PrefixDial;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PrefixListAdapter extends ArrayAdapter<PrefixData> {
	
	private LayoutInflater mInflater;
	
	public PrefixListAdapter(Context context, int textViewResourceId, List<PrefixData> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/* Viewは再利用される */
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.selection, null);
		}
		TextView textTitle = (TextView)convertView.findViewById(R.id.textTitle);
		TextView textPrefix = (TextView)convertView.findViewById(R.id.textPrefix);
		PrefixData data = getItem(position);
		textTitle.setText(data.title);
		if(data.prefix == null || data.prefix.length() == 0){
			textPrefix.setText(" ");
		}
		else{
			textPrefix.setText(data.prefix);
		}
		convertView.setTag(data);
		return convertView;
	}

	public void replace(PrefixData item1, PrefixData item2){
		int position1 = getPosition(item1);
		int position2 = getPosition(item2);

		if(position2 > position1){
			replaceInOrder(item1, item2);
		}
		else{
			replaceInOrder(item2, item1);
		}
	}

	private void replaceInOrder(PrefixData smaller, PrefixData larger){
		int posLarger = getPosition(larger);
		int posSmaller = getPosition(smaller);

		remove(larger);
		insert(smaller, posLarger);
		/* smallerは2つListに入っているので、若いほうをremoveで削除する */
		remove(smaller);
		insert(larger, posSmaller);
	}
}
