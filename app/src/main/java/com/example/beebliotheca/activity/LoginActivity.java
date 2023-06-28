package com.example.beebliotheca.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.beebliotheca.adapter.AnimationFloatAdapter;
import com.example.beebliotheca.databinding.ActivityLoginBinding;
import com.example.beebliotheca.object.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.Vector;

public class LoginActivity extends Activity {

    private ActivityLoginBinding binding;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        binding.background.addView(new AnimationFloatAdapter(this));

        binding.loginButton.setOnClickListener(n ->
                loginHandler(
                        Objects.requireNonNull(binding.studentIDForm.getText()).toString(),
                        Objects.requireNonNull(binding.passwordForm.getText()).toString()
                ));
    }

    boolean userExist = false;
    User curUser;
    private void loginHandler(String studentId, String password){

        if(studentId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all the form!", Toast.LENGTH_SHORT).show();
        }
        else if(!User.Handler.valStudentId(studentId) || !User.Handler.valPassword(User.Handler.hashPassword(password))) {
            Toast.makeText(this, "Invalid account credentials!", Toast.LENGTH_SHORT).show();
        }
        else {
            //DB Validate take ID
//            validateUserDb(studentId, password);
            userExist = false;
            Query query = db.collection("User").whereEqualTo("studentId", studentId);
            query = query.whereEqualTo("password", password);

            query.get().addOnCompleteListener(
                    task -> {
                        if(task.isSuccessful()){
                            if(!task.getResult().isEmpty()){
                                userExist = true;
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                Integer curId = Integer.parseInt(doc.get("id").toString());
                                String curPassword = doc.getString("password").toString();
                                String curEmail = doc.getString("email");
                                String curName = doc.getString("name");
                                curUser = new User(curId, curName, studentId, curEmail, curPassword);
                            }
                        }
                    }
            ).continueWith(
                    task -> {
                        if(userExist) {
//                            User authenticatedUser = new User(0, "admin", "2540115225", "haiyaa@binus.ac.id", "admin"); //From DB get by ID
//                            User.Current.set(authenticatedUser);
                            User.Current.set(curUser);
//                            Log.i("insertCurUser", User.Current.get().getId());
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            Toast.makeText(this, "Invalid account credentials!", Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
            );

        }
    }


}