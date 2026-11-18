package com.parsa.androidboost;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout content;
    private static final int BG = Color.rgb(8,13,18), PANEL = Color.rgb(20,29,37);
    private static final int GREEN = Color.rgb(0,230,118), WHITE = Color.WHITE, MUTED = Color.rgb(180,195,205);

    static class AppEntry {
        String label, packageName, reason;
        AppEntry(String l, String p, String r) { label=l; packageName=p; reason=r; }
    }

    @Override public void onCreate(Bundle state) { super.onCreate(state); buildUi(); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(24), dp(20), dp(24));
        scroll.addView(content); setContentView(scroll);

        TextView title = text("ANDROID BOOST",26,GREEN,true); title.setGravity(Gravity.CENTER); content.addView(title);
        TextView subtitle = text("USB Device Optimizer • نسخه آزمایشی ۰.۱",13,MUTED,false); subtitle.setGravity(Gravity.CENTER); content.addView(subtitle);
        addPanel("اطلاعات دستگاه", "برند: " + Build.MANUFACTURER + "\nمدل: " + Build.MODEL + "\nنسخه اندروید: " + Build.VERSION.RELEASE + "\nSDK: " + Build.VERSION.SDK_INT);
        Button scan = button("شروع اسکن برنامه‌ها"); scan.setOnClickListener(v -> scanApps()); content.addView(scan);
        Button tips = button("بهینه‌سازی سرعت (راهنمای امن)"); tips.setOnClickListener(v -> showOptimizationTips()); content.addView(tips);
        addPanel("وضعیت", "این نسخه فقط بررسی اولیه انجام می‌دهد. هیچ برنامه‌ای خودکار حذف نمی‌شود و تغییر سیستمی انجام نخواهد شد.");
    }

    private void scanApps() {
        PackageManager pm=getPackageManager(); List<PackageInfo> installed;
        try { installed=pm.getInstalledPackages(PackageManager.GET_PERMISSIONS); }
        catch(Exception e){ addPanel("خطا","دریافت فهرست برنامه‌ها ناموفق بود."); return; }
        int total=installed.size(); java.util.ArrayList<AppEntry> flagged=new java.util.ArrayList<>();
        for(PackageInfo pi:installed) {
            if(pi.applicationInfo==null) continue;
            String pkg=pi.packageName==null?"":pi.packageName.toLowerCase(Locale.ROOT), label;
            try { label=pm.getApplicationLabel(pi.applicationInfo).toString(); } catch(Exception e){ label=pkg; }
            String s=(pkg+" "+label).toLowerCase(Locale.ROOT);
            String[] terms={"spy","stealer","keylogger","trojan","malware","hacktool","rattool"};
            for(String term:terms) if(s.contains(term)){ flagged.add(new AppEntry(label,pi.packageName,"نام یا شناسه مشکوک است؛ این به معنی ویروس قطعی نیست.")); break; }
        }
        content.removeAllViews();
        TextView title=text("ANDROID BOOST",26,GREEN,true); title.setGravity(Gravity.CENTER); content.addView(title);
        addPanel("نتیجه اسکن","برنامه‌های بررسی‌شده: "+total+"\nموارد نیازمند بررسی: "+flagged.size()+"\nاین اسکن ساده است و جای آنتی‌ویروس معتبر را نمی‌گیرد.");
        if(flagged.isEmpty()) addPanel("نتیجه","بر اساس بررسی ساده نام برنامه‌ها، موردی پیدا نشد. این نتیجه تضمین پاک بودن دستگاه نیست.");
        for(AppEntry app:flagged) addAppCard(app);
        Button rescan=button("اسکن دوباره"); rescan.setOnClickListener(v->scanApps()); content.addView(rescan);
        Button back=button("بازگشت"); back.setOnClickListener(v->buildUi()); content.addView(back);
    }

    private void addAppCard(AppEntry app) {
        LinearLayout p=panel(); p.addView(text(app.label,18,WHITE,true)); p.addView(text(app.packageName,12,MUTED,false)); p.addView(text(app.reason,13,MUTED,false));
        Button uninstall=button("باز کردن صفحه حذف برنامه"); uninstall.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("بررسی برنامه").setMessage("این برنامه فقط به دلیل نام/شناسه علامت‌گذاری شده است. حذف آن با خود شماست.").setNegativeButton("لغو",null).setPositiveButton("رفتن به حذف برنامه",(d,w)->{ try{startActivity(new Intent(Intent.ACTION_DELETE,Uri.parse("package:"+app.packageName)));}catch(Exception ignored){} }).show());
        p.addView(uninstall); content.addView(p);
    }

    private void showOptimizationTips() {
        new AlertDialog.Builder(this).setTitle("بهینه‌سازی امن").setMessage("• انیمیشن‌ها را در گزینه‌های توسعه‌دهنده روی ۰.۵x تنظیم کنید.\n• برنامه‌های بلااستفاده را حذف یا محدود کنید.\n• فضای خالی کافی نگه دارید.\n• پیام‌رسان‌های ضروری را خواب عمیق نگذارید.\n\nاین نسخه هنوز تنظیمات سیستم را خودکار تغییر نمی‌دهد.").setPositiveButton("متوجه شدم",null).show();
    }
    private void addPanel(String heading,String body){LinearLayout p=panel();p.addView(text(heading,17,GREEN,true));p.addView(text(body,14,WHITE,false));content.addView(p);}
    private LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(16),dp(14),dp(16),dp(14));p.setBackgroundColor(PANEL);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(14),0,dp(4));p.setLayoutParams(lp);return p;}
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(Color.BLACK);b.setBackgroundColor(GREEN);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,0);b.setLayoutParams(lp);return b;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,android.graphics.Typeface.BOLD);t.setPadding(0,dp(4),0,dp(4));return t;}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+0.5f);}
}
