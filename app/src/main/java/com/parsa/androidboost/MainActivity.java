
package com.parsa.androidboost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.BatteryManager;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import java.io.File;

public class MainActivity extends Activity {

    private LinearLayout content;

    private static final int BG = Color.rgb(8,13,18);
    private static final int PANEL = Color.rgb(20,29,37);
    private static final int GREEN = Color.rgb(0,230,118);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(180,195,205);

    static class AppEntry {
        String label, packageName, reason;

        AppEntry(String l, String p, String r) {
            label=l;
            packageName=p;
            reason=r;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20),dp(24),dp(20),dp(24));

        scroll.addView(content);
        setContentView(scroll);

        TextView title = text("ANDROID BOOST",26,GREEN,true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView subtitle = text(
            "USB Device Optimizer • نسخه ۰.۲",
            13,MUTED,false
        );
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        addPanel(
            "اطلاعات دستگاه",
            "برند: " + Build.MANUFACTURER +
            "\nمدل: " + Build.MODEL +
            "\nاندروید: " + Build.VERSION.RELEASE +
            "\nSDK: " + Build.VERSION.SDK_INT
        );

        Button optimize = button("داشبورد بهینه‌سازی گوشی");
        optimize.setOnClickListener(v -> showDashboard());
        content.addView(optimize);

        Button scan = button("شروع اسکن برنامه‌ها");
        scan.setOnClickListener(v -> scanApps());
        content.addView(scan);

        addPanel(
            "وضعیت",
            "بررسی اطلاعات دستگاه فعال است. هیچ برنامه‌ای بدون اجازه کاربر حذف نمی‌شود."
        );
    }

    private void showDashboard() {
        content.removeAllViews();

        TextView title = text("داشبورد بهینه‌سازی",24,GREEN,true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        long totalRam = 0;
        long freeRam = 0;

        ActivityManager manager =
            (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        if(manager != null) {
            ActivityManager.MemoryInfo info =
                new ActivityManager.MemoryInfo();

            manager.getMemoryInfo(info);

            totalRam = info.totalMem;
            freeRam = info.availMem;
        }

        long usedRam = Math.max(0,totalRam-freeRam);

        addPanel(
            "حافظه RAM",
            "کل حافظه: " + formatSize(totalRam) +
            "\nمصرف‌شده: " + formatSize(usedRam) +
            "\nآزاد: " + formatSize(freeRam) +
            "\nدرصد استفاده: " + percentage(usedRam,totalRam) + "%"
        );

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());

        long totalStorage =
            stat.getBlockSizeLong() * stat.getBlockCountLong();

        long freeStorage =
            stat.getAvailableBlocksLong() * stat.getBlockSizeLong();

        long usedStorage = totalStorage-freeStorage;

        addPanel(
            "حافظه داخلی",
            "کل حافظه: " + formatSize(totalStorage) +
            "\nاشغال‌شده: " + formatSize(usedStorage) +
            "\nفضای خالی: " + formatSize(freeStorage) +
            "\nدرصد استفاده: " + percentage(usedStorage,totalStorage) + "%"
        );

        Intent batteryIntent = registerReceiver(
            null,
            new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );

        String battery = "اطلاعات باتری در دسترس نیست.";

        if(batteryIntent != null) {
            int level = batteryIntent.getIntExtra(
                BatteryManager.EXTRA_LEVEL,-1
            );

            int scale = batteryIntent.getIntExtra(
                BatteryManager.EXTRA_SCALE,-1
            );

            if(level >= 0 && scale > 0) {
                battery = "شارژ باتری: " + (level*100/scale) + "%";
            }
        }

        addPanel("وضعیت باتری",battery);

        Button refresh = button("به‌روزرسانی اطلاعات");
        refresh.setOnClickListener(v -> showDashboard());
        content.addView(refresh);

        Button batterySettings = button("مدیریت مصرف باتری");
        batterySettings.setOnClickListener(v -> {
            try {
                startActivity(new Intent(
                    android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS
                ));
            } catch(Exception e) {
                startActivity(new Intent(
                    android.provider.Settings.ACTION_SETTINGS
                ));
            }
        });
        content.addView(batterySettings);

        Button back = button("بازگشت به صفحه اصلی");
        back.setOnClickListener(v -> buildUi());
        content.addView(back);
    }

    private String percentage(long used,long total) {
        if(total <= 0) return "0";
        return String.valueOf(
            Math.min(100,(used*100/total))
        );
    }

    private String formatSize(long bytes) {
        if(bytes <= 0) return "نامشخص";

        double gb = bytes / (1024.0*1024.0*1024.0);

        if(gb >= 1) {
            return String.format(Locale.US,"%.2f GB",gb);
        }

        double mb = bytes / (1024.0*1024.0);

        return String.format(Locale.US,"%.0f MB",mb);
    }

    private void scanApps() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> installed;

        try {
            installed = pm.getInstalledPackages(
                PackageManager.GET_PERMISSIONS
            );
        } catch(Exception e) {
            addPanel("خطا","دریافت فهرست برنامه‌ها ناموفق بود.");
            return;
        }

        int total = installed.size();

        java.util.ArrayList<AppEntry> flagged =
            new java.util.ArrayList<>();

        for(PackageInfo pi : installed) {
            if(pi.applicationInfo == null) continue;

            String pkg = pi.packageName == null
                ? "" : pi.packageName.toLowerCase(Locale.ROOT);

            String label;

            try {
                label = pm.getApplicationLabel(
                    pi.applicationInfo
                ).toString();
            } catch(Exception e) {
                label = pkg;
            }

            String s = (pkg+" "+label).toLowerCase(Locale.ROOT);

            String[] terms = {
                "spy","stealer","keylogger",
                "trojan","malware","hacktool","rattool"
            };

            for(String term : terms) {
                if(s.contains(term)) {
                    flagged.add(new AppEntry(
                        label,
                        pi.packageName,
                        "نام یا شناسه مشکوک است؛ این به معنی ویروس قطعی نیست."
                    ));
                    break;
                }
            }
        }

        content.removeAllViews();

        TextView title = text("نتیجه اسکن",24,GREEN,true);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        addPanel(
            "گزارش اسکن",
            "برنامه‌های بررسی‌شده: "+total+
            "\nموارد نیازمند بررسی: "+flagged.size()+
            "\nاین اسکن ساده است و جای آنتی‌ویروس معتبر را نمی‌گیرد."
        );

        if(flagged.isEmpty()) {
            addPanel(
                "نتیجه",
                "موردی بر اساس نام برنامه‌ها پیدا نشد. این نتیجه تضمین پاک بودن دستگاه نیست."
            );
        }

        for(AppEntry app : flagged) {
            addAppCard(app);
        }

        Button back = button("بازگشت");
        back.setOnClickListener(v -> buildUi());
        content.addView(back);
    }

