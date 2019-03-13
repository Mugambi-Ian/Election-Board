package com.nenecorp.kabuelectionboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.support.constraint.Constraints.TAG;

public class KUEB {
    public static Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    public static String getTimefromTimePicker(TimePicker view) {
        int hourOfDay = view.getCurrentHour();
        int minute = view.getCurrentMinute();
        String hour = String.format("%02d", hourOfDay);
        String min = String.format("%02d", minute);
        String time = hour + ":" + min + ":00";
        return time;
    }

    public static class CONTRACT implements BaseColumns {
        public static final String ELECTIONS = "ELECTIONS";
        public static final String election_Time = "election_Time";
        public static final String election_Date = "election_Date";
        public static final String election_Year = "election_Year";
        public static final String election_Duration = "election_Duration";
        public static final String KUSO_POSITIONS = "KUSO_POSITIONS";
        public static final String position = "Position";
        public static final String Ballot = "Ballot";
        public static final String ballot_Category = "ballot_Category";
        public static final String total_Candidates = "total_Candidates";
        public static final String candidates_Name = "candidates_Name";
        public static final String candidates_ID = "candidates_ID";
        public static final String candidates_Photo = "candidates_Photo";
        public static final String Candidates = "Candidates";
        public static final String ACTIVE = "ACTIVE";
        public static final String FINISHED = "FINISHED";
        public static final String election_Status = "election_Status";
        public static final String PREPARING = "PREPARING";
    }

}
