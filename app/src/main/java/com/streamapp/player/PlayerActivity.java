package com.streamapp.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
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
    private View topBar;

    // Fullscreen
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private FrameLayout mFullscreenContainer;
    private int mOriginalOrientation;

    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>(Arrays.asList(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "ads.pubmatic.com", "ads.rubiconproject.com", "amazon-adsystem.com",
        "cdn.adnxs.com", "ib.adnxs.com", "ads.yahoo.com",
        "openx.net", "mopub.com", "applovin.com",
        "outbrain.com", "taboola.com", "sharethrough.com",
        "criteo.com", "criteo.net", "2mdn.net",
        "scorecardresearch.com", "quantserve.com", "hotjar.com",
        "connect.facebook.net",
        "popads.net", "popcash.net", "propellerads.com",
        "adsrvr.org", "smartadserver.com", "exoclick.com",
        "trafficjunky.net", "adtng.com", "juicyads.com",
        "adcolony.com", "chartboost.com", "ironsource.com"
    ));

    private static final String AD_BLOCK_JS =
        "(function() {" +
        "  window.open = function() { return null; };" +
        "  window.alert = function() {};" +
        "  window.confirm = function() { return true; };" +
        "  function removeAds() {" +
        "    var selectors = [" +
        "      '[id*=\"ad\"]', '[class*=\"ad-\"]', '[class*=\"ads\"]'," +
        "      '[id*=\"popup\"]', '[class*=\"popup\"]'," +
        "      '[id*=\"overlay\"]'," +
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
        "  setInterval(removeAds, 500);" +
        "  document.addEventListener('click', function(e) {" +
        "    var el = e.target.closest('a');" +
        "    if (el && el.target === '_blank') {" +
        "      e.preventDefault(); e.stopPropagation();" +
        "    }" +
        "  }, true);" +
        "})();";

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Schermo sempre acceso durante la riproduzione
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_player);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        topBar = findViewById(R.id.topBar);

        // Container per il fullscreen video
        mFullscreenContainer = new FrameLayout(this);
        mFullscreenContainer.setBackgroundColor(0xFF000000);
        addContentView(mFullscreenContainer,
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );
        mFullscreenContainer.setVisibility(View.GONE);

        String link = getIntent().getStringExtra("link");
        String title = getIntent().getStringExtra("title");
        String number = getIntent().getStringExtra("number");

        if (number != null && title != null) {
            tvTitle.setText(number + " – " + (title.contains(":") ? title.substring(0, title.indexOf(":")) : title));
        }

        btnBack.setOnClickListener(v -> {
            if (mCustomView != null) {
                hideCustomView();
            } else {
                finish();
            }
        });

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
        // Cache aggressiva per velocizzare il caricamento
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDatabaseEnabled(true);
        // Precarica connessioni
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host != null) {
                    for (String blocked : BLOCKED_DOMAINS) {
                        if (host.contains(blocked)) {
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
                view.evaluateJavascript(AD_BLOCK_JS, null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && (host.contains("supervideo") || host.contains("player"))) {
                    return false;
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            // *** FULLSCREEN: il pulsante fullscreen del player funziona ***
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                mCustomViewCallback = callback;
                mOriginalOrientation = getRequestedOrientation();

                // Nascondi UI
                topBar.setVisibility(View.GONE);
                webView.setVisibility(View.GONE);

                // Mostra il video a schermo intero
                mFullscreenContainer.addView(mCustomView,
                    new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                );
                mFullscreenContainer.setVisibility(View.VISIBLE);

                // Forza landscape e nascondi barre sistema
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                hideSystemUI();
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                return false;
            }
        });
    }

    private void hideCustomView() {
        if (mCustomView == null) return;

        // Ripristina UI
        mFullscreenContainer.setVisibility(View.GONE);
        mFullscreenContainer.removeView(mCustomView);
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        mCustomViewCallback = null;

        topBar.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);

        // Ripristina orientamento e barre sistema
        setRequestedOrientation(mOriginalOrientation);
        showSystemUI();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                hideCustomView();
                return true;
            }
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Mantieni fullscreen se si ritorna al focus
        if (hasFocus && mCustomView != null) {
            hideSystemUI();
        }
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
