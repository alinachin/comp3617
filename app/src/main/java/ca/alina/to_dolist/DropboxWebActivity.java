package ca.alina.to_dolist;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DropboxWebActivity extends AppCompatActivity {

    private static final String urlOAuth = "https://www.dropbox.com/oauth2/authorize";
    private static final String paramResponseType = "token";
    private static final String paramClientId = "ig7k7p1t1h0s2ue";
    private static final String paramRedirectUri = "http://localhost/myapp/dropbox";
    private static final String urlGoogleSignIn = "https://accounts.google.com/signin/oauth";

    static final String API_UPLOAD = "https://content.dropboxapi.com/2/files/upload";
    static final String DROPBOX_FILENAME = "backup.db";
    static final String PREF_FILE = "oauth";
    static final String PREF_SESSION_KEY = "token";
    static final String PREF_USER_KEY = "uid";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_web);

        mWebView = (WebView) findViewById(R.id.webView);

        webLogin();
    }

    private void webLogin() {
        // display loading progress
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView1, String uri) {
                if (uri != null) {
                    if (uri.startsWith(paramRedirectUri)) {
                        saveToken(uri);

                        setResult(RESULT_OK);
                        finish();
                        return true;
                    }
                    if (uri.startsWith(urlGoogleSignIn)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // Show progressbar
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onReceivedError( WebView view, int errorCode, String description, String failingUrl) {
                // Show error
                // Stop spinner or progressbar
                progressBar.setVisibility(ProgressBar.GONE);

                // process error?
                Toast.makeText(view.getContext(), "Couldn't load Dropbox sign-in", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Stop spinner or progressBar
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);

        // for CSRF protection
        //String paramState;

        Uri.Builder builder = Uri.parse(urlOAuth).buildUpon();
        builder.appendQueryParameter("response_type", paramResponseType);
        builder.appendQueryParameter("client_id", paramClientId);
        builder.appendQueryParameter("redirect_uri", paramRedirectUri);
        String newUri = builder.build().toString();
        //Log.i("DropboxWebActivity", newUri);


        mWebView.loadUrl(newUri);
    }

    private void saveToken(String uri) {
        //Log.i("DropboxWebActivity", "redirect: " + uri);

        // todo check for error param

        SharedPreferences.Editor editor = getSharedPreferences(PREF_FILE, MODE_PRIVATE).edit();

        Uri parsedUri = Uri.parse(uri);
        String fragment = parsedUri.getFragment();
        //Log.e("DropboxWebActivity", fragment);

        String[] params = fragment.split("&");
        for (String s : params) {
            if (s.startsWith("account_id")) {
                String uid = s.substring(s.indexOf('=') + 1);
                //Log.e("DropboxWebActivity", uid);
                editor.putString(PREF_USER_KEY, uid);
            }
            else if (s.startsWith("access_token")) {
                String accessToken = s.substring(s.indexOf('=') + 1);
                //Log.e("DropboxWebActivity", accessToken);
                editor.putString(PREF_SESSION_KEY, accessToken);
            }
        }

        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
