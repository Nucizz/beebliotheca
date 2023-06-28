package com.example.beebliotheca.object;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Base64;
import java.util.regex.Pattern;

import kotlin.jvm.Throws;

public class User {
    int id;
    String name;
    String studentId;
    String email;

    String password;

    public User(int id, String name, String studentId, String email, String password) {
        this.id = id;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.password = password;
    }

    public int getIdNum() {
        return id;
    }

    public String getId() {
        return "BN" + id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(String id) {
        Pattern isNumeric = Pattern.compile("\\d+");

        if(isNumeric.matcher(id.substring(2)).matches()){
            this.id = Integer.parseInt(id.substring(2));
        } else if(isNumeric.matcher(id).matches()){
            this.id = Integer.parseInt(id);
        } else {
            throw new IllegalArgumentException("This function accepts BN[Num] or [Num] in string format only!");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class Handler {

        public static boolean valStudentId(String text) {
            Pattern regex = Pattern.compile("\\d{10}");
            return regex.matcher(text).matches();
        }

        public static boolean valPassword(String text) {
            Pattern regex = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$");
            return regex.matcher(text).matches();
        }

        public static String hashPassword(String text) {
            return Base64.getEncoder().encodeToString(text.getBytes());
        }

    }

    public static class Current {
        private static User curUser;

        public static void set(User user) {
            curUser = user;
        }

        public static User get() {
            return curUser;
        }

        public static void logout() {
            curUser = null;
        }

    }

}
