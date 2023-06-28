package com.example.beebliotheca.object;

import java.util.ArrayList;

public class Favorite {

    String favoriteId;
    String bookId;
    Integer userId;

    public Favorite(String favoriteId, String bookId, int userId) {
        this.favoriteId = favoriteId;
        this.bookId = bookId;
        this.userId = userId;
    }

    public String getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static class CurrentFavorite {
        private static Favorite curFavorite;

        public static void set(Favorite favorite) {
            curFavorite = favorite;
        }

        public static Favorite get() {
            return curFavorite;
        }

    }
}
