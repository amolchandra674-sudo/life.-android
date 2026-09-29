package com.life.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.graphics.Insets;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.net.Uri;
import java.io.OutputStream;
import java.util.Base64;

public class MainActivity extends Activity {
    private WebView webView;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(0xFFF7F2E8);
        getWindow().setNavigationBarColor(0xFFF7F2E8);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFFF7F2E8);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFFF7F2E8);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new LifeBridge(), "Android");

        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // Android 15+ enforces edge-to-edge for modern target SDKs.
        // Keep Life's bottom navigation above the phone's system navigation area.
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0, top, 0, bottom);
            return insets;
        });

        setContentView(root);
        root.requestApplyInsets();

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    private class LifeBridge {
        @JavascriptInterface public void saveFile(String fileName, String mimeType, String base64) {
            try {
                byte[] data = Base64.getDecoder().decode(base64);
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                v.put(MediaStore.Downloads.MIME_TYPE, mimeType);
                v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Life");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) throw new Exception("Could not create file");
                try (OutputStream out = getContentResolver().openOutputStream(uri)) { out.write(data); }
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Saved to Downloads/Life", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Could not save file", Toast.LENGTH_SHORT).show());
            }
        }

        @JavascriptInterface public void printPage() {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager)getSystemService(Context.PRINT_SERVICE);
                if (pm == null) return;
                PrintDocumentAdapter a = webView.createPrintDocumentAdapter("Life report");
                pm.print("Life report", a, new PrintAttributes.Builder().build());
            });
        }
    }
}
