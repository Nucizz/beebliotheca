package com.example.beebliotheca.activity.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.beebliotheca.R;
import com.example.beebliotheca.activity.CategoryDetailsActivity;
import com.example.beebliotheca.adapter.BookAdapter;
import com.example.beebliotheca.databinding.FragmentHomeBinding;
import com.example.beebliotheca.object.Book;
import com.google.common.collect.Lists;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ArrayList<Book> latestCollectionList = new ArrayList<>();
    private ArrayList<Book> searchResultsList = new ArrayList<>();
    private BookAdapter searchAdapter;

    FirebaseFirestore db;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        int width = getWidth();
        setWidth(binding.cat1, width);
        setWidth(binding.cat2, width);
        setWidth(binding.cat3, width);
        setWidth(binding.cat4, width);

        binding.cat1.setOnClickListener(n -> {
            Intent details1 = new Intent(requireActivity(), CategoryDetailsActivity.class);
            details1.putExtra("id", 1);
            details1.putExtra("name", "Computer");
            startActivity(details1);
        });

        binding.cat2.setOnClickListener(n -> {
            Intent details2 = new Intent(requireActivity(), CategoryDetailsActivity.class);
            details2.putExtra("id", 2);
            details2.putExtra("name", "History");
            startActivity(details2);
        });

        binding.cat3.setOnClickListener(n -> {
            Intent details3 = new Intent(requireActivity(), CategoryDetailsActivity.class);
            details3.putExtra("id", 3);
            details3.putExtra("name", "Psychology");
            startActivity(details3);
        });

        /*
        Category 4 biarin aja deh wkwkwk, lelah bang
        cuma protoype juga kita ga punya data sebanyak itu wkwkwk
        iyain dee
        */

        binding.cat4.setOnClickListener(n -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle("Sorry!");
            builder.setMessage("More categories are unnecessary at current state, " +
                    "Please use the search bar for specific books. " +
                    "We will continue to expand and add more. Please stay tuned!");
            builder.setPositiveButton("I Understand", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        binding.latestCollectionError.setVisibility(View.VISIBLE);
        binding.latestCollectionError.setText("Loading data...");

        db = FirebaseFirestore.getInstance();
//        latestCollectionList = ; //DB

        if(latestCollectionList.isEmpty()){
            getLatestCollectionDb();
        }

        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        return binding.getRoot();
    }

    private void getLatestCollectionDb(){
        db.collection("Book").get().addOnCompleteListener(
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
                            latestCollectionList.add(curBook);
//                            Log.d("getDbbook", curId);
                        }
                    }
                }
        ).continueWith(
                task -> {
                    if(!latestCollectionList.isEmpty()) {
                        binding.latestCollection.setVisibility(View.VISIBLE);
                        binding.latestCollectionError.setVisibility(View.GONE);
                        //get small amount of collection
                        ArrayList<Book> latestCollectionSubList = new ArrayList<>();
                        for (int i = 0; i < 15; i++) {
                            latestCollectionSubList.add(latestCollectionList.get(i));
                        }
                        BookAdapter bookAdapter = new BookAdapter(requireActivity(), latestCollectionSubList);
                        binding.latestCollection.setAdapter(bookAdapter);
                        binding.latestCollection.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    } else {
                        binding.latestCollection.setVisibility(View.GONE);
                        binding.latestCollectionError.setVisibility(View.VISIBLE);
                        binding.latestCollectionError.setText("Latest collections books will appear here.");
                    }

                    Book.CurrentBookList.set(latestCollectionList);
                    searchAdapter = new BookAdapter(getActivity(), searchResultsList);
                    binding.searchResults.setLayoutManager(new LinearLayoutManager(requireActivity()));
                    binding.searchResults.setAdapter(searchAdapter);
                    return null;
                }
        );
    }

    private void performSearch(String query) {
        if (TextUtils.isEmpty(query)) {
            binding.defaultView.setVisibility(View.VISIBLE);
            binding.searchResults.setVisibility(View.GONE);
        } else {
            binding.defaultView.setVisibility(View.GONE);
            binding.searchResults.setVisibility(View.VISIBLE);

            // Cara ambil query => binding.searchBar.getQuery().toString()
            String API_query = binding.searchBar.getQuery().toString();
            API_query.replace(" ", "%20");
            String API_Url = "https://api.springernature.com/meta/v2/json?q=title:%22" + API_query + "%22%20openaccess:true&p=20&api_key=9fbe316362e3d85ddac6dba4504acfa8";
            Log.i("insertSearch", API_Url);
            getSearchJSONDetails(API_Url);

            //reset list
            searchResultsList = new ArrayList<>(); // DB
        }
    }

    private void getSearchJSONDetails(String API_Url){
        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(API_Url,
                response -> {
                    try {
                        Log.i("insertObjectReq", "response try");
                        JSONArray responseArray = response.getJSONArray("records");

                        for (int i = 0; i < responseArray.length(); i++) {

                            JSONObject jsonObject = responseArray.getJSONObject(i);

                            String id;
                            int categoryId;
                            String title;
                            String isbn = "";
                            String author = "";
                            String description;
                            String imageURL = "";
                            String documentURL;

                            id = jsonObject.getString("identifier");

                            JSONArray subjectArray = jsonObject.getJSONArray("subjects");
                            String subjectObject = subjectArray.getString(0);
                            if (subjectObject.equals("Computer Science")) {
                                categoryId = 1;
                            } else if (subjectObject.equals("History")) {
                                categoryId = 2;
                            } else if (subjectObject.equals("Psychology")) {
                                categoryId = 3;
                            } else {
                                categoryId = 4;
                            }

                            title = jsonObject.getString("title");

                            if(jsonObject.optJSONObject("isbn") != null){
                                //only books have isbn
                                isbn = jsonObject.getString("isbn");
                            }

                            if (jsonObject.optJSONArray("creators") != null) {
                                JSONArray creatorArray = jsonObject.getJSONArray("creators");
                                JSONObject curCreatorObject = creatorArray.getJSONObject(0);
                                String firstAuthor = curCreatorObject.getString("creator");
                                if (creatorArray.length() == 1) {
                                    author = firstAuthor;
                                } else if (creatorArray.length() == 2) {
                                    JSONObject curCreatorObject2 = creatorArray.getJSONObject(1);
                                    author = firstAuthor + " & " + curCreatorObject2.getString("creator");
                                } else {
                                    //more than 2
                                    author = firstAuthor + ", et al.";
                                }
                            } else {
                                author = "N/A";
                            }

                            description = jsonObject.getString("publicationType");

                            JSONArray urlArray = jsonObject.getJSONArray("url");
                            JSONObject objectUrl = urlArray.getJSONObject(2); //source URL
                            documentURL = objectUrl.getString("value");

                            Book book = new Book(id, categoryId, title, isbn, author, description, imageURL, documentURL);

                            Log.i("insertDataTitle", title);
                            searchResultsList.add(book);

                        }
                        searchAdapter.setBookList(searchResultsList);
                        searchAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Log.i("insertErrorAPI", e.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("insertErrorResponse", error.toString());
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }


    private void setWidth(View widget, int width) {
        ViewGroup.LayoutParams layoutParams = widget.getLayoutParams();
        layoutParams.width = width;
        widget.setLayoutParams(layoutParams);
    }

    private int getWidth() {
        WindowManager windowManager = (WindowManager) requireActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int space = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                70.0f,
                getResources().getDisplayMetrics()
        );

        return (displayMetrics.widthPixels - space) / 2;
    }

}