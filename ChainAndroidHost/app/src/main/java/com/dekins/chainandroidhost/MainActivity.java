package com.dekins.chainandroidhost;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;

import com.dekins.chainandroidhost.utils.DownloadUtil;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.multidex.MultiDex;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dekins.chainandroidhost.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.dcloud.feature.sdk.DCSDKInitConfig;
import io.dcloud.feature.sdk.DCUniMPSDK;
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback;
import io.dcloud.feature.sdk.Interface.IUniMP;
import io.dcloud.feature.sdk.MenuActionSheetItem;
import io.dcloud.feature.unimp.config.IUniMPReleaseCallBack;
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration;
import io.dcloud.feature.unimp.config.UniMPReleaseConfiguration;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    Context mContext;
    Handler mHandler;
    /**
     * unimp小程序实例缓存
     **/
    HashMap<String, IUniMP> mUniMPCaches = new HashMap<>();

    private void initMiniAppHost() {
        mContext = this;
        mHandler = new Handler();
        //初始化 uni小程序SDK ----start----------
        MenuActionSheetItem item = new MenuActionSheetItem("关于", "gy");
        MenuActionSheetItem item1 = new MenuActionSheetItem("获取当前页面url", "hqdqym");
        MenuActionSheetItem item2 = new MenuActionSheetItem("跳转到宿主原生测试页面", "gotoTestPage");
        List<MenuActionSheetItem> sheetItems = new ArrayList<>();
        sheetItems.add(item);
        sheetItems.add(item1);
        sheetItems.add(item2);
        Log.i("unimp", "onCreate----");
        DCSDKInitConfig config = new DCSDKInitConfig.Builder()
                .setCapsule(true)
                .setMenuDefFontSize("16px")
                .setMenuDefFontColor("#ff00ff")
                .setMenuDefFontWeight("normal")
                .setMenuActionSheetItems(sheetItems)
                .setEnableBackground(false)//开启后台运行
                .build();
        DCUniMPSDK.getInstance().initialize(this, config, new IDCUniMPPreInitCallback() {
            @Override
            public void onInitFinished(boolean b) {
                Log.i("unimp", "onInitFinished----" + b);
            }
        });
        //初始化 uni小程序SDK ----end----------
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initMiniAppHost();
        Button btnInSide = findViewById(R.id.btnInSide);
        Button btnOutSide = findViewById(R.id.btnOutSide);
        btnInSide.setOnClickListener(view -> {
            try {
                UniMPOpenConfiguration uniMPOpenConfiguration = new UniMPOpenConfiguration();
                uniMPOpenConfiguration.splashClass = SplashHost.class;
                IUniMP uniMP = DCUniMPSDK.getInstance().openUniMP(mContext, "__UNI__04E3A11", uniMPOpenConfiguration);
                mUniMPCaches.put(uniMP.getAppid(), uniMP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        btnOutSide.setOnClickListener(view -> {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1002);
            updateWgt();
        });
//
//        setSupportActionBar(binding.toolbar);
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//
    }

    /**
     * 模拟更新wgt
     */
    private void updateWgt() {
        final String wgtUrl = "https://raw.githubusercontent.com/dekinsq/awesome-aws/main/__UNI__EA6CD15.wgt";
        final String wgtName = "__UNI__EA6CD15.wgt";
        String downFilePath = getExternalCacheDir().getPath();
        Handler uiHandler = new Handler();
        DownloadUtil.get().download(MainActivity.this, wgtUrl, downFilePath, wgtName, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Log.e("unimp", "onDownloadSuccess --- file === " + file.getPath());
                Log.e("unimp", "onDownloadSuccess --- file length === " + file.length());
                UniMPReleaseConfiguration uniMPReleaseConfiguration = new UniMPReleaseConfiguration();
                uniMPReleaseConfiguration.wgtPath = file.getPath();
                uniMPReleaseConfiguration.password = "789456123";
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        DCUniMPSDK.getInstance().releaseWgtToRunPath("__UNI__EA6CD15", uniMPReleaseConfiguration, new IUniMPReleaseCallBack() {
                            @Override
                            public void onCallBack(int code, Object pArgs) {
                                Log.e("unimp", "code ---  " + code + "  pArgs --" + pArgs);
                                if (code == 1) {
                                    //释放wgt完成
                                    try {
                                        UniMPOpenConfiguration uniMPOpenConfiguration = new UniMPOpenConfiguration();
                                        uniMPOpenConfiguration.splashClass = SplashHost.class;
                                        DCUniMPSDK.getInstance().openUniMP(MainActivity.this, "__UNI__EA6CD15",uniMPOpenConfiguration);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    //释放wgt失败
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {
                Log.i("unimp progress", String.valueOf(progress));
            }

            @Override
            public void onDownloadFailed() {
                Log.e("unimp", "downFilePath  ===  onDownloadFailed");
            }
        });
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

}