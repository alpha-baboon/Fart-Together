package com.example.raftrumble;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.bluetooth.*;
import android.os.Build;
import android.widget.*;
import android.view.Gravity;
import java.io.*;
import java.util.UUID;

public class MainActivity extends Activity {

    private static final UUID APP_UUID =
        UUID.fromString("9d3a7f20-1b2c-4d5e-8f60-123456789abc");

    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private BluetoothServerSocket server;
    private OutputStream output;
    private TextView status;
    private GameView game;

    private volatile boolean connected = false;
    private volatile boolean host = false;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        adapter = BluetoothAdapter.getDefaultAdapter();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setBackgroundColor(0xff102b3f);

        status = new TextView(this);
        status.setText("FART TOGETHER - SOLO MODE");
        status.setTextColor(0xffffffff);
        status.setGravity(Gravity.CENTER);
        layout.addView(status);

        game = new GameView(this, this);
        layout.addView(game,
            new LinearLayout.LayoutParams(-1, 0, 1));

        Button create = new Button(this);
        create.setText("HOST GAME");

        Button join = new Button(this);
        join.setText("JOIN GAME");

        layout.addView(create);
        layout.addView(join);

        setContentView(layout);

        create.setOnClickListener(v -> startHost());
        join.setOnClickListener(v -> joinGame());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            }, 10);

            return false;
        }
        return true;
    }

    private void startHost() {
        if (!checkPermission() || adapter == null) return;

        new Thread(() -> {
            try {
                host = true;

                server = adapter.listenUsingRfcommWithServiceRecord(
                    "FartTogether", APP_UUID);

                runOnUiThread(() ->
                    status.setText("Waiting for player..."));

                socket = server.accept();
                server.close();

                setupConnection();

                runOnUiThread(() ->
                    status.setText("PLAYER CONNECTED!"));

                listen();

            } catch (Exception e) {
                updateStatus("Host error: " + e.getMessage());
            }
        }).start();
    }

    private void joinGame() {
        if (!checkPermission() || adapter == null) return;

        new Thread(() -> {
            try {
                if (!adapter.isEnabled())
                    throw new Exception("Enable Bluetooth first");

                BluetoothDevice device = null;

                for (BluetoothDevice d : adapter.getBondedDevices()) {
                    device = d;
                    break;
                }

                if (device == null)
                    throw new Exception(
                        "Pair with host in Bluetooth settings first");

                socket = device.createRfcommSocketToServiceRecord(APP_UUID);
                socket.connect();

                host = false;
                setupConnection();

                updateStatus("CONNECTED!");

                listen();

            } catch (Exception e) {
                updateStatus("Connection failed: " + e.getMessage());
            }
        }).start();
    }

    private void setupConnection() throws Exception {
        output = socket.getOutputStream();
        connected = true;
    }

    synchronized void sendThrust(float x, float y) {
        if (!connected || host || output == null) return;

        try {
            output.write(("T," + x + "," + y + "\n").getBytes());
            output.flush();
        } catch (Exception ignored) {}
    }

    private void listen() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

            String line;

            while (connected && (line = reader.readLine()) != null) {
                String[] p = line.split(",");

                if (p.length == 3) {
                    float x = Float.parseFloat(p[1]);
                    float y = Float.parseFloat(p[2]);

                    if (host && p[0].equals("T")) {
                        game.addRemote(x, y);
                    }

                    if (!host && p[0].equals("S")) {
                        game.setRemoteState(x, y);
                    }
                }
            }

        } catch (Exception ignored) {}

        connected = false;
        updateStatus("Connection closed");
    }

    synchronized void publishState(float x, float y) {
        if (!connected || !host || output == null) return;

        try {
            output.write(("S," + x + "," + y + "\n").getBytes());
            output.flush();
        } catch (Exception ignored) {}
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> status.setText(message));
    }

    @Override
    protected void onDestroy() {
        connected = false;

        try {
            if (socket != null) socket.close();
            if (server != null) server.close();
        } catch (Exception ignored) {}

        super.onDestroy();
    }
                          }