    private void addAppCard(AppEntry app) {
        LinearLayout p = panel();

        p.addView(text(app.label,18,WHITE,true));
        p.addView(text(app.packageName,12,MUTED,false));
        p.addView(text(app.reason,13,MUTED,false));

        Button uninstall = button("بررسی و حذف برنامه");

        uninstall.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("بررسی برنامه")
                .setMessage(
                    "این برنامه فقط به دلیل نام یا شناسه علامت‌گذاری شده است. حذف آن با خود شماست."
                )
                .setNegativeButton("لغو",null)
                .setPositiveButton("رفتن به حذف برنامه",(d,w) -> {
                    try {
                        startActivity(new Intent(
                            Intent.ACTION_DELETE,
                            Uri.parse("package:"+app.packageName)
                        ));
                    } catch(Exception ignored) {}
                })
                .show()
        );

        p.addView(uninstall);
        content.addView(p);
    }

    private void addPanel(String heading,String body) {
        LinearLayout p = panel();

        p.addView(text(heading,17,GREEN,true));
        p.addView(text(body,14,WHITE,false));

        content.addView(p);
    }

    private LinearLayout panel() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(16),dp(14),dp(16),dp(14));
        p.setBackgroundColor(PANEL);

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1,-2);

        lp.setMargins(0,dp(14),0,dp(4));
        p.setLayoutParams(lp);

        return p;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.BLACK);
        b.setBackgroundColor(GREEN);

        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(-1,-2);

        lp.setMargins(0,dp(10),0,0);
        b.setLayoutParams(lp);

        return b;
    }

    private TextView text(
        String s,int size,int color,boolean bold
    ) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);

        if(bold) {
            t.setTypeface(
                null,android.graphics.Typeface.BOLD
            );
        }

        t.setPadding(0,dp(4),0,dp(4));

        return t;
    }

    private int dp(int v) {
        return (int)(
            v*getResources().getDisplayMetrics().density+0.5f
        );
    }
    }
                
