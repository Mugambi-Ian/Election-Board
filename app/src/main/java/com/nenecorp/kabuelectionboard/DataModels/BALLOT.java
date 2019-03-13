package com.nenecorp.kabuelectionboard.DataModels;

import java.util.ArrayList;

public class BALLOT {
    private String ballot_Category;
    private ArrayList<CANDIDATE> candidates = new ArrayList<>();

    public BALLOT() {
    }

    public BALLOT(String ballot_Category, ArrayList<CANDIDATE> candidates) {
        this.ballot_Category = ballot_Category;
        this.candidates = candidates;
    }

    public String getBallot_Category() {
        return ballot_Category;
    }

    public void setBallot_Category(String ballot_Category) {
        this.ballot_Category = ballot_Category;
    }

    public ArrayList<CANDIDATE> getCandidates() {
        return candidates;
    }

    public void setCandidates(ArrayList<CANDIDATE> candidates) {
        this.candidates = candidates;
    }
}
