package com.example.beebliotheca.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beebliotheca.R;
import com.example.beebliotheca.activity.BookDetailsActivity;
import com.example.beebliotheca.databinding.ItemBookBinding;
import com.example.beebliotheca.object.Book;
import com.example.beebliotheca.object.Favorite;
import com.example.beebliotheca.object.History;
import com.example.beebliotheca.object.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> bookList;
    private Context ctx;
    FirebaseFirestore db;

    public BookAdapter(Context ctx, ArrayList<Book> bookList) {
        this.ctx = ctx;
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemBookBinding binding = ItemBookBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookList.get(position));
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void setBookList(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    boolean historyExist = false;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ItemBookBinding binding;

        public ViewHolder(@NonNull ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Book book) {
            binding.title.setText(book.getTitle());
            binding.author.setText(book.getAuthor());
            binding.description.setText(book.getDescription());
            if(!book.getImageURL().isEmpty()){
                Picasso.get().load(book.getImageURL()).into(binding.image);
            }

            binding.view.setOnClickListener(n -> {
                Intent details = new Intent(ctx, BookDetailsActivity.class);
                details.putExtra("item", book);
                //add new from search API
                addNewBookFromSearch(book);
                //ADD to History DB
                addHistoryDB(book);

                ctx.startActivity(details);
            });
        }

        private void addHistoryDB(Book book){
            int curUserId = User.Current.get().getIdNum();
            Query query = db.collection("History");
            query = query.whereEqualTo("bookId", book.getId());
            query = query.whereEqualTo("userId", curUserId);

            query.get().addOnCompleteListener(
                    task ->{
                        Map<String, Object> dataHistory= new HashMap<>();
                        String curAccessTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        dataHistory.put("bookId", book.getId());
                        dataHistory.put("userId", curUserId);
                        dataHistory.put("accessTime", curAccessTime);

                        if(task.isSuccessful() && !task.getResult().isEmpty()){
                            historyExist = true;
                            //if history exist, update access time
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            String getHistoryId = doc.getString("historyId").toString();
                            dataHistory.put("historyId", getHistoryId);
                            db.collection("History").document(getHistoryId).set(dataHistory);
                        } else if (task.getResult().isEmpty()) {
                            //history don't exist, insert new history
                            db.collection("History").add(dataHistory).addOnSuccessListener(
                                    new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            //get and rewrite auto-generated history id
                                            String curHistoryId = documentReference.getId();
                                            dataHistory.put("historyId", curHistoryId);
                                            db.collection("History").document(curHistoryId).set(dataHistory);
                                        }
                                    }
                            );


                        }
                    }
            );
        }

        private void addNewBookFromSearch(Book book){
            //check if exist in DB book collection
            Query query = db.collection("Book");
            query = query.whereEqualTo("title", book.getTitle());

            query.get().addOnCompleteListener(
                    task -> {
                        //if not exist, add to db, update current list
                        if(task.isSuccessful() && task.getResult().isEmpty()){
                            //add to db
                            Map<String, Object> newSearchBook= new HashMap<>();
                            newSearchBook.put("id", book.getId());
                            newSearchBook.put("categoryId", book.getCategoryId());
                            newSearchBook.put("title", book.getTitle());
                            newSearchBook.put("isbn", book.getIsbn());
                            newSearchBook.put("author", book.getAuthor());
                            newSearchBook.put("description", book.getDescription());
                            newSearchBook.put("imageURL", book.getImageURL());
                            newSearchBook.put("documentURL", book.getDocumentURL());
                            db.collection("Book").add(newSearchBook);


                            //update current book list
                            ArrayList<Book> curBookList = Book.CurrentBookList.getList();
                            curBookList.add(book);
                            Book.CurrentBookList.set(curBookList);
                            notifyDataSetChanged();
                            Log.i("InsertBookDB", "addNewBookFromSearch: " + book.getTitle());
                        }
                        //if already exist, do nothing
            });


        }
    }
}
