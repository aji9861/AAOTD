package com.runnirr.aaotdfetcher;


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.webkit.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Adam on 5/29/13.
 *
 * Asynchronous web loader that "purchases" the free app of the day from Amazon
 */
public class WebLoader extends AsyncTask<Void, Void, Void> {
    private String username;
    private String password;

    public final NotificationDataManager notificationData = new NotificationDataManager();
    public final Context context;
    public final MainActivity myActivity;

    private String previousUrl = "";

    public WebLoader(MainActivity c){
        this.context = c;
        this.myActivity = c;

        getCredentials();
    }



    private void getCredentials(){
        SharedPreferences preferences = myActivity.getPreferences(Context.MODE_PRIVATE);
        this.username = preferences.getString("username", "");
        this.password = preferences.getString("password", "");
    }

    @Override
    protected Void doInBackground(Void... params) {
        WebView myWebView = (WebView) myActivity.findViewById(R.id.webView);
        WebViewClient myWebClient = new MyWebViewClient();
        WebChromeClient myChromeClient = new MyWebChromeClient();
        myWebView.setWebViewClient(myWebClient);
        myWebView.setWebChromeClient(myChromeClient);

        myWebView.getSettings().setSavePassword(false);
        myWebView.getSettings().setJavaScriptEnabled(true);

        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        myWebView.loadUrl(context.getResources().getString(R.string.amazon_app_url));

        return null;
    }

    private class MyWebViewClient extends WebViewClient{
        public void onPageFinished(WebView view, String url) {
            if (url.startsWith(getString(R.string.amazon_app_start_url))){
                previousUrl = getString(R.string.amazon_app_start_url);
                view.loadUrl(JavascriptCalls.getJsSignInButton());
            } else if(url.startsWith(getString(R.string.amazon_sign_in_start_url))){
                if(previousUrl.startsWith(getString(R.string.amazon_sign_in_start_url))){
                    previousUrl = getString(R.string.amazon_sign_in_start_url);
                    Log.e("AAOTD", "Error logging in.");
                    myActivity.getPreferences(Context.MODE_PRIVATE).edit().putString("password", "");
                }else{
                    previousUrl = getString(R.string.amazon_sign_in_start_url);
                    view.loadUrl(JavascriptCalls.getJsSignInForm(username, password));
                }
            }else if (url.startsWith(getString(R.string.amazon_thanks_start_url)) ||
                    url.startsWith(getString(R.string.amazon_thanks_start_ssl_url))){
                previousUrl = getString(R.string.amazon_thanks_start_url);
                view.loadUrl(JavascriptCalls.getJsFetchComplete());
            } else{
                previousUrl = url;
                Log.d("AAOTD", "Some other page loaded: " + url);
            }
            super.onPageFinished(view, url);
        }

        private String getString(int id){
            return context.getResources().getString(id);
        }
    }

    private class MyWebChromeClient extends WebChromeClient{

        private String parseMessage(String message, String split){
            return message.split(split)[1];
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            if(message.startsWith("link:")){
                String link = parseMessage(message, "link:");
                notificationData.setLink(link);

            }else if(message.startsWith("image:")){
                String image = parseMessage(message, "image:");
                // Set the image asynchronously
                new ImageFetch().execute(image);

            }else if(message.startsWith("title:")){
                String title = parseMessage(message, "title:");
                notificationData.setTitle(title);

            }else{
                Log.w("AAOTD", "Unhandled JSAlert: " + message);
            }

            result.confirm();
            return true;
        }

    }

    private final class ImageFetch extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... params){
            Log.d("AAOTD", "Doing background image fetch for image " + params[0]);
            return loadImageFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap b){
            notificationData.setBitmapImage(b);
        }

        /* Adapted from http://stackoverflow.com/questions/8992964/android-load-from-url-to-bitmap */
        private Bitmap loadImageFromURL(String url) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream is = connection.getInputStream();
                Bitmap b = BitmapFactory.decodeStream(is);
                connection.disconnect();
                return b;
            } catch (Exception e) {
                Log.e("AAOTD", "Error fetching image: " + url, e);
                return null;
            }
        }
    }

    private final class NotificationDataManager{
        protected String link = null;
        protected String title = null;
        protected Bitmap bitmap = null;

        public void displayNotification(){
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                    .setContentTitle("New app from Amazon.com")
                    .setContentText(title)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(bitmap);

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(link.hashCode(), nb.build());
        }

        void setLink(String l){
            this.link = l;
            checkAndNotify();
        }

        void setBitmapImage(Bitmap b){
            bitmap = b;
            checkAndNotify();
        }

        void setTitle(String t){
            this.title = t;
            checkAndNotify();
        }

        private synchronized boolean checkAllData(){
            return link != null && bitmap != null && title != null;
        }

        private void checkAndNotify(){
            if(checkAllData()){
                displayNotification();
            }
        }
    }

}
