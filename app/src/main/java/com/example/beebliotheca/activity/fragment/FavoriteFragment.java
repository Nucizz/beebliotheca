package com.example.beebliotheca.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.beebliotheca.databinding.FragmentFavoriteBinding;
import com.example.beebliotheca.object.Book;
import com.example.beebliotheca.object.Favorite;
import com.example.beebliotheca.object.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Iterator;

public class FavoriteFragment extends Fragment {

    private FragmentFavoriteBinding binding;
    private static ArrayList<Favorite> favoriteList;
    private static ArrayList<Book> favoriteListBook;
    private static BookAdapter favoriteAdapter;
    FirebaseFirestore db;
    public FavoriteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(getLayoutInflater());

        db = FirebaseFirestore.getInstance();
        favoriteList = new ArrayList<>(); //DB
        favoriteListBook = new ArrayList<>();


        binding.likedError.setVisibility(View.VISIBLE);
        binding.likedError.setText("Loading Favorite... ");


        ViewGroup.LayoutParams layoutParams = binding.likedError.getLayoutParams();
        layoutParams.height = getHeight();
        binding.likedError.setLayoutParams(layoutParams);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFavoriteData();
    }

    @Override
    public void onResume() {
        super.onResume();
        setFavoriteData();
    }

    private void setFavoriteData(){
        Integer curUserId = User.Current.get().getIdNum();
        db.collection("Favorite").whereEqualTo("userId", curUserId).get().addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        favoriteList = new ArrayList<>(); //supaya data ga duplicate
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            String curFavoriteId = doc.getString("favoriteId").toString();
                            String curBookId = doc.getString("bookId").toString();
                            Favorite favorite = new Favorite(curFavoriteId, curBookId, curUserId);
                            favoriteList.add(favorite);
                        }
                    }
                }
        ).continueWith(
                task -> {
                    //pindahin data book ke favorite list
                    favoriteListBook = new ArrayList<>();
                    if(!Book.CurrentBookList.getList().isEmpty()){
                        Log.i("intoFunction", "Current Book List Detected");
                        Log.i("intoFunction", String.valueOf(favoriteList.size()));
                        for (int i = 0; i < favoriteList.size(); i++) {
                            Book curFavoriteBook = Book.CurrentBookList.getBook(favoriteList.get(i).getBookId());
                            if(curFavoriteBook != null){
                                favoriteListBook.add(curFavoriteBook);
                            }
                            //opsi kalo gaada di current book list yg ada di database (diakses pas search)
                        }
                    }

                    //atur recycle view
                    if(!favoriteListBook.isEmpty()) {
                        binding.liked.setVisibility(View.VISIBLE);
                        binding.likedError.setVisibility(View.GONE);
                        favoriteAdapter = new BookAdapter(requireActivity(), favoriteListBook);
                        binding.liked.setAdapter(favoriteAdapter);
                        binding.liked.setLayoutManager(new LinearLayoutManager(requireActivity()));
                        Log.i("intoFunction", "adapter Set");
                    } else {
                        binding.liked.setVisibility(View.GONE);
                        binding.likedError.setVisibility(View.VISIBLE);
                        binding.likedError.setText("Your liked books will appear here.");
                    }
                    return null;
                }
        );
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

    public static void addBook(Book newFavorite) {
        favoriteListBook.add(newFavorite);
        favoriteAdapter.notifyItemRangeChanged(0, favoriteList.size());
    }

    public static void deleteBook(Book delFavorite) {
        int ind = favoriteListBook.indexOf(delFavorite);
        favoriteListBook.remove(delFavorite);
//        favoriteListBook.trimToSize();
        favoriteAdapter.setBookList(favoriteListBook);
        favoriteAdapter.notifyItemRemoved(ind);
//        favoriteAdapter.notifyItemRangeRemoved(ind, 1);
        Log.i("deleteFavoriteData", "deleteBook: " + favoriteListBook.size());
    }
}