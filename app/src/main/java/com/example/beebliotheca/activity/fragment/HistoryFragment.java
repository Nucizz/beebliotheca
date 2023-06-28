package com.example.beebliotheca.activity.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.beebliotheca.adapter.BookAdapter;
import com.example.beebliotheca.databinding.FragmentHistoryBinding;
import com.example.beebliotheca.object.Book;
import com.example.beebliotheca.object.Favorite;
import com.example.beebliotheca.object.History;
import com.example.beebliotheca.object.User;
import com.example.beebliotheca.object.accessTimeComparator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private ArrayList<History> historyList;
    private ArrayList<Book> historyBookList;

//    private static BookAdapter HistoryAdapter;
    FirebaseFirestore db;
    Integer curUserId;
    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(getLayoutInflater());

        db = FirebaseFirestore.getInstance();
        historyList = new ArrayList<>(); //DB
        historyBookList = new ArrayList<>();
        curUserId = User.Current.get().getIdNum();

        binding.historyListError.setVisibility(View.VISIBLE);
        binding.historyListError.setText("Load History...");

        getHistoryDB();

        ViewGroup.LayoutParams layoutParams = binding.historyListError.getLayoutParams();
        layoutParams.height = getHeight();
        binding.historyListError.setLayoutParams(layoutParams);

        return binding.getRoot();
    }



    @Override
    public void onResume() {
        super.onResume();
//        historyList = new ArrayList<>(); //DB
//        historyBookList = new ArrayList<>();
        getHistoryDB();
    }

    private void getHistoryDB(){
        db.collection("History").whereEqualTo("userId", curUserId).get().addOnCompleteListener(
                task -> {
                    historyList = new ArrayList<>();
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            String curBookId = doc.getString("bookId").toString();
                            String curHistoryId = doc.getString("historyId").toString();
                            String curAccessTime = doc.getString("accessTime").toString();

                            History history = new History(curHistoryId, curBookId, curUserId, curAccessTime);
                            historyList.add(history);
                        }
                    }
                }
        ).continueWith(
                task -> {

                    ArrayList<Book> separateBookList = new ArrayList<>();

                    db.collection("Book").get().addOnCompleteListener(
                            task1 -> {
                                if(task1.isSuccessful()){
                                    for (QueryDocumentSnapshot doc: task1.getResult()){
                                        String curId = doc.getString("id").toString();
                                        Integer curCategoryId = Integer.parseInt(doc.get("categoryId").toString());
                                        String curTitle = doc.getString("title");
                                        String curIsbn = doc.getString("isbn");
                                        String curAuthor = doc.getString("author");
                                        String curDescription = doc.getString("description");
                                        String curImageURL = doc.getString("imageURL");
                                        String curDocumentURL = doc.getString("documentURL");
                                        Book curBook = new Book(curId, curCategoryId, curTitle, curIsbn, curAuthor, curDescription, curImageURL, curDocumentURL);
                                        separateBookList.add(curBook);
                                    }
                                }
                            }
                    ).continueWith(
                            task1 -> {
                                historyBookList = new ArrayList<>();
                                if(!Book.CurrentBookList.getList().isEmpty()){
//                        Log.i("intoFunction", "Current Book List Detected");
                                    Log.i("intoFunction", String.valueOf(historyList.size()));
                                    Collections.sort(historyList, new accessTimeComparator());
                                    for (int i = 0; i < historyList.size(); i++) {
                                        String curBookId = historyList.get(i).getBookId();
                                        Book curHistoryBook = separateBookList.stream().filter(book -> book.getId().equals(curBookId)).findFirst().orElse(null);
                                        if(curHistoryBook != null){
                                            curHistoryBook.setDescription(curHistoryBook.getDescription() + "\nLast accessed:" + historyList.get(i).getAccessTime() + " WIB");
                                            historyBookList.add(curHistoryBook);
                                        }
                                        //opsi kalo gaada di current book list yg ada di database (diakses pas search)
                                        //atur di search home fragment/book adapter
                                    }
                                    //sorting
                                }

                                setHistoryFragmentView();

                                return null;
                            }
                    );
                    return null;
                }
        );
    }

    private void setHistoryFragmentView(){
        if(!historyBookList.isEmpty()) {
            binding.historyList.setVisibility(View.VISIBLE);
            binding.historyListError.setVisibility(View.GONE);
            BookAdapter historyAdapter = new BookAdapter(requireActivity(), historyBookList);
            binding.historyList.setAdapter(historyAdapter);
            binding.historyList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        } else {
            binding.historyList.setVisibility(View.GONE);
            binding.historyListError.setVisibility(View.VISIBLE);
            binding.historyListError.setText("Your finished books will appear here.");
        }
    }

    private int getHeight() {
        WindowManager windowManager = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int space = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                180.0f,
                getResources().getDisplayMetrics()
        );

        return displayMetrics.heightPixels - space;
    }
}