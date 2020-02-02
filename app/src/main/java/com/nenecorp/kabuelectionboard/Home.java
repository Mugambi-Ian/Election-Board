package com.nenecorp.kabuelectionboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nenecorp.kabuelectionboard.DataModels.BALLOT;
import com.nenecorp.kabuelectionboard.DataModels.CANDIDATE;
import com.nenecorp.kabuelectionboard.Listeners.SwipeListener;
import com.nenecorp.kabuelectionboard.Views.Candidates.CandidatesAdapter;
import com.nenecorp.kabuelectionboard.Views.Positions.PostionsAdapter;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {
    private static final String TAG = "Home";
    private static String ELECTION_YEAR;
    private static String ELECTION_ID;
    private static String ELECTION_DURATION;
    private static String ELECTION_DATE;
    private static String ELECTION_TIME;
    private static String BALLOT_ID;
    private static String deadLine_Date, deadline_Start, deadLine_Stop;
    private static Timer timer = new Timer();
    private final int NEW_ELECTION = 1, LOAD_ELECTION = 2;
    private final String ADD_SCREEN = "ADD_SCREEN", CREATE_ELECTION = "CREATE_ELECTION";
    private final String on_ELECTION_DATE = "on_ELECTION_DATE";
    private final String on_ELECTION_TIME = "on_ELECTION_TIME";
    private Handler mHandler;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference elections = database.getReference().child(KUEB.CONTRACT.ELECTIONS), kuso_Positions = database.getReference().child(KUEB.CONTRACT.KUSO_POSITIONS);
    private RelativeLayout welcomeScreen, dashBoard, dateTimeSettings, layout_Positions, layout_Candidates, addCandidate, back_FromCandidate;
    private Dialog creatingElection, addPosition, savingCandidate;
    private Animation fadeIn, fadeOut, slide_FromRight, slide_Left, slide_Right, slide_FromLeft;
    private EditText candidatesName;
    private Uri selectedImage;
    private boolean allSet = false, isElectionOnGoing;
    private String category;
    private LinearLayout btn_ProceedFromName, btn_ProceedFromPicture;
    private boolean mActive = false;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (mActive) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                int hourOfDay = calendar.getTime().getHours();
                String hour = String.format("%02d", hourOfDay);
                int minute = calendar.getTime().getMinutes();
                String min = String.format("%02d", minute);
                String time = hour + ":" + min + ":00";
                DateFormat format = new SimpleDateFormat("hh:mm:ss");
                try {
                    Date current = format.parse(time);
                    Date Time = format.parse(getElectionTime());
                    Calendar c = Calendar.getInstance();
                    c.setTime(Time);
                    c.add(Calendar.MINUTE, -1);
                    Date electionTime = c.getTime();

                    if (current.after(electionTime)) {
                        Log.i(TAG, "run: ");
                        refreshActivity();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mHandler.postDelayed(mRunnable, 1000);
            }
        }
    };

    public Home() {
    }

    public static String getDeadLine_Date() {
        return deadLine_Date;
    }

    public static void setDeadLine_Date(String deadLine_Date) {
        Home.deadLine_Date = deadLine_Date;
    }

    public static String getDeadline_Start() {
        return deadline_Start;
    }

    public static void setDeadline_Start(String deadline_Start) {
        Home.deadline_Start = deadline_Start;
    }

    public static String getDeadLine_Stop() {
        return deadLine_Stop;
    }

    public static void setDeadLine_Stop(String deadLine_Stop) {
        Home.deadLine_Stop = deadLine_Stop;
    }

    public static String getElectionDate() {
        return ELECTION_DATE;
    }

    public static void setElectionDate(String electionDate) {
        ELECTION_DATE = electionDate;
    }

    public static String getElectionDuration() {
        return ELECTION_DURATION;
    }

    public static void setElectionDuration(String electionDuration) {
        ELECTION_DURATION = electionDuration;
    }

    public static String getElectionTime() {
        return ELECTION_TIME;
    }

    public static void setElectionTime(String electionTime) {
        ELECTION_TIME = electionTime;
    }

    public static String getElectionYear() {
        return ELECTION_YEAR;
    }

    public static void setElectionYear(String electionYear) {
        ELECTION_YEAR = electionYear;
    }

    public static String getElectionId() {
        return ELECTION_ID;
    }

    public static void setElectionId(String electionId) {
        ELECTION_ID = electionId;
    }

    public static String getBallotId() {
        return BALLOT_ID;
    }

    public static void setBallotId(String ballotId) {
        BALLOT_ID = ballotId;
    }

    public boolean electionIsOnGoing() {
        return isElectionOnGoing;
    }

    public void setElectionStatus(boolean electionOnGoing) {
        isElectionOnGoing = electionOnGoing;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        creatingElection = new Dialog(Home.this);
        creatingElection.setContentView(R.layout.pop_up_creating_election);
        creatingElection.setCancelable(false);
        creatingElection.setCanceledOnTouchOutside(false);
        addPosition = new Dialog(Home.this);
        addPosition.setContentView(R.layout.pop_up_add_position);
        addPosition.setCanceledOnTouchOutside(false);
        savingCandidate = new Dialog(Home.this);
        savingCandidate.setContentView(R.layout.pop_up_saving_candidate);
        savingCandidate.setCancelable(false);
        savingCandidate.setCanceledOnTouchOutside(false);
        addCandidate = findViewById(R.id.Layout_addCandidate);
        welcomeScreen = findViewById(R.id.layout_welcomeScreen);
        dashBoard = findViewById(R.id.Layout_addScreen);
        dateTimeSettings = findViewById(R.id.Layout_date_time);
        layout_Positions = findViewById(R.id.Layout_Positions);
        layout_Candidates = findViewById(R.id.Layout_Candidates);
        fadeOut = AnimationUtils.loadAnimation(Home.this, R.anim.fade_out);
        fadeIn = AnimationUtils.loadAnimation(Home.this, R.anim.fade_in);
        slide_FromRight = AnimationUtils.loadAnimation(Home.this, R.anim.slide_back_from_right);
        slide_Left = AnimationUtils.loadAnimation(Home.this, R.anim.slide_left);
        slide_Right = AnimationUtils.loadAnimation(Home.this, R.anim.slide_right);
        slide_FromLeft = AnimationUtils.loadAnimation(Home.this, R.anim.slide_right_from_left);
        getElectionStatus();
    }

    private void getElectionStatus() {
        elections.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DatabaseReference elect = null;
                for (DataSnapshot year : dataSnapshot.getChildren()) {
                    for (DataSnapshot election : year.getChildren()) {
                        if (election.child(KUEB.CONTRACT.election_Status).getValue(String.class).equals(KUEB.CONTRACT.PREPARING)) {
                            setElectionStatus(true);
                            setElectionYear(year.getKey());
                            setElectionId(election.getKey());
                            elect = election.getRef();
                            setElectionTime(election.child(KUEB.CONTRACT.election_Time).getValue(String.class));
                            setElectionDuration(election.child(KUEB.CONTRACT.election_Duration).getValue(String.class));
                            setElectionDate(election.child(KUEB.CONTRACT.election_Date).getValue(String.class));
                        } else if (election.child(KUEB.CONTRACT.election_Status).getValue(String.class).equals(KUEB.CONTRACT.ACTIVE)) {
                            setElectionStatus(true);
                            setElectionYear(year.getKey());
                            setElectionId(election.getKey());
                            setElectionTime(election.child(KUEB.CONTRACT.election_Time).getValue(String.class));
                            setElectionDuration(election.child(KUEB.CONTRACT.election_Duration).getValue(String.class));
                            setElectionDate(election.child(KUEB.CONTRACT.election_Date).getValue(String.class));
                        }
                    }
                }
                if (electionIsOnGoing()) {
                    DataSnapshot ballotsSnapshot = dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.Ballot);
                    ArrayList<BALLOT> ballots = new ArrayList<>();
                    for (DataSnapshot ball : ballotsSnapshot.getChildren()) {
                        GenericTypeIndicator<ArrayList<CANDIDATE>> ans = new GenericTypeIndicator<ArrayList<CANDIDATE>>() {
                        };
                        if (ball.hasChild(KUEB.CONTRACT.Candidates)) {
                            ArrayList<CANDIDATE> candidates = ball.child(KUEB.CONTRACT.Candidates).getValue(ans);
                            String category = ball.child(KUEB.CONTRACT.ballot_Category).getValue(String.class);
                            ballots.add(new BALLOT(category, candidates));
                        } else {
                            ArrayList<CANDIDATE> candidates = new ArrayList<>();
                            String category = ball.child(KUEB.CONTRACT.ballot_Category).getValue(String.class);
                            ballots.add(new BALLOT(category, candidates));
                        }

                    }
                    Calendar tDate = Calendar.getInstance();
                    String year = "" + tDate.get(Calendar.YEAR);
                    Calendar eDate = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, EEE - hh:mm:ss");
                    Log.i(TAG, "onDataChange: " + getElectionDate() + " - " + getElectionTime());
                    Log.i(TAG, "onDataChange: " + dateFormat.format(tDate.getTime()));
                    try {
                        eDate.setTime(dateFormat.parse(getElectionDate() + " - " + getElectionTime()));
                        Calendar dDate = Calendar.getInstance();
                        dDate.add(Calendar.HOUR, Integer.parseInt(getElectionDuration()));
                        if (tDate.before(dDate) && getElectionYear().equals(year) && tDate.get(Calendar.DAY_OF_MONTH) == eDate.get(Calendar.DAY_OF_MONTH)) {
                            DateFormat dT = new SimpleDateFormat("hh:mm a");
                            setDeadline_Start(dT.format(tDate.getTime()));
                            setDeadLine_Stop(dT.format(dDate.getTime()));
                            dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).getRef().setValue(KUEB.CONTRACT.ACTIVE);
                            setDateTime(getElectionYear(), getElectionDate(), getElectionTime(), getElectionDuration(), on_ELECTION_TIME);
                        } else if (tDate.before(eDate)) {
                            Log.i(TAG, "onDataChange: " + true);
                            mHandler = new Handler();
                            mActive = true;
                            mHandler.post(mRunnable);
                            setDateTime(getElectionYear(), getElectionDate(), getElectionTime(), getElectionDuration(), on_ELECTION_DATE);
                        } else if (tDate.after(dDate)) {
                            Log.i(TAG, "onDataChange: after deadline");
                            dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).getRef().setValue(KUEB.CONTRACT.FINISHED);
                            refreshActivity();
                        } else {
                            loadElection(ballots, elect, LOAD_ELECTION);
                        }
                       /*
                        Date electiondate = dateFormat.parse(getElectionDate());
                        if (getElectionYear().equals("" + year) && d.equals(getElectionDate())) {
                            int hourOfDay = tDate.get(Calendar.HOUR_OF_DAY);
                            String hour = String.format("%02d", hourOfDay);
                            int minute = tDate.get(Calendar.MINUTE);
                            String min = String.format("%02d", minute);
                            String time = hour + ":" + min + ":00";
                            DateFormat format = new SimpleDateFormat("hh:mm:ss");
                            try {
                                Date current = format.parse(time);
                                Date Time = format.parse(getElectionTime());
                                Calendar D_Line = Calendar.getInstance();
                                D_Line.setTime(Time);
                                D_Line.add(Calendar.HOUR, Integer.parseInt(getElectionDuration()));
                                Date deadLine = D_Line.getTime();
                                Calendar c = Calendar.getInstance();
                                c.setTime(Time);
                                c.add(Calendar.MINUTE, -1);
                                Date electionTime = c.getTime();
                                Log.i(TAG, "onDataChange: " + current.toGMTString() + " " + electionTime.toGMTString() + " " + deadLine.toGMTString());
                                if (current.after(electionTime) && current.before(deadLine)) {
                                    DateFormat dT = new SimpleDateFormat("hh:mm a");
                                    setDeadline_Start(dT.format(current));
                                    setDeadLine_Stop(dT.format(deadLine));
                                    dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).getRef().setValue(KUEB.CONTRACT.ACTIVE);
                                    setDateTime(getElectionYear(), getElectionDate(), getElectionTime(), getElectionDuration(), on_ELECTION_TIME);
                                } else if (current.before(electionTime)) {
                                    Log.i(TAG, "onDataChange: " + true);
                                    mHandler = new Handler();
                                    mActive = true;
                                    mHandler.post(mRunnable);
                                    setDateTime(getElectionYear(), getElectionDate(), getElectionTime(), getElectionDuration(), on_ELECTION_DATE);
                                } else if (deadLine.before(current)) {
                                    Log.i(TAG, "onDataChange: after deadline");
                                    dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).getRef().setValue(KUEB.CONTRACT.FINISHED);
                                    refreshActivity();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        } else if (electiondate.before(tDate)) {
                            dataSnapshot.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).getRef().setValue(KUEB.CONTRACT.FINISHED);
                            refreshActivity();
                        } else {
                            loadElection(ballots, elect, LOAD_ELECTION);
                        }
                      */
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    startElectionPreparation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startElectionPreparation() {
        welcomeScreen.setVisibility(View.VISIBLE);
        CardView startElection = findViewById(R.id.Start_Election);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.startAnimation(fadeOut);
        progressBar.setVisibility(View.GONE);
        startElection.startAnimation(fadeIn);
        startElection.setVisibility(View.VISIBLE);
        startElection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInitialDate();
            }
        });
    }

    private void setInitialDate() {
        setDateTime("", "", "", "", CREATE_ELECTION);
    }

    private void loadElection(final ArrayList<BALLOT> ballots,
                              final DatabaseReference election, int status) {
        CardView btn_DateTime = findViewById(R.id.Btn_dateTime);
        ListView positions = findViewById(R.id.ListView_Positions);
        LinearLayout ben_addPosition = findViewById(R.id.Btn_addPosition);
        final PostionsAdapter adapter = new PostionsAdapter(Home.this, R.layout.activity_home, ballots);
        final LinearLayout back = findViewById(R.id.Btn_backFromCandidates);
        if (status == NEW_ELECTION) {
            creatingElection.dismiss();
            dashBoard.setVisibility(View.VISIBLE);
            dashBoard.startAnimation(slide_FromLeft);
            dateTimeSettings.startAnimation(slide_Right);
            dateTimeSettings.setVisibility(View.GONE);
        } else if (status == LOAD_ELECTION) {
            welcomeScreen.setVisibility(View.GONE);
            dashBoard.setVisibility(View.VISIBLE);
        }
        positions.setAdapter(adapter);
        ben_addPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPosition(election, ballots, adapter);
            }
        });
        btn_DateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateTime(getElectionYear(), getElectionDate(), getElectionTime(), getElectionDuration(), ADD_SCREEN);
            }
        });
        positions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                DatabaseReference positionReference = election.child(KUEB.CONTRACT.Ballot).child("" + position);
                setBallotId("" + position);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        backToAddScreen(adapter, ballots.get(position).getCandidates());
                    }
                });
                editCandidates(ballots.get(position).getCandidates(), ballots.get(position).getBallot_Category(), positionReference);
            }
        });
    }

    private void addPosition(final DatabaseReference election,
                             final ArrayList<BALLOT> ballots, final PostionsAdapter adapter) {
        addPosition.show();
        final EditText position = addPosition.findViewById(R.id.editText_Category);
        LinearLayout ben_addPosition = addPosition.findViewById(R.id.Btn_addPosition);
        ben_addPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = position.getText().toString();
                if (!data.equals("")) {
                    savePosition(data, election, ballots, adapter);
                }
            }
        });
    }

    private void savePosition(final String data, DatabaseReference election,
                              final ArrayList<BALLOT> ballots, final PostionsAdapter adapter) {
        kuso_Positions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id = String.valueOf(dataSnapshot.getChildrenCount());
                DatabaseReference pos = dataSnapshot.child(id).getRef();
                pos.child(KUEB.CONTRACT.position).setValue(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        election.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id = String.valueOf(dataSnapshot.child(KUEB.CONTRACT.Ballot).getChildrenCount());
                DatabaseReference ballot = dataSnapshot.child(KUEB.CONTRACT.Ballot).child(id).getRef();
                ballot.child(KUEB.CONTRACT.ballot_Category).setValue(data);
                ballot.child(KUEB.CONTRACT.total_Candidates).setValue(0);
                ArrayList<CANDIDATE> candidates = new ArrayList<>();
                ballots.add(new BALLOT(data, candidates));
                adapter.notifyDataSetChanged();
                addPosition.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void backToAddScreen(PostionsAdapter adapter, ArrayList<CANDIDATE> candidates) {
        adapter.notifyDataSetChanged();
        layout_Positions.startAnimation(slide_FromRight);
        layout_Positions.setVisibility(View.VISIBLE);
        layout_Candidates.startAnimation(slide_Left);
        layout_Candidates.setVisibility(View.INVISIBLE);
    }

    private void editCandidates(final ArrayList<CANDIDATE> candidates,
                                final String ballot_category, final DatabaseReference category) {
        final TextView position = findViewById(R.id.textView_positionCategory);
        final LinearLayout btn_AddCandidate = findViewById(R.id.Btn_addCandidates);
        final ListView listView = findViewById(R.id.ListView_Candidates);
        final CandidatesAdapter adapter = new CandidatesAdapter(Home.this, R.layout.activity_home, candidates);


        layout_Positions.startAnimation(slide_Right);
        layout_Positions.setVisibility(View.INVISIBLE);
        layout_Candidates.startAnimation(fadeIn);
        layout_Candidates.startAnimation(slide_FromLeft);
        layout_Candidates.setVisibility(View.VISIBLE);

        listView.setAdapter(adapter);
        btn_AddCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCandidate(category, candidates, adapter, ballot_category);
            }
        });
        position.setText("Candidates for the position of " + ballot_category);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                removeCandidateItem(candidates.get(position), candidates, adapter, category);
            }
        });
    }

    private void newCandidate(final DatabaseReference category,
                              final ArrayList<CANDIDATE> candidates, final CandidatesAdapter adapter, String categoryS) {
        this.category = categoryS;
        layout_Candidates.startAnimation(slide_Right);
        layout_Candidates.setVisibility(View.GONE);
        addCandidate.startAnimation(slide_FromLeft);
        addCandidate.setVisibility(View.VISIBLE);


        back_FromCandidate = addCandidate.findViewById(R.id.Back_from_edit_Candidate);
        back_FromCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFromCandidate();
            }
        });
        final CardView start = addCandidate.findViewById(R.id.Welcome_Card);
        final CardView picture = addCandidate.findViewById(R.id.Take_photo);
        final CardView confrim = addCandidate.findViewById(R.id.Confirm_Screen);

        start.setVisibility(View.VISIBLE);
        picture.setVisibility(View.GONE);
        confrim.setVisibility(View.GONE);

        candidatesName = start.findViewById(R.id.editText_Name);
        btn_ProceedFromName = start.findViewById(R.id.Btn_proceedFromName);

        btn_ProceedFromName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allSet) {
                    confrimCandidateData(category, candidates, adapter, start, confrim, picture, start);
                } else {
                    setCandidatePhoto(category, candidates, adapter, picture, start, confrim);
                }
            }
        });

    }

    private void backFromCandidate() {
        resetAddCandidate();
        savingCandidate.dismiss();
        addCandidate.startAnimation(slide_Left);
        addCandidate.setVisibility(View.GONE);
        layout_Candidates.startAnimation(slide_FromRight);
        layout_Candidates.setVisibility(View.VISIBLE);
    }

    private void resetAddCandidate() {
        candidatesName.setText("");
        allSet = false;
        selectedImage = null;

    }

    private void setCandidatePhoto(final DatabaseReference category,
                                   final ArrayList<CANDIDATE> candidates, final CandidatesAdapter adapter,
                                   final CardView picture, final CardView start, final CardView confirm) {
        start.startAnimation(slide_Right);
        start.setVisibility(View.GONE);
        picture.startAnimation(slide_FromLeft);
        picture.setVisibility(View.VISIBLE);
        CardView btn_AddImage = picture.findViewById(R.id.Btn_AddImage);
        final CircleImageView btn = picture.findViewById(R.id.Candidates_ImageButton);
        final CircleImageView candidatesImage = picture.findViewById(R.id.Candidates_ImageRecieved);
        btn.setVisibility(View.VISIBLE);
        candidatesImage.setVisibility(View.INVISIBLE);
        final PickImageDialog pickImageDialog = PickImageDialog.build(new PickSetup())
                .setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult pickResult) {
                        if (pickResult.getError() == null) {
                            selectedImage = pickResult.getUri();
                            btn.setVisibility(View.INVISIBLE);
                            candidatesImage.setVisibility(View.VISIBLE);
                            Glide.with(Home.this).load(selectedImage).into(candidatesImage);
                        }
                    }
                });
        btn_AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageDialog.show(Home.this);
            }
        });
        btn_ProceedFromPicture = picture.findViewById(R.id.Btn_proceedImage);
        btn_ProceedFromPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confrimCandidateData(category, candidates, adapter, picture, confirm, picture, start);
            }
        });
    }

    private void confrimCandidateData(final DatabaseReference category,
                                      final ArrayList<CANDIDATE> candidates, final CandidatesAdapter adapter, CardView view,
                                      final CardView confirm, final CardView picture, final CardView start) {
        allSet = true;
        view.startAnimation(slide_Right);
        view.setVisibility(View.GONE);
        confirm.startAnimation(slide_FromLeft);
        confirm.setVisibility(View.VISIBLE);

        CircleImageView imageView = confirm.findViewById(R.id.Candidates_Image);
        LinearLayout btnName = confirm.findViewById(R.id.Btn_CandidatesName);
        LinearLayout btnAdd = confirm.findViewById(R.id.Btn_addCandidate);

        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCandidatesName(start, confirm);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCandidatesImage(picture, confirm);
            }
        });
        final TextView name = confirm.findViewById(R.id.textView_CandidatesName);
        name.setText(candidatesName.getText().toString());

        Glide.with(Home.this).load(selectedImage).into(imageView);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savingCandidate.show();
                Uri upload = selectedImage;
                CANDIDATE newCandidate = new CANDIDATE(candidatesName.getText().toString(), getCandidatesId(candidates.size(), candidatesName.getText().toString()), "");
                saveCandidate(upload, newCandidate, adapter, category, candidates);
            }
        });
    }

    private void saveCandidate(final Uri upload, final CANDIDATE newCandidate,
                               final CandidatesAdapter adapter, final DatabaseReference category,
                               final ArrayList<CANDIDATE> candidates) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(KUEB.CONTRACT.ELECTIONS).child(getElectionYear()).child(getElectionId());
        final StorageReference image = storageReference.child(KUEB.CONTRACT.Ballot).child(getBallotId()).child(KUEB.CONTRACT.Candidates).child("" + candidates.size());
        final UploadTask uploadTask = image.putFile(upload);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                return image.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri taskResult = task.getResult();
                    DatabaseReference candidate = category.child(KUEB.CONTRACT.Candidates).child("" + candidates.size());
                    candidate.child(KUEB.CONTRACT.candidates_Photo).setValue(taskResult.toString());
                    candidate.child(KUEB.CONTRACT.candidates_Name).setValue(newCandidate.getCandidates_Name());
                    candidate.child(KUEB.CONTRACT.candidates_ID).setValue(newCandidate.getCandidates_ID());
                    candidates.add(new CANDIDATE(newCandidate.getCandidates_Name(), newCandidate.getCandidates_ID(), taskResult.toString()));
                    adapter.notifyDataSetChanged();
                    backFromCandidate();
                }
            }
        });
    }

    private String getCandidatesId(int size, String s) {
        return getCategory() + "_" + size + "_" + s;
    }

    private void changeCandidatesImage(CardView picture, CardView confirm) {
        confirm.startAnimation(slide_Left);
        confirm.setVisibility(View.GONE);
        confirm.startAnimation(slide_FromRight);
        confirm.setVisibility(View.VISIBLE);
    }

    private void changeCandidatesName(final CardView start, final CardView confirm) {
        confirm.startAnimation(slide_Left);
        confirm.setVisibility(View.GONE);
        start.startAnimation(slide_FromRight);
        start.setVisibility(View.VISIBLE);
    }

    private void removeCandidateItem(final CANDIDATE candidate,
                                     final ArrayList<CANDIDATE> candidates, final CandidatesAdapter adapter, final DatabaseReference category) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        candidates.remove(candidate);
                        category.child(KUEB.CONTRACT.Candidates).setValue("");
                        category.child(KUEB.CONTRACT.Candidates).setValue(candidates);

                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setMessage("By clicking proceed, you will remove " + candidate.getCandidates_Name() + " from this elections ballot paper").setPositiveButton("Cancel", dialogClickListener)
                .setNegativeButton("Proceed", dialogClickListener).show();

    }

    private void setDateTime(String yearValue, String dateValue, String timeValue, String
            durationValue, final String caller) {
        final CardView settingsLayout = findViewById(R.id.Layout_dateTimeSettings);
        final CardView dateLayout = findViewById(R.id.Layout_date);
        final CardView timeLayout = findViewById(R.id.Layout_time);
        final CardView countdown = findViewById(R.id.Layout_countDown);
        final TextView date = findViewById(R.id.textView_Date);
        final TextView year = findViewById(R.id.textView_ElectionYear);
        final TextView time = findViewById(R.id.textView_time);
        final DatePicker datePicker = findViewById(R.id.DatePicker);
        final TimePicker timePicker = findViewById(R.id.TimePicker);
        final EditText duration = findViewById(R.id.editText_Duration);
        final LinearLayout cancelEelction = findViewById(R.id.Btn_cancelElection);
        final LinearLayout saveChanges = findViewById(R.id.Btn_saveChanges);
        final CardView btnDate = findViewById(R.id.Btn_Change_Date);
        final RelativeLayout backfromDate = findViewById(R.id.Back_from_date);
        final CardView btnTime = findViewById(R.id.Btn_ChangeTime);
        final RelativeLayout backFromTime = findViewById(R.id.Back_from_time);
        final RelativeLayout backfromTimeSettings = findViewById(R.id.Back_from_timeSettings);
        final TextView countDownTextView = findViewById(R.id.countDownTimerTextView);


        dashBoard.setVisibility(View.GONE);
        dashBoard.startAnimation(slide_Left);

        if (caller.equals(ADD_SCREEN)) {
            welcomeScreen.setVisibility(View.GONE);
            dateTimeSettings.startAnimation(slide_FromRight);
        } else if (caller.equals(CREATE_ELECTION)) {
            welcomeScreen.setVisibility(View.GONE);
            welcomeScreen.startAnimation(fadeOut);
            welcomeScreen.startAnimation(slide_Right);
            dateTimeSettings.startAnimation(slide_FromLeft);
            backfromTimeSettings.setVisibility(View.INVISIBLE);
            cancelEelction.setVisibility(View.GONE);
        } else if (caller.equals(on_ELECTION_DATE)) {
            welcomeScreen.setVisibility(View.GONE);
            dateTimeSettings.startAnimation(slide_FromRight);
            btnDate.setEnabled(false);
            backfromTimeSettings.setVisibility(View.INVISIBLE);
        } else if (caller.equals(on_ELECTION_TIME)) {
            welcomeScreen.setVisibility(View.GONE);
            countdown.setVisibility(View.VISIBLE);
            settingsLayout.setVisibility(View.GONE);
            backfromTimeSettings.setVisibility(View.GONE);
            String format = "hh:mm a";
            DateFormat dateFormat = new SimpleDateFormat(format);
            try {
                Date c = dateFormat.parse(getDeadline_Start());
                Date s = dateFormat.parse(getDeadLine_Stop());
                int milliseconds = (int) (s.getTime() - c.getTime());
                Log.i(TAG, "setDateTime: " + milliseconds);
                CountDownTimer countDownTimer = new CountDownTimer(milliseconds, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        long totalSecs = millisUntilFinished / 1000;
                        long hours = (totalSecs / 3600);
                        long mins = (totalSecs / 60) % 60;
                        long secs = totalSecs % 60;

                        countDownTextView.setText(String.format("%02d:%02d:%02d",
                                hours,
                                mins,
                                secs));

                    }

                    @Override
                    public void onFinish() {
                        elections.child(getElectionYear()).child(getElectionId()).child(KUEB.CONTRACT.election_Status).setValue(KUEB.CONTRACT.FINISHED);
                        refreshActivity();
                    }
                };
                countDownTimer.start();

            } catch (ParseException e) {
                e.printStackTrace();
            }


            Log.i(TAG, "setDateTime: " + timer);
        }

        dateTimeSettings.setVisibility(View.VISIBLE);

        if (yearValue.equals("")) {
            year.setText("" + datePicker.getYear());
            date.setText("");
            time.setText("");
            saveChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createNewElection(
                            year.getText().toString(),
                            date.getText().toString(),
                            time.getText().toString(),
                            duration.getText().toString());
                }
            });
        } else {
            year.setText(getElectionYear());
            date.setText(getElectionDate());
            time.setText(getElectionTime());
            duration.setText(getElectionDuration());
            saveChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!caller.equals(on_ELECTION_DATE)) {
                        saveTimeChanges(
                                year.getText().toString(),
                                date.getText().toString(),
                                time.getText().toString(),
                                duration.getText().toString()
                        );
                    } else {
                        onlySaveChanges(year.getText().toString(),
                                date.getText().toString(),
                                time.getText().toString(),
                                duration.getText().toString());
                        refreshActivity();
                    }

                }
            });
        }

        backfromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateSet(dateLayout, settingsLayout, date, datePicker);
            }
        });

        backFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeSet(timeLayout, settingsLayout, time, timePicker);
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(timeLayout, settingsLayout);
            }
        });
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(dateLayout, settingsLayout);
            }
        });
        dateLayout.setOnTouchListener(new SwipeListener(Home.this) {
            @Override
            public void onRightToLeftSwipe() {

            }

            @Override
            public void onLeftToRightSwipe() {
                dateSet(dateLayout, settingsLayout, date, datePicker);
            }

            @Override
            public void onTopToBottomSwipe() {
            }

            @Override
            public void onBottomToTopSwipe() {
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return super.onTouch(v, event);
            }
        });
        timeLayout.setOnTouchListener(new SwipeListener(Home.this) {
            @Override
            public void onRightToLeftSwipe() {
            }

            @Override
            public void onLeftToRightSwipe() {
                timeSet(timeLayout, settingsLayout, time, timePicker);
            }

            @Override
            public void onTopToBottomSwipe() {
            }

            @Override
            public void onBottomToTopSwipe() {
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return super.onTouch(v, event);
            }
        });
        backfromTimeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dashBoard.setVisibility(View.VISIBLE);
                dashBoard.startAnimation(slide_FromLeft);
                dateTimeSettings.startAnimation(slide_Right);
                dateTimeSettings.setVisibility(View.GONE);
            }
        });
    }

    private void onlySaveChanges(String year, String date, String time, String duration) {
        if (!year.equals("") && !date.equals("") && !time.equals("") && !duration.equals("")) {
            DatabaseReference elect = elections.child(getElectionYear()).child(getElectionId());
            elect.child(KUEB.CONTRACT.election_Date).setValue(date);
            elect.child(KUEB.CONTRACT.election_Duration).setValue(duration);
            elect.child(KUEB.CONTRACT.election_Time).setValue(time);
            setElectionDate(date);
            setElectionTime(time);
            setElectionDuration(duration);
        } else {
            Toast.makeText(Home.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewElection(final String year, final String date, final String time,
                                   final String duartion) {
        creatingElection.show();
        elections.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(year)) {
                    String id = String.valueOf((int) dataSnapshot.child(year).getChildrenCount());
                    Log.i(TAG, "createNewElection: ");
                    setElectionYear(year);
                    setElectionId(id);
                    DatabaseReference election = dataSnapshot.child(year).child(id).getRef();
                    election.child(KUEB.CONTRACT.election_Status).setValue(KUEB.CONTRACT.PREPARING);
                    election.child(KUEB.CONTRACT.election_Date).setValue(date);
                    election.child(KUEB.CONTRACT.election_Time).setValue(time);
                    election.child(KUEB.CONTRACT.election_Duration).setValue(duartion);
                    beginRegistration(election);
                } else {
                    String id = "0";
                    Log.i(TAG, "createNewElection: ");
                    setElectionYear(year);
                    setElectionId(id);
                    DatabaseReference election = dataSnapshot.child(year).child(id).getRef();
                    election.child(KUEB.CONTRACT.election_Status).setValue(KUEB.CONTRACT.PREPARING);
                    election.child(KUEB.CONTRACT.election_Date).setValue(date);
                    election.child(KUEB.CONTRACT.election_Time).setValue(time);
                    election.child(KUEB.CONTRACT.election_Duration).setValue(duartion);
                    beginRegistration(election);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void beginRegistration(final DatabaseReference election) {
        final ArrayList<String> positions = new ArrayList<>();
        kuso_Positions.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot pos : dataSnapshot.getChildren()) {
                    String Id = pos.getKey();
                    DataSnapshot position = dataSnapshot.child(Id);
                    String category = position.child(KUEB.CONTRACT.position).getValue(String.class);
                    positions.add(category);
                }
                createBallots(positions, election);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createBallots(ArrayList<String> positions, final DatabaseReference election) {
        final ArrayList<BALLOT> ballots = new ArrayList<>();
        for (String category : positions) {
            ArrayList<CANDIDATE> candidates = new ArrayList<>();
            ballots.add(new BALLOT(category, candidates));
        }

        election.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                election.child(KUEB.CONTRACT.Ballot).setValue(ballots);
                loadElection(ballots, election, NEW_ELECTION);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveTimeChanges(String year, String date, String time, String duration) {
        if (!year.equals("") && !date.equals("") && !time.equals("") && !duration.equals("")) {
            dashBoard.setVisibility(View.VISIBLE);
            dashBoard.startAnimation(slide_FromLeft);
            dateTimeSettings.startAnimation(slide_Right);
            dateTimeSettings.setVisibility(View.GONE);
            DatabaseReference elect = elections.child(getElectionYear()).child(getElectionId());
            elect.child(KUEB.CONTRACT.election_Date).setValue(date);
            elect.child(KUEB.CONTRACT.election_Duration).setValue(duration);
            elect.child(KUEB.CONTRACT.election_Time).setValue(time);
            setElectionDate(date);
            setElectionTime(time);
            setElectionDuration(duration);
        } else {
            Toast.makeText(Home.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
        }

    }

    private String getDate(DatePicker datePicker) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, EEE");
        return dateFormat.format(KUEB.getDateFromDatePicker(datePicker));
    }

    private void dateSet(CardView dateLayout, CardView settingsLayout, TextView
            date, DatePicker datePicker) {
        settingsLayout.startAnimation(slide_FromRight);
        settingsLayout.setVisibility(View.VISIBLE);
        dateLayout.startAnimation(slide_Left);
        dateLayout.setVisibility(View.INVISIBLE);
        date.setText(getDate(datePicker));

    }

    private void setDate(CardView dateLayout, CardView settingsLayout) {
        settingsLayout.startAnimation(slide_Right);
        settingsLayout.setVisibility(View.INVISIBLE);
        dateLayout.startAnimation(slide_FromLeft);
        dateLayout.setVisibility(View.VISIBLE);

    }

    private void timeSet(CardView dateLayout, CardView settingsLayout, TextView
            time, TimePicker timePicker) {
        settingsLayout.startAnimation(slide_FromRight);
        settingsLayout.setVisibility(View.VISIBLE);
        dateLayout.startAnimation(slide_Left);
        dateLayout.setVisibility(View.INVISIBLE);
        time.setText(KUEB.getTimefromTimePicker(timePicker));

    }

    private void setTime(CardView dateLayout, CardView settingsLayout) {
        settingsLayout.startAnimation(slide_Right);
        settingsLayout.setVisibility(View.INVISIBLE);
        dateLayout.startAnimation(slide_FromLeft);
        dateLayout.setVisibility(View.VISIBLE);

    }

    public String getCategory() {
        return category;
    }

    private void refreshActivity() {
        finish();
        startActivity(new Intent(Home.this, Home.class));
    }
}
