package com.example.beebliotheca.object;



import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;

public class History{
    String historyId;
    String bookId;
    int userId;
    String accessTime;

    public History(String historyId, String bookId, int userId, String accessTime) {
        this.historyId = historyId;
        this.bookId = bookId;
        this.userId = userId;
        this.accessTime = accessTime;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
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

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(String accessTime) {
        this.accessTime = accessTime;
    }

    public static class CurrentHistory {
        private static History curHistory;

        public static void set(History history) {
            curHistory = history;
        }

        public static History get() {
            return curHistory;
        }

    }


}

