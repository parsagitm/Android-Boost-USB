package com.parsa.androidboost;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.BatteryManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private LinearLayout content;

    private static final int BG = Color.rgb(4, 12, 29);
    private static final int BLUE = Color.rgb(76, 210, 255);
    private static final int GOLD = Color.rgb(255, 220, 105);
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = Color.rgb(190, 211, 232);

    static class AppEntry {
        String label, packageName, reason;
        AppEntry(String l, String p, String r) {
            label = l;
            packageName = p;
            reason = r;
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildHome();
    }

    private void buildHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackground(makeGradient(
            new int[]{Color.rgb(3, 10, 27), Color.rgb(5, 27, 53), Color.rgb(4, 12, 29)},
            0, 0, 0, 0, 0
        ));

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(26));
        scroll.addView(content);
        setContentView(scroll);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView version = text("👑  V1.0.0", 14, GOLD, true);
        version.setPadding(dp(14), dp(9), dp(14), dp(9));
        version.setBackground(makeGradient(
            new int[]{Color.argb(100, 30, 55, 85), Color.argb(70, 20, 35, 65)},
            dp(22), dp(1), Color.argb(180, 255, 220, 105), 0, 0
        ));
        top.addView(version);

        View spacer = new View(this);
        top.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        TextView settings = text("⚙", 27, BLUE, true);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(makeGradient(
            new int[]{Color.argb(130, 30, 95, 150), Color.argb(90, 10, 40, 85)},
            dp(40), dp(1), Color.argb(150, 100, 215, 255), dp(4), dp(4)
        ));
        settings.setOnClickListener(v -> openSettings(android.provider.Settings.ACTION_SETTINGS));
        top.addView(settings, new LinearLayout.LayoutParams(dp(52), dp(52)));
        content.addView(top);

        TextView robot = text("🤖", 54, BLUE, true);
        robot.setGravity(Gravity.CENTER);
        robot.setShadowLayer(dp(18), 0, 0, BLUE);
        LinearLayout.LayoutParams robotLp = new LinearLayout.LayoutParams(-1, -2);
        robotLp.topMargin = dp(8);
        content.addView(robot, robotLp);

        TextView title = text("Android Boost", 34, WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setShadowLayer(dp(15), 0, 0, BLUE);
        content.addView(title);

        TextView subtitle = text("SMART DEVICE OPTIMIZER  •  هوشمند، سریع، ساده", 12, MUTED, false);
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        addDeviceHero();

        addFeatureRow(
            "▦", "داشبورد RAM و حافظه",
            "مشاهده وضعیت دستگاه", BLUE,
            () -> showDashboard(),
            "⚡", "بهینه‌سازی هوشمند",
            "ابزارهای پیشنهادی گوشی", GOLD,
            () -> showSmartOptimizer()
        );

        addFeatureRow(
            "⬡", "اسکن برنامه‌ها",
            "بررسی نام برنامه‌های مشکوک", GOLD,
            () -> scanApps(),
            "▣", "مدیریت باتری",
            "رفتن به تنظیمات باتری", BLUE,
            () -> openSettings(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS)
        );

        LinearLayout tip = panel();
        TextView tipTitle = text("💡  نکته طلایی", 16, GOLD, true);
        tip.addView(tipTitle);
        tip.addView(text(
            "برای بهتر شدن عملکرد گوشی، برنامه‌های غیرضروری را از تنظیمات باتری و برنامه‌ها مدیریت کن.",
            13, WHITE, false
        ));
        content.addView(tip);

        TextView footer = text("ساخته‌شده برای تجربه بهتر اندروید  •  Android Boost", 11, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerLp = new LinearLayout.LayoutParams(-1, -2);
        footerLp.topMargin = dp(16);
        content.addView(footer, footerLp);
    }

    private void addDeviceHero() {
        LinearLayout hero = panel();
        LinearLayout.LayoutParams heroLp = new LinearLayout.LayoutParams(-1, -2);
        heroLp.setMargins(0, dp(22), 0, dp(8));
        hero.setLayoutParams(heroLp);

        TextView heading = text("وضعیت دستگاه شما", 18, GOLD, true);
        hero.addView(heading);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        long totalRam = 0, freeRam = 0;
        if (manager != null) {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            totalRam = info.totalMem;
            freeRam = info.availMem;
        }

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalStorage = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long freeStorage = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setGravity(Gravity.CENTER);
        TextView ramIcon = text("◉", 43, BLUE, true);
        ramIcon.setGravity(Gravity.CENTER);
        ramIcon.setShadowLayer(dp(12), 0, 0, BLUE);
        left.addView(ramIcon);
        TextView ramFree = text(percentage(freeRam, totalRam) + "%", 27, WHITE, true);
        ramFree.setGravity(Gravity.CENTER);
        left.addView(ramFree);
        TextView ramLabel = text("RAM آزاد", 12, MUTED, false);
        ramLabel.setGravity(Gravity.CENTER);
        left.addView(ramLabel);
        row.addView(left, new LinearLayout.LayoutParams(0, -2, 1));

        View divider = new View(this);
        divider.setBackgroundColor(Color.argb(130, 85, 195, 245));
        row.addView(divider, new LinearLayout.LayoutParams(dp(1), dp(125)));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setPadding(dp(14), 0, 0, 0);
        right.addView(text("📱  " + Build.MANUFACTURER + " " + Build.MODEL, 13, WHITE, true));
        right.addView(text("🤖  Android " + Build.VERSION.RELEASE, 13, MUTED, false));
        right.addView(text("▦  RAM: " + formatSize(totalRam), 13, MUTED, false));
        right.addView(text("▤  حافظه: " + formatSize(totalStorage), 13, MUTED, false));
        right.addView(text("فضای خالی: " + formatSize(freeStorage), 12, GOLD, true));
        row.addView(right, new LinearLayout.LayoutParams(0, -2, 2));
        hero.addView(row);
        content.addView(hero);
    }

    private void addFeatureRow(
        String icon1, String title1, String desc1, int color1, Runnable action1,
        String icon2, String title2, String desc2, int color2, Runnable action2
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(featureCard(icon1, title1, desc1, color1, action1),
            new LinearLayout.LayoutParams(0, dp(145), 1));

        View gap = new View(this);
        row.addView(gap, new LinearLayout.LayoutParams(dp(10), 1));

        row.addView(featureCard(icon2, title2, desc2, color2, action2),
            new LinearLayout.LayoutParams(0, dp(145), 1));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(10);
        content.addView(row, lp);
    }

    private LinearLayout featureCard(String icon, String title, String desc, int accent, Runnable action) {
        LinearLayout card = panel();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.addView(text(icon, 31, accent, true));
        card.addView(text(title, 14, WHITE, true));
        card.addView(text(desc, 11, MUTED, false));
        card.setOnClickListener(v -> action.run());
        card.setFocusable(true);
        return card;
    }

    private void showSmartOptimizer() {
        content.removeAllViews();
        addPageTitle("بهینه‌سازی هوشمند");

        addPanel("دستگاه شناسایی شد",
            "برند: " + detectBrand() +
            "\nمدل: " + Build.MODEL +
            "\nنسخه اندروید: " + Build.VERSION.RELEASE +
            "\nنسخه سیستم: " + Build.DISPLAY);

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        long totalRam = 0, freeRam = 0;
        if (manager != null) {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            totalRam = info.totalMem;
            freeRam = info.availMem;
        }

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalStorage = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long freeStorage = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();

        addPanel("گزارش بررسی",
            "RAM کل: " + formatSize(totalRam) +
            "\nRAM آزاد: " + formatSize(freeRam) +
            "\nفضای کل: " + formatSize(totalStorage) +
            "\nفضای خالی: " + formatSize(freeStorage));

        addPanel("اقدامات پیشنهادی",
            "هر ابزار را جداگانه انتخاب کن. قبل از باز شدن تنظیمات، اجازه می‌گیریم.");

        addAction("مدیریت برنامه‌ها",
            "مشاهده برنامه‌ها و مدیریت دسترسی و اجرای آن‌ها از تنظیمات اندروید.",
            android.provider.Settings.ACTION_APPLICATION_SETTINGS);

        addAction("مدیریت مصرف باتری",
            "مشاهده تنظیمات باتری و محدودیت فعالیت پس‌زمینه برنامه‌ها.",
            android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS);

        addAction("مدیریت حافظه داخلی",
            "بررسی فضای ذخیره‌سازی و پاک‌سازی فایل‌های غیرضروری توسط خود کاربر.",
            android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS);

        String manufacturer = Build.MANUFACTURER.toLowerCase(Locale.ROOT);
        if (manufacturer.contains("samsung")) {
            addVendorAction("ابزار Device Care سامسونگ",
                "باز کردن ابزار مراقبت از دستگاه سامسونگ، در صورت نصب بودن.",
                "com.samsung.android.lool");
        } else if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            addVendorAction("ابزار امنیت شیائومی",
                "باز کردن برنامه امنیتی رسمی گوشی، در صورت موجود بودن.",
                "com.miui.securitycenter");
        }

        addButton("بررسی دوباره گوشی", () -> showSmartOptimizer());
        addButton("بازگشت به خانه", () -> buildHome());
    }

    private String detectBrand() {
        String brand = Build.MANUFACTURER.toLowerCase(Locale.ROOT);
        String model = Build.MODEL.toLowerCase(Locale.ROOT);
        if (brand.contains("samsung")) return "Samsung Galaxy / One UI";
        if (brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") ||
            model.contains("redmi") || model.contains("poco")) return "Xiaomi / Redmi / POCO";
        if (brand.contains("google")) return "Google Pixel";
        if (brand.contains("huawei")) return "Huawei";
        if (brand.contains("honor")) return "HONOR";
        if (brand.contains("oppo")) return "OPPO";
        if (brand.contains("vivo")) return "Vivo";
        if (brand.contains("realme")) return "Realme";
        if (brand.contains("motorola")) return "Motorola";
        return Build.MANUFACTURER + " / Android";
    }

    private void addAction(String title, String description, String action) {
        LinearLayout p = panel();
        p.addView(text(title, 16, GOLD, true));
        p.addView(text(description, 13, WHITE, false));
        Button execute = button("بررسی و اجرای این اقدام");
        execute.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description + "\n\nاین اقدام تنظیمات گوشی را باز می‌کند. تغییرات را خودت انجام بده.")
            .setNegativeButton("لغو", null)
            .setPositiveButton("ادامه", (d, w) -> openSettings(action))
            .show());
        p.addView(execute);
        content.addView(p);
    }

    private void addVendorAction(String title, String description, String packageName) {
        LinearLayout p = panel();
        p.addView(text(title, 16, GOLD, true));
        p.addView(text(description, 13, WHITE, false));
        Button execute = button("باز کردن ابزار سازنده");
        execute.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description + "\n\nاجازه می‌دهی برنامه سازنده باز شود؟")
            .setNegativeButton("لغو", null)
            .setPositiveButton("باز کن", (d, w) -> {
                try {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launch != null) startActivity(launch);
                    else openSettings(android.provider.Settings.ACTION_SETTINGS);
                } catch (Exception e) {
                    openSettings(android.provider.Settings.ACTION_SETTINGS);
                }
            }).show());
        p.addView(execute);
        content.addView(p);
    }

    private void openSettings(String action) {
        try {
            startActivity(new Intent(action));
        } catch (Exception e) {
            try {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            } catch (Exception ignored) {
                new AlertDialog.Builder(this)
                    .setMessage("باز کردن تنظیمات این دستگاه ممکن نشد.")
                    .setPositiveButton("باشه", null).show();
            }
        }
    }

    private void showDashboard() {
        content.removeAllViews();
        addPageTitle("داشبورد RAM و حافظه");

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        long totalRam = 0, freeRam = 0;
        if (manager != null) {
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            totalRam = info.totalMem;
            freeRam = info.availMem;
        }
        long usedRam = Math.max(0, totalRam - freeRam);
        addPanel("حافظه RAM",
            "کل حافظه: " + formatSize(totalRam) +
            "\nمصرف‌شده: " + formatSize(usedRam) +
            "\nآزاد: " + formatSize(freeRam) +
            "\nدرصد استفاده: " + percentage(usedRam, totalRam) + "%");

        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long totalStorage = stat.getBlockSizeLong() * stat.getBlockCountLong();
        long freeStorage = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        long usedStorage = totalStorage - freeStorage;
        addPanel("حافظه داخلی",
            "کل حافظه: " + formatSize(totalStorage) +
            "\nاشغال‌شده: " + formatSize(usedStorage) +
            "\nفضای خالی: " + formatSize(freeStorage) +
            "\nدرصد استفاده: " + percentage(usedStorage, totalStorage) + "%");

        Intent batteryIntent = registerReceiver(null,
            new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String battery = "اطلاعات باتری در دسترس نیست.";
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) battery = "شارژ باتری: " + (level * 100 / scale) + "%";
        }
        addPanel("وضعیت باتری", battery);
        addButton("به‌روزرسانی اطلاعات", () -> showDashboard());
        addButton("بازگشت به خانه", () -> buildHome());
    }

    private void scanApps() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> installed;
        try {
            installed = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        } catch (Exception e) {
            content.removeAllViews();
            addPageTitle("نتیجه اسکن");
            addPanel("خطا", "دریافت فهرست برنامه‌ها ناموفق بود.");
            addButton("بازگشت به خانه", () -> buildHome());
            return;
        }

        int total = installed.size();
        ArrayList<AppEntry> flagged = new ArrayList<>();
        String[] terms = {"spy", "stealer", "keylogger", "trojan", "malware", "hacktool", "rattool"};

        for (PackageInfo pi : installed) {
            if (pi.applicationInfo == null) continue;
            String pkg = pi.packageName == null ? "" : pi.packageName.toLowerCase(Locale.ROOT);
            String label;
            try {
                label = pm.getApplicationLabel(pi.applicationInfo).toString();
            } catch (Exception e) {
                label = pkg;
            }
            String combined = (pkg + " " + label).toLowerCase(Locale.ROOT);
            for (String term : terms) {
                if (combined.contains(term)) {
                    flagged.add(new AppEntry(label, pi.packageName,
                        "نام یا شناسه مشکوک است؛ این به معنی ویروس قطعی نیست."));
                    break;
                }
            }
        }

        content.removeAllViews();
        addPageTitle("نتیجه اسکن");
        addPanel("گزارش اسکن",
            "برنامه‌های بررسی‌شده: " + total +
            "\nموارد نیازمند بررسی: " + flagged.size() +
            "\nاین اسکن ساده است و جای آنتی‌ویروس معتبر را نمی‌گیرد.");

        if (flagged.isEmpty()) {
            addPanel("نتیجه",
                "موردی بر اساس نام برنامه‌ها پیدا نشد. این نتیجه تضمین پاک بودن دستگاه نیست.");
        }
        for (AppEntry app : flagged) addAppCard(app);
        addButton("بازگشت به خانه", () -> buildHome());
    }

    private void addAppCard(AppEntry app) {
        LinearLayout p = panel();
        p.addView(text(app.label, 17, WHITE, true));
        p.addView(text(app.packageName, 12, MUTED, false));
        p.addView(text(app.reason, 13, MUTED, false));
        Button uninstall = button("بررسی و حذف برنامه");
        uninstall.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle("بررسی برنامه")
            .setMessage("این برنامه فقط به دلیل نام یا شناسه علامت‌گذاری شده است. حذف آن با خود شماست.")
            .setNegativeButton("لغو", null)
            .setPositiveButton("رفتن به حذف برنامه", (d, w) -> {
                try {
                    startActivity(new Intent(Intent.ACTION_DELETE,
                        Uri.parse("package:" + app.packageName)));
                } catch (Exception ignored) {}
            }).show());
        p.addView(uninstall);
        content.addView(p);
    }

    private void addPageTitle(String title) {
        TextView heading = text(title, 23, BLUE, true);
        heading.setGravity(Gravity.CENTER);
        heading.setShadowLayer(dp(10), 0, 0, BLUE);
        content.addView(heading);
    }

    private void addPanel(String heading, String body) {
        LinearLayout p = panel();
        p.addView(text(heading, 17, GOLD, true));
        p.addView(text(body, 14, WHITE, false));
        content.addView(p);
    }

    private void addButton(String label, Runnable action) {
        Button b = button(label);
        b.setOnClickListener(v -> action.run());
        content.addView(b);
    }

    private LinearLayout panel() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(17), dp(15), dp(17), dp(15));
        p.setBackground(makeGradient(
            new int[]{Color.argb(145, 22, 68, 112), Color.argb(100, 12, 35, 72)},
            dp(22), dp(1), Color.argb(125, 115, 215, 255), dp(5), dp(5)
        ));
        p.setElevation(dp(5));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(10), 0, dp(5));
        p.setLayoutParams(lp);
        animateEntrance(p);
        return p;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.rgb(5, 22, 43));
        b.setTextSize(14);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(makeGradient(
            new int[]{Color.rgb(100, 220, 255), Color.rgb(255, 222, 125)},
            dp(18), dp(1), Color.argb(190, 255, 255, 255), dp(4), dp(4)
        ));
        b.setElevation(dp(4));
        b.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90)
                    .setInterpolator(new DecelerateInterpolator()).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP ||
                       event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(150)
                    .setInterpolator(new DecelerateInterpolator()).start();
            }
            return false;
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(9), 0, dp(4));
        b.setLayoutParams(lp);
        animateEntrance(b);
        return b;
    }

    private GradientDrawable makeGradient(int[] colors, float radius, int strokeWidth,
                                          int strokeColor, int elevationX, int elevationY) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, colors);
        d.setCornerRadius(radius);
        if (strokeWidth > 0) d.setStroke(strokeWidth, strokeColor);
        return d;
    }

    private void animateEntrance(View view) {
        view.setAlpha(0f);
        view.setTranslationY(dp(12));
        view.animate().alpha(1f).translationY(0f).setDuration(380)
            .setInterpolator(new DecelerateInterpolator()).start();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_LOCALE);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    private String percentage(long value, long total) {
        if (total <= 0) return "0";
        return String.valueOf(Math.max(0, Math.min(100, value * 100 / total)));
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "نامشخص";
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1) return String.format(Locale.US, "%.2f GB", gb);
        double mb = bytes / (1024.0 * 1024.0);
        return String.format(Locale.US, "%.0f MB", mb);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
