package ca.alina.to_dolist;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class DropboxWebActivity extends AppCompatActivity {

    private static final String urlOAuth = "https://www.dropbox.com/oauth2/authorize";
    private static final String paramResponseType = "token";
    private static final String paramClientId = "ig7k7p1t1h0s2ue";
    private static final String paramRedirectUri = "http://localhost/myapp/dropbox";
    private static final String urlGoogleSignIn = "https://accounts.google.com/signin/oauth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox_web);

        WebView webView = (WebView) findViewById(R.id.webView);

        // display loading progress
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView1, String uri) {
                if (uri != null) {
                    if (uri.startsWith(paramRedirectUri)) {
                        // retrieve token
                        retrieveToken(uri);
                        webView1.setVisibility(WebView.GONE);
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

                // todo process error?
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Stop spinner or progressBar
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        // for CSRF protection
        //String paramState;

        Uri.Builder builder = Uri.parse(urlOAuth).buildUpon();
        builder.appendQueryParameter("response_type", paramResponseType);
        builder.appendQueryParameter("client_id", paramClientId);
        builder.appendQueryParameter("redirect_uri", paramRedirectUri);
        String newUri = builder.build().toString();
        Log.i("DropboxWebActivity", newUri);


        webView.loadUrl(newUri);
    }

    private void retrieveToken(String uri) {
        Log.i("DropboxWebActivity", "redirect: " + uri);
    }
}
