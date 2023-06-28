package com.example.beebliotheca.object;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class Book implements Parcelable {
    private String id;
    private int categoryId;
    private String title;
    private String isbn;
    private String author;
    private String description;
    private String imageURL;
    private String documentURL;

    public Book(String id, int categoryId, String title, String isbn, String author, String description, String imageURL, String documentURL) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.description = description;
        this.imageURL = imageURL;
        this.documentURL = documentURL;
    }

    protected Book(Parcel in) {
        id = in.readString();
        categoryId = in.readInt();
        title = in.readString();
        isbn = in.readString();
        author = in.readString();
        description = in.readString();
        imageURL = in.readString();
        documentURL = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getId() {
        return id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDocumentURL() {
        return documentURL;
    }

    public void setDocumentURL(String documentURL) {
        this.documentURL = documentURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(categoryId);
        dest.writeString(title);
        dest.writeString(isbn);
        dest.writeString(author);
        dest.writeString(description);
        dest.writeString(imageURL);
        dest.writeString(documentURL);
    }

    public static class CurrentBookList {

        private static ArrayList<Book> curBookList = new ArrayList<>();

        public static void set(ArrayList<Book> bookList) {
            curBookList = bookList;
        }

        public static ArrayList<Book> getList() {
            return curBookList;
        }


        public static Book getBook(String bookId){
            return curBookList.stream().filter(book -> book.id.equals(bookId)).findFirst().orElse(null);
        }

    }
}
