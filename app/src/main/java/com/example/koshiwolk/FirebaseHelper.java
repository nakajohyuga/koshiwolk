package com.example.koshiwolk;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private DatabaseReference mDatabase;

    public FirebaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void getUserData(String userId, FirebaseDataListener listener) {
        mDatabase.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        listener.onDataReceived(dataSnapshot);
                    }
                });
    }

    public interface FirebaseDataListener {
        void onDataReceived(DataSnapshot dataSnapshot);
    }
}
