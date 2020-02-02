package com.nenecorp.kabuelectionboard.Views.Positions;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nenecorp.kabuelectionboard.DataModels.BALLOT;
import com.nenecorp.kabuelectionboard.R;

import java.util.ArrayList;

public class PostionsAdapter extends ArrayAdapter<BALLOT> {
    public PostionsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<BALLOT> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.postions_list_item, parent, false);
        }
        BALLOT ballot = getItem(position);
        TextView positionTextView = itemView.findViewById(R.id.textView_position);
        TextView total_Candidates = itemView.findViewById(R.id.total_candidates);
        positionTextView.setText(ballot.getBallot_Category());
        total_Candidates.setText("" + ballot.getCandidates().size());
        return itemView;
    }
}
