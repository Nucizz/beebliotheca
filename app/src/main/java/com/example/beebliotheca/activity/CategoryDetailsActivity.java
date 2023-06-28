package com.example.beebliotheca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.beebliotheca.adapter.BookAdapter;
import com.example.beebliotheca.databinding.ActivityCategoryDetailsBinding;
import com.example.beebliotheca.object.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CategoryDetailsActivity extends Activity {

    private ActivityCategoryDetailsBinding binding;
    private ArrayList<Book> bookList;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewGroup.LayoutParams layoutParams = binding.bookListError.getLayoutParams();
        layoutParams.height = getHeight();
        binding.bookListError.setLayoutParams(layoutParams);

        int categoryId = getIntent().getIntExtra("id", -1);
        String category = getIntent().getStringExtra("name");

        binding.backButton.setOnClickListener(n -> finish());
        binding.layoutName.setText(category);

        binding.bookListError.setVisibility(View.VISIBLE);
        binding.bookListError.setText("Loading data...");

        db = FirebaseFirestore.getInstance();
        bookList = new ArrayList<>(); //DB
        db.collection("Book").whereEqualTo("categoryId", categoryId).get().addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            String curId = doc.getString("id").toString();
                            Integer curCategoryId = Integer.parseInt(doc.get("categoryId").toString());
                            String curTitle = doc.getString("title");
                            String curIsbn = doc.getString("isbn");
                            String curAuthor = doc.getString("author");
                            String curDescription = doc.getString("description");
                            String curImageURL = doc.getString("imageURL");
                            String curDocumentURL = doc.getString("documentURL");
                            Book curBook = new Book(curId, curCategoryId, curTitle, curIsbn, curAuthor, curDescription, curImageURL, curDocumentURL);
                            bookList.add(curBook);
//                            Log.d("getDbbook", curId);
                        }
                    }
                }
        ).continueWith(
                task -> {
                    if(!bookList.isEmpty()) {
                        binding.bookList.setVisibility(View.VISIBLE);
                        binding.bookListError.setVisibility(View.GONE);
                        BookAdapter savedAdapter = new BookAdapter(this, bookList);
                        binding.bookList.setAdapter(savedAdapter);
                        binding.bookList.setLayoutManager(new LinearLayoutManager(this));
                    } else {
                        binding.bookList.setVisibility(View.GONE);
                        binding.bookListError.setVisibility(View.VISIBLE);
                        binding.bookListError.setText("No book listed in this category.");
                    }


                    return null;
                }
        );

    }

    private int getHeight() {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.heightPixels;
    }
}