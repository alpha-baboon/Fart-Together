package com.example.raftrumble;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final UUID APP_UUID = UUID.fromString("9d3a7f20-1b2c-4d5e-8f60-123456789abc");
    private BluetoothAdapter adapter;
    private TextView status;
    private GameView game;
    private BluetoothSocket socket;
    private OutputStream out;
    private volatile boolean connected = false;
    private volatile boolean host = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        adapter = BluetoothAdapter.getDefaultAdapter();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xff102b3f);
        status = new TextView(this);
        status.setText("RAFT RUMBLE  •  Solo practice");
        status.setTextColor(0xffffffff);
        status.setGravity(Gravity.CENTER);
        status.setPadding(8, 10, 8, 10);
        layout.addView(status);
        game = new GameView(this, this);
        layout.addView(game, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout controls = new LinearLayout(this);
        Button create = new Button(this); create.setText("HOST GAME");
        Button join = new Button(this); join.setText("JOIN PAIRED");
        controls.addView(create, new LinearLayout.LayoutParams(0, -2, 1));
        controls.addView(join, new LinearLayout.LayoutParams(0, -2, 1));
        layout.addView(controls);
        setContentView(layout);
        create.setOnClickListener(v -> startHost());
        join.setOnClickListener(v -> joinPaired());
        if (adapter == null) status.setText("Bluetooth is not available on this phone.");
    }

    private boolean permissions() {
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 7);
            return false;
        }
        return true;
    }
    private void startHost() {
        if (!permissions() || adapter == null) return;
        new Thread(() -> {
            try {
                host = true;
                BluetoothServerSocket server = adapter.listenUsingRfcommWithServiceRecord("RaftRumble", APP_UUID);
                runOnUiThread(() -> status.setText("Hosting — pair phones in Android Bluetooth settings, then wait…"));
                socket = server.accept(); server.close(); setupSocket();
                runOnUiThread(() -> status.setText("Player connected! You are the raft captain."));
                readLoop();
            } catch (Exception e) { runOnUiThread(() -> status.setText("Host stopped: " + e.getMessage())); }
        }).start();
    }
    private void joinPaired() {
        if (!permissions() || adapter == null) return;
        new Thread(() -> {
            try {
                if (!adapter.isEnabled()) throw new Exception("Turn Bluetooth on first.");
                BluetoothDevice target = null;
                for (BluetoothDevice d : adapter.getBondedDevices()) {
                    if (d.getName() != null && d.getName().toLowerCase().contains("raft")) { target = d; break; }
                }
                if (target == null) {
                    runOnUiThread(() -> status.setText("Pair the host phone first in Android Bluetooth settings. Then tap JOIN PAIRED."));
                    return;
                }
                socket = target.createRfcommSocketToServiceRecord(APP_UUID);
                socket.connect(); host = false; setupSocket();
                runOnUiThread(() -> status.setText("Connected! Drag on your half to propel the raft."));
                readLoop();
            } catch (Exception e) { runOnUiThread(() -> status.setText("Join failed: " + e.getMessage())); }
        }).start();
    }
    private void setupSocket() throws Exception { out = socket.getOutputStream(); connected = true; }
    synchronized void sendThrust(float x, float y) {
        if (!connected || host || out == null) return;
        try { out.write((x + "," + y + "\n").getBytes()); out.flush(); } catch (Exception ignored) {}
    }
    private void readLoop() {
        try {
            InputStream in = socket.getInputStream();
            byte[] buf = new byte[128];
            while (connected) {
                int n = in.read(buf); if (n < 0) break;
                String s = new String(buf, 0, n).trim();
                String[] p = s.split(",");
                if (p.length == 2) {
                    float x = Float.parseFloat(p[0]), y = Float.parseFloat(p[1]);
                    if (host) game.addRemote(x, y);
                    else game.setRemoteState(x, y);
                }
            }
        } catch (Exception ignored) {}
        connected = false;
        runOnUiThread(() -> status.setText("Bluetooth connection ended."));
    }
    void publishState(float x, float y) {
        if (!connected || !host || out == null) return;
        try { out.write(("S," + x + "," + y + "\n").getBytes()); out.flush(); } catch (Exception ignored) {}
    }
    @Override public void onRequestPermissionsResult(int r, String[] p, int[] g) { super.onRequestPermissionsResult(r,p,g); }
    @Override protected void onDestroy() { connected = false; try { if(socket != null) socket.close(); } catch(Exception ignored){} super.onDestroy(); }
}
