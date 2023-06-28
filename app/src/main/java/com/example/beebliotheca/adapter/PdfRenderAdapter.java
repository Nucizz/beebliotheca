package com.example.beebliotheca.adapter;

import android.os.AsyncTask;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PdfRenderAdapter extends AsyncTask<String, Void, File> {
    private DownloadPdfListener listener;

    public PdfRenderAdapter(DownloadPdfListener listener) {
        this.listener = listener;
    }

    @Override
    protected File doInBackground(String... urls) {
        String pdfUrl = urls[0];
        File outputFile = null;

        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Create a temporary file to save the downloaded PDF
                outputFile = File.createTempFile("temp_pdf", ".pdf");

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;
    }

    @Override
    protected void onPostExecute(File file) {
        if (file != null) {
            listener.onPdfDownloaded(file);
        } else {
            listener.onDownloadFailed();
        }
    }

    public interface DownloadPdfListener {
        void onPdfDownloaded(File file);
        void onDownloadFailed();
    }
}
