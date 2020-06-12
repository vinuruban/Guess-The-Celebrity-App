package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    /** Tag for the log messages */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    String url = "https://www.imdb.com/list/ls052283250/";
    ArrayList<String> names;
    ArrayList<String> images;
    int randomTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        String result;

        //runs bg thread 1
        try {
            result = task.execute(url).get(); //in GBooks, everything was handled in extractFeatureFromJson() and didn't need to return anything, hence the code was task.execute(url);
            names = extractInfo(result, "<img alt=\"(.*?)\"");
            images = extractInfo(result, "src=\"(.*?)\"");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //runs bg thread 2 within the method
        generateQuiz();

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            // Perform the HTTP request for earthquake data and process the response.
            String result = QueryUtils.fetchData(urls[0]);
            return result;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public ArrayList<String> extractInfo(String html, String splitBetween) {
        ArrayList<String> list = new ArrayList<String>();
        Pattern p = Pattern.compile(splitBetween);
        Matcher m = p.matcher(html);
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }

    public void generateQuiz() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        //ArrayList<String> avoided since String[] can be limited to 4. Also, we can set text to specific position as coded in the For loop below (nameList[i] = ... )
        String[] nameList = new String[4];

        Random rand = new Random();

        // To select a celeb - obtain a number between [0 - 99].
        int randomCeleb = rand.nextInt(100);

        // Obtain a number between [0 - 3].
        randomTag = rand.nextInt(4);

        //set image through another bg thread!
        try {
            new DownloadImageTask(imageView)
                    .execute(images.get(randomCeleb));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error: " + e.getMessage());
        }

        //set names
        for (int i = 0; i < 4; i++) {
            if (i == randomTag) {
                nameList[i] = names.get(randomCeleb);
            } else {
                int otherRandomCelebs = rand.nextInt(100);
                //in case the randomly generated numbers are equal to randomCeleb, regenerated random numbers
                while (otherRandomCelebs == randomCeleb) {
                    otherRandomCelebs = rand.nextInt(100);
                }
                nameList[i] = names.get(otherRandomCelebs);
            }

            Button button = (Button) findViewById(R.id.button);
            Button button2 = (Button) findViewById(R.id.button2);
            Button button3 = (Button) findViewById(R.id.button3);
            Button button4 = (Button) findViewById(R.id.button4);

            button.setText(nameList[0]);
            button2.setText(nameList[1]);
            button3.setText(nameList[2]);
            button4.setText(nameList[3]);
        }
    }

    public void clickAnswer(View view) {
        Button clickedButton = (Button) view;
        String expectedPosition = Integer.toString(randomTag);
        String actualPosition = clickedButton.getTag().toString();

        if (expectedPosition.equals(actualPosition)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            generateQuiz();
        }
        else {
            Toast.makeText(this, "Wrong - try again", Toast.LENGTH_SHORT).show();
        }
    }

}