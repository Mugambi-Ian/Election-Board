package com.nenecorp.kabuelectionboard.Views.Candidates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nenecorp.kabuelectionboard.DataModels.CANDIDATE;
import com.nenecorp.kabuelectionboard.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class CandidatesAdapter extends ArrayAdapter<CANDIDATE> {
    public CandidatesAdapter(@NonNull Context context, int resource, @NonNull ArrayList<CANDIDATE> objects) {
        super(context, resource, objects);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.candidates_list_item, parent, false);
        }
        CANDIDATE candidate = getItem(position);
        TextView name = itemView.findViewById(R.id.textView_Name);
        CircleImageView imageView = itemView.findViewById(R.id.candidate_Photo);
        name.setText(candidate.getCandidates_Name());
        Glide.with(getContext()).load(candidate.getCandidates_Photo()).into(imageView);
        return itemView;
    }
}
