package com.example.beebliotheca.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.example.beebliotheca.R;
import com.example.beebliotheca.activity.fragment.FavoriteFragment;
import com.example.beebliotheca.adapter.PdfRenderAdapter;
import com.example.beebliotheca.adapter.WebViewClientAdaptor;
import com.example.beebliotheca.databinding.ActivityBookDetailsBinding;
import com.example.beebliotheca.object.Book;
import com.example.beebliotheca.object.Favorite;
import com.example.beebliotheca.object.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BookDetailsActivity extends Activity {

    private ActivityBookDetailsBinding binding;
    private Book book;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        book = getIntent().getParcelableExtra("item", Book.class);

        binding.backButton.setOnClickListener(n -> finish());
        binding.layoutName.setText(book.getTitle());

        db = FirebaseFirestore.getInstance();
        getFavoriteData();

        setUnLikeButton();

        binding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        binding.webView.getSettings().setSupportMultipleWindows(false);
        binding.webView.setWebViewClient(new WebViewClientAdaptor());
        binding.webView.loadUrl(book.getDocumentURL());
//        loadPDF(book.getDocumentURL());
//        loadPDF("http://link.springer.com/openurl/pdf?id=doi:10.1007/s00404-022-06704-z");
    }

    boolean bookFavoriteExist = false;
    private void getFavoriteData(){
        bookFavoriteExist = false;
        User curUser = User.Current.get();
        Log.i("getFavoriteData", String.valueOf(curUser.getStudentId()));
        db.collection("Favorite").whereEqualTo("userId", User.Current.get().getIdNum()).get().addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            String curFavoriteId = doc.getString("favoriteId").toString();
                            String curBookId = doc.getString("bookId").toString();
                            if(curBookId.equals(book.getId())){
                                bookFavoriteExist = true;
                                Favorite curFavorite = new Favorite(curFavoriteId, curBookId, curUser.getIdNum());
                                Favorite.CurrentFavorite.set(curFavorite);
                                break;
                            }
                        }
                    }
                }
        ).continueWith(
            task -> {
                if(bookFavoriteExist) { // Klo like
                    setLikeButton();
                } else { // Klo blm like
                    setUnLikeButton();
                }

                binding.likeButton.setOnClickListener(n -> {
                    if(bookFavoriteExist) { // Klo like
                        FavoriteFragment.deleteBook(book);
                        setUnLikeButton();
                        bookFavoriteExist = false;
                        //DB
                        deleteFavoriteFromDB(Favorite.CurrentFavorite.get());
                    } else { // Klo blm like
//                        FavoriteFragment.addBook(book);
                        setLikeButton();
                        bookFavoriteExist = true;
                        //DB
                        addFavoriteToDB(book.getId());
                    }
                });
                return null;
            }
        );
    }

    private void deleteFavoriteFromDB(Favorite favorite){
        db.collection("Favorite").document(favorite.getFavoriteId()).delete().addOnCompleteListener(
                task -> {
                    Toast.makeText(this , "Deleted from favorite", Toast.LENGTH_SHORT).show();
                    setUnLikeButton();
                }
        );
    }


    String generateFavoriteId;
    private void addFavoriteToDB(String bookId){
        int curUserId = User.Current.get().getIdNum();
//        Log.i("insertFavoriteDB", "inject data");
        Map<String, Object> dataFavorite= new HashMap<>();
        generateFavoriteId = "";
        dataFavorite.put("bookId", bookId);
        dataFavorite.put("userId", curUserId);
        db.collection("Favorite").add(dataFavorite).addOnSuccessListener(
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        generateFavoriteId = documentReference.getId();
                        dataFavorite.put("favoriteId", generateFavoriteId);
                        db.collection("Favorite").document(generateFavoriteId).set(dataFavorite);
                        Toast.makeText(BookDetailsActivity.this, "Added to Favorite", Toast.LENGTH_SHORT).show();
                        setLikeButton();
                    }
                }
        );
    }

    private void setLikeButton(){
        binding.likeButton.clearColorFilter();
        binding.likeButton.setColorFilter(ContextCompat.getColor(this, R.color.error), PorterDuff.Mode.SRC_ATOP);
    }

    private void setUnLikeButton(){
        binding.likeButton.clearColorFilter();
        binding.likeButton.setColorFilter(ContextCompat.getColor(this, R.color.background_high), PorterDuff.Mode.SRC_ATOP);
    }

//    private void loadPDF(String pdfFilePath) {
//        try {
//            File file = new File(pdfFilePath);
//            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
//
//            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
//            PdfRenderer.Page currentPage = pdfRenderer.openPage(0);
//
//            // Create a bitmap to render the PDF page
//            Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
//
//            // Render the PDF page onto the bitmap
//            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//
//            // Display the bitmap in the ImageView
//            binding.document.setImageBitmap(bitmap);
//
//            currentPage.close();
//            pdfRenderer.close();
//            fileDescriptor.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void loadPDFFromUrl(String pdfUrl) {
//        PdfRenderAdapter downloadPdfTask = new PdfRenderAdapter(new PdfRenderAdapter.DownloadPdfListener() {
//            @Override
//            public void onPdfDownloaded(File file) {
//                loadPDF(file.getAbsolutePath());
//            }
//
//            @Override
//            public void onDownloadFailed() {
//                Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        downloadPdfTask.execute(pdfUrl);
//    }
}