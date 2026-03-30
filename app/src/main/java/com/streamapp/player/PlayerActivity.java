package com.streamapp.player;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayerActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private ImageButton btnBack;
    private View loadingOverlay;

    // Domains to block (ads, trackers, popups)
    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>(Arrays.asList(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "ads.pubmatic.com", "ads.rubiconproject.com", "amazon-adsystem.com",
        "cdn.adnxs.com", "ib.adnxs.com", "ads.yahoo.com",
        "openx.net", "mopub.com", "applovin.com", "unity3d.com",
        "outbrain.com", "taboola.com", "sharethrough.com",
        "criteo.com", "criteo.net", "2mdn.net",
        "scorecardresearch.com", "quantserve.com", "hotjar.com",
        "facebook.com/tr", "connect.facebook.net",
        "popads.net", "popcash.net", "propellerads.com",
        "adsrvr.org", "smartadserver.com", "exoclick.com",
        "trafficjunky.net", "adtng.com", "juicyads.com",
        "adcolony.com", "chartboost.com", "ironsource.com"
    ));

    // JS injected to kill popups and overlays
    private static final String AD_BLOCK_JS =
        "(function() {" +
        "  // Block window.open popups" +
        "  window.open = function() { return null; };" +
        "  // Block alert/confirm spam" +
        "  window.alert = function() {};" +
        "  window.confirm = function() { return true; };" +
        "  // Remove overlay divs commonly used by ads" +
        "  function removeAds() {" +
        "    var selectors = [" +
        "      '[id*=\"ad\"]', '[class*=\"ad-\"]', '[class*=\"ads\"]'," +
        "      '[id*=\"popup\"]', '[class*=\"popup\"]'," +
        "      '[id*=\"overlay\"]', '[class*=\"overlay\"]'," +
        "      '[class*=\"banner\"]', 'iframe[src*=\"ad\"]'," +
        "      '.modal-backdrop', '[class*=\"gdpr\"]'," +
        "      '[id*=\"cookie\"]', '[class*=\"cookie\"]'" +
        "    ];" +
        "    selectors.forEach(function(sel) {" +
        "      try {" +
        "        document.querySelectorAll(sel).forEach(function(el) {" +
        "          if (el && el.style) el.style.display = 'none';" +
        "        });" +
        "      } catch(e) {}" +
        "    });" +
        "  }" +
        "  removeAds();" +
        "  // Keep removing every 500ms for dynamic ads" +
        "  setInterval(removeAds, 500);" +
        "  // Block navigation to ad pages" +
        "  document.addEventListener('click', function(e) {" +
        "    var el = e.target.closest('a');" +
        "    if (el && el.target === '_blank') {" +
        "      e.preventDefault(); e.stopPropagation();" +
        "    }" +
        "  }, true);" +
        "})();";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        String link = getIntent().getStringExtra("link");
        String title = getIntent().getStringExtra("title");
        String number = getIntent().getStringExtra("number");

        if (number != null && title != null) {
            tvTitle.setText(number + " – " + (title.contains(":") ? title.substring(0, title.indexOf(":")) : title));
        }

        btnBack.setOnClickListener(v -> finish());

        setupWebView();

        if (link != null) {
            webView.loadUrl(link);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setGeolocationEnabled(false);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        );

        // Block ads at network level
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host != null) {
                    for (String blocked : BLOCKED_DOMAINS) {
                        if (host.contains(blocked)) {
                            // Return empty response = blocked
                            return new WebResourceResponse("text/plain", "utf-8",
                                new ByteArrayInputStream("".getBytes()));
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingOverlay.setVisibility(View.GONE);
                // Inject ad-block JS
                view.evaluateJavascript(AD_BLOCK_JS, null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                // Only allow supervideo domain navigation
                if (host != null && (host.contains("supervideo") || host.contains("player"))) {
                    return false; // allow
                }
                // Block external navigations (popups, redirects)
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            // Block popups
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                return false; // Block all popup windows
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // TV remote support
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
