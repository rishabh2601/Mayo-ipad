package org.cnmc.painclinic.painreport;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by kevinagary on 9/8/13.
 */
public class Browselocal extends Activity {
    private WebView webView;

    private String __currentURL = null;
    private Boolean __isPaused = false;
    //private boolean webViewTimersFlag = false;

    // nested class to inject to Javascript
    class BLHelper {
        @JavascriptInterface
        public void finishMe() {
            Browselocal.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Browselocal.this.finish();
                }
            });
        }
        /* Tried this hack to get back to the submit page without success
        @JavascriptInterface
        public void goBack() {
            Browselocal.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Browselocal.this.webView.loadDataWithBaseURL("file:///android_asset/", "<a href=\"submit.html\">BACK</a>", "text/html", "utf-8", null);
                }
            });
        }
        */
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BrowseLocal", "LIFECYCLE STATE: in onCreate after super");
        setContentView(R.layout.activity_browselocal);

        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("BrowseLocal", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        // inject our object that allows a finish callback
        webView.addJavascriptInterface(new BLHelper(), "__blhelper");

        //webView.setWebViewClient(new WebViewClient());
        //if (Build.VERSION.SDK_INT < 11) {
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean shouldOverride = false;
                if (url.startsWith("https://")) { //NON-NLS
                    // DO SOMETHING
                    shouldOverride = true;
                } else {
                    Log.d("Browselocal", "shouldOverride is gonna return false");
                }
                return shouldOverride;
            }

            @Override
            public void onPageFinished (WebView webView, String url)
            {
                if (webView.getOriginalUrl().indexOf("index.html") != -1) {
                    Log.d("Browselocal", " called onPageFinished with PIN " + MainActivity.getUserPIN() + " and submit URL " + MainActivity.getSubmitURL());
                    webView.loadUrl("javascript:document.getElementById('__prPIN').value='"+MainActivity.getUserPIN()+"';javascript:document.getElementById('__prServer').value='"+MainActivity.getSubmitURL()+"'");
                } else {
                    Log.d("BrowseLocal", " called onPageFinished but not on index.html");
                }
            }
        });
        //} else {
        //  webView.setWebViewClient(new AssetIncludeWorkaround(this.getApplicationContext()));
        //}

        WebSettings webSettings = webView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSupportZoom(false);

        // proposed fix for WebViewCoreThread memory leak - an initial call to resumeTimers and only one call to pauseTimers below
        // http://www.anddev.org/other-coding-problems-f5/webviewcorethread-problem-t10234.html
        webView.resumeTimers();
        __isPaused = false;
        //__resumeMe();

        //webView.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d("BrowseLocal", "LIFECYCLE STATE: in onPause after super");
        if (webView != null) {
            __currentURL = webView.getOriginalUrl();
            Log.d("Browselocal", "onPause saving current URL as " + __currentURL);
        } else {
            Log.d("Browselocal", "onPause pausing with no WebView");
        }
        __pauseMe();

    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("BrowseLocal", "LIFECYCLE STATE: in onResume after super");
        if (__currentURL == null) {
            if (webView != null) {
                Log.d("Browselocal", "onResume resuming to main URL");
                webView.loadUrl(MainActivity.getDeliveryURL());
                __currentURL = MainActivity.getDeliveryURL();
            }
        } else {
            if (webView != null) {
                Log.d("Browselocal", "onResume resuming to " + __currentURL);
                webView.loadUrl(__currentURL);
            }
        }
        __resumeMe();
    }

    private void __pauseMe() {
        synchronized (__isPaused) {
            if (!__isPaused) {
                if (webView != null) {
                    webView.pauseTimers();
                    __isPaused = true;
                }
            }
        }
    }

    private void __resumeMe() {
        synchronized (__isPaused) {
            if (__isPaused) {
                if (webView != null) {
                    webView.resumeTimers();
                    __isPaused = false;
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // disable the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false; // this avoids passing to super
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        Log.d("BrowseLocal", "LIFECYCLE STATE: in onDestroy");
        if (webView != null) {
            //webView.removeAllViews();
            webView.clearHistory();
            webView.clearCache(true);
            if (Build.VERSION.SDK_INT < 18) {
                webView.clearView();
            } else {
                webView.loadUrl("about:blank");
            }
            webView.freeMemory();
            //webView.pauseTimers();
            __pauseMe();
            webView = null;
        }

        super.onDestroy();
    }
    @Override
    public void finish() {
        Log.d("BrowseLocal", "LIFECYCLE STATE: in finish");
        if (webView != null) {
            //webView.removeAllViews();
            webView.clearHistory();
            webView.clearCache(true);
            if (Build.VERSION.SDK_INT < 18) {
                webView.clearView();
            } else {
                webView.loadUrl("about:blank");
            }
            webView.freeMemory();
            //webView.pauseTimers();
            __pauseMe();
            webView = null;
        }

        super.finish();
    }
}