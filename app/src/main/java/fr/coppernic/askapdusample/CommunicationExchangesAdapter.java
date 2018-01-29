package fr.coppernic.askapdusample;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by benoist on 19/04/17.
 */

public class CommunicationExchangesAdapter extends ArrayAdapter<CommunicationExchanges> {

    private List<CommunicationExchanges> mExchanges;
    private LayoutInflater mLayoutInflater;
    private int mResourceId;


    public CommunicationExchangesAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<CommunicationExchanges> objects) {
        super(context, resource, objects);

        mExchanges = objects;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResourceId = resource;
    }

    @Override
    public int getCount() {
        return mExchanges.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(mResourceId, null);
        }

        TextView tvInData = (TextView)view.findViewById(R.id.tvInData);
        TextView tvOutData = (TextView)view.findViewById(R.id.tvOutData);
        TextView tvStatusData = (TextView)view.findViewById(R.id.tvStatusData);

        tvInData.setText(mExchanges.get(position).getDataSent());
        tvOutData.setText(mExchanges.get(position).getDataReceived());
        tvStatusData.setText(mExchanges.get(position).getStatus());

        return view;
    }
}
