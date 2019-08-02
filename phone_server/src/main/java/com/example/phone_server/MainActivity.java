package com.example.phone_server;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.phone_server.http.NIOHttpServer;
import com.example.phone_server.list.SlideLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0x1001;

    private WebView webView;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NIOHttpServer.getInstance().init(MainActivity.this);

        init();
    }

    private boolean checkSelfPermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int isGranted = ContextCompat.checkSelfPermission(this, permission);
        //检查权限 是否授权
        if (isGranted != PackageManager.PERMISSION_GRANTED) {
            //未授权的
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
            return false;
        } else {
            //已授权
            return true;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        textView = findViewById(R.id.text);
        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        ListView listView = findViewById(R.id.list);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission()) {
                    sendRequestWithHttpURLConnection();
                }
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NIOHttpServer.getInstance().onDestroy();
            }
        });

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @SuppressLint("DefaultLocale")
            @Override
            public String getItem(int position) {
                return String.format("item - %d", position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            SlideLayout mSlideLayout;

            @SuppressLint("DefaultLocale")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ViewHolder holder;
                if(convertView == null){
                    holder = new ViewHolder();
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_list, parent, false);
                    holder.slideLayout = convertView.findViewById(R.id.slide_layout);
                    holder.textView = convertView.findViewById(R.id.item_name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.textView.setText(String.format("item - %d", position));
                holder.slideLayout.setOnSlideChangeListener(new SlideLayout.onSlideChangeListener() {
                    @Override
                    public void onMenuOpen(SlideLayout slideLayout) {
                        mSlideLayout = slideLayout;
                    }

                    @Override
                    public void onMenuClose(SlideLayout slideLayout) {
                        if(mSlideLayout != null){
                            mSlideLayout = null;
                        }
                    }

                    @Override
                    public void onClick(SlideLayout slideLayout) {
                        if(mSlideLayout != null){
                            mSlideLayout.closeMenu();
                        }
                    }
                });
                return convertView;
            }

            class ViewHolder {
                SlideLayout slideLayout;
                TextView textView;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(checkSelfPermission()){
//            File mTransferFile = new File(Environment.getExternalStorageDirectory() + File.separator + "WifiTransfer");
//            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTransferFile.list());
//            listView.setAdapter(adapter);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //用户给APP授权的结果
        //判断grantResults是否已全部授权，如果是，执行相应操作，如果否，提醒开启权限
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "权限已申请", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "权限已拒绝", Toast.LENGTH_SHORT).show();
            }
            List<String> deniedPermission = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    deniedPermission.add(permissions[i]);
                }
            }
            //已全部授权
            if (deniedPermission.size() == 0) {
                return;
            }
            showMissingPermissionDialog();
        }
    }

    //// 显示缺失权限提示
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("帮助");
        builder.setMessage("当前应用缺少必要的权限，请打开 “设置” -> “权限” 打开所需权限");
        // 拒绝, 退出应用
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip = getIpAddressString();
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    final URL url = new URL("http://" + ip + ":54321/");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.connect();

                    //此时获取的是字节流
                    InputStream in = connection.getInputStream();
                    //对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in)); //将字节流转化成字符流
                    final StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadData(response.toString(), "text/html", "utf-8");
                            textView.setText(String.format("请求地址：http://%s:54321", url.getHost()));
                        }
                    });
                    Log.d("InetAddress", "请求数据：" + response.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public static String getIpAddressString() {
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
