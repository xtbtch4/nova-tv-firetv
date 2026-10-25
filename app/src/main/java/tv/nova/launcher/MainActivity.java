package tv.nova.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class MainActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(5,7,10));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "NovaTV");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction()==KeyEvent.ACTION_DOWN && e.getKeyCode()==KeyEvent.KEYCODE_BACK) {
            if (web != null) web.evaluateJavascript("backHome()", null);
            return true;
        }
        return super.dispatchKeyEvent(e);
    }
    public class Bridge {
        @android.webkit.JavascriptInterface public String getApps() {
            JSONArray out = new JSONArray();
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                PackageManager pm = getPackageManager();
                List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
                if (list.isEmpty()) {
                    i.removeCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    list = pm.queryIntentActivities(i, 0);
                }
                for (ResolveInfo r : list) {
                    String pkg = r.activityInfo.packageName;
                    if (pkg.equals(getPackageName())) continue;
                    JSONObject o = new JSONObject();
                    o.put("name", r.loadLabel(pm).toString());
                    o.put("package", pkg);
                    out.put(o);
                }
            } catch (Exception ignored) {}
            return out.toString();
        }
        @android.webkit.JavascriptInterface public void launchApp(String pkg) {
            runOnUiThread(() -> {
                try {
                    Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
                    if (i != null) startActivity(i);
                    else Toast.makeText(MainActivity.this, "Не удалось открыть приложение", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(MainActivity.this, "Ошибка запуска", Toast.LENGTH_SHORT).show();
                }
            });
        }
        @android.webkit.JavascriptInterface public void openSettings() {
            runOnUiThread(() -> { try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); } catch (Exception ignored) {} });
        }
    }
}