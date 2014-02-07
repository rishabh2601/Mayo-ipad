/**
 * Workaround taken from defect 17535, March 2012
 * http://code.google.com/p/android/issues/detail?id=17535
 */
package org.cnmc.painclinic.painreport;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;

public class AssetIncludeWorkaround extends WebViewClient {
    private Context mContext;

    public AssetIncludeWorkaround(Context context) {
        mContext = context;
    }

    @Override
    @TargetApi(11)
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        InputStream stream = inputStreamForAndroidResource(url);
        if (stream != null) {
            return new WebResourceResponse(null, null, stream);
        }
        return super.shouldInterceptRequest(view, url);
    }

    private InputStream inputStreamForAndroidResource(String url) {
        final String ANDROID_ASSET = "file:///android_asset/";

        if (url.startsWith(ANDROID_ASSET)) {
            url = url.replaceFirst(ANDROID_ASSET, "");
            try {
                AssetManager assets = mContext.getAssets();
                Uri uri = Uri.parse(url);
                return assets.open(uri.getPath(), AssetManager.ACCESS_STREAMING);
            } catch (IOException e) {}
        }
        return null;
    }

    /**
     * Added this from the setWebViewClient call over in BrowseLocal
     * @param view
     * @param url
     * @return
     */
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
        webView.loadUrl("javascript:document.getElementById('__prPIN').value='"+MainActivity.getUserPIN()+"';javascript:document.getElementById('__prServer').value='"+MainActivity.getSubmitURL()+"'");
    }
}
