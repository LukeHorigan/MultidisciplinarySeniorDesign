package com.example.myapplication;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
//import android.demo.entities.Product;
import android.graphics.Typeface;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import android.view.View;

import java.util.Date;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private final static String TAG = MainActivity.class.getSimpleName();

    TextView infoIp, infoPort;
    Button button;
    static final int UdpServerPORT = 8888;
    UdpServerThread udpServerThread;

    int NumberOfModuless = 10;

    TextView[][] views  = new TextView[NumberOfModuless][5];
    Modules modules[] = new Modules[NumberOfModuless];
    Boolean init = false;
    int initModules =0;
    boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoIp =  findViewById(R.id.infoip);
        infoPort =  findViewById(R.id.infoport);

        button =  findViewById(R.id.button_send);
        infoIp.setText(getIpAddress());
        infoPort.setText("Port Number: " + (UdpServerPORT));
        button =  findViewById(R.id.button_send);
        initView();
        loadData();
        initModules();

        if (button != null) {
            button.setOnClickListener((View.OnClickListener)(new View.OnClickListener() {
                public final void onClick(View it) {
                  initModules();
                }
            }));
        }
    }

    @Override
    protected void onStart() {
        udpServerThread = new UdpServerThread(UdpServerPORT);
        udpServerThread.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (udpServerThread != null) {
            udpServerThread.setRunning(false);
            udpServerThread = null;
        }

        super.onStop();
    }


    private class UdpServerThread extends Thread {

        int serverPort;
        DatagramSocket socket;

        boolean running;

        public UdpServerThread(int serverPort) {
            super();
            this.serverPort = serverPort;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {

            running = true;
            int module;
            String received;
            int command;

            try {
                socket = new DatagramSocket(serverPort);
                Log.e(TAG, "UDP Server is running");
                while (running) {
                    byte[] buf = new byte[256];

                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);     //this code block the program flow

//                  // send the response to the client at "address" and "port"
                    InetAddress address = packet.getAddress();
                    int port = packet.getPort();
                        received= new String(packet.getData(), 0, 1);
                        module = Integer.parseInt(received.trim());
                        if (module > 0 && module <= NumberOfModuless) {
                            modules[module-1].setPort(port);
                            modules[module-1].setIP(address);
                            modules[module-1].setModule(module);
                            received= new String(packet.getData(), 1, 1);
                            command = Integer.parseInt(received.trim());
                            received = new String(packet.getData(), 2, packet.getLength());
                            views[module-1][1].setText("Online");
                            if(command == 1) {
                                views[module - 1][2].setText(received);
                            }
                            else{
                                views[module-1][3].setText("Bumped");
                            }
                        }
                //  else {
//                        // continue reading the data
//                        received= new String(packet.getData(), 0, 1);
//                        module = Integer.parseInt(received.trim());
//                        received= new String(packet.getData(), 2, 2);
//
////                      ToF = Integer.parseInt(received.trim());
//                    }
//                    if(module == 1){
//                        // then the data is from module 1 Now check for Data ID
//                        received= new String(packet.getData(), 2, 2);
//                        ToF = Integer.parseInt(received.trim());
//                        if(ToF == 1) {
//                        //    updatePrompt("Module 1 has been initialized successfully\n");
//                        }else{
//                           // updatePrompt("Module 1 failed to initialize ToF Sensor\n");
//                        }
//                    }
//                    else if(module ==2){
//                        received= new String(packet.getData(), 2, 2);
//                        DataId = Integer.parseInt(received.trim());
//                        received= new String(packet.getData(), 5, 2);
//                        Data = Integer.parseInt(received.trim());
//                        updatePrompt2("Module " + module + " DataId " + DataId + " Data " + Data  + "\n");
//                    }
              //    }
                }

                Log.e(TAG, "UDP Server ended");

            }  catch (IOException e) {
                e.printStackTrace();
            }  finally {
                if (socket != null) {
                    socket.close();
                    Log.e(TAG, "socket.close()");
                }
            }
        }
    }


    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip = "Local IP address: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e + "\n";
        }

        return ip;
    }

    private void initView() {
        tableLayout = (TableLayout) findViewById(R.id.tableLayoutProduct);
    }

    private void loadData() {
        createColumns();
        InitColumns();
    }

    private void createColumns() {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Id Column
        TextView textViewModule = new TextView(this);
        textViewModule.setText("Module");
        textViewModule.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewModule.setPadding(5, 5, 5, 0);
        textViewModule.setHeight(30);
        textViewModule.setWidth(100);
        tableRow.addView(textViewModule);

        // Name Column
        TextView textViewStatus = new TextView(this);
        textViewStatus.setText("Status");
        textViewStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewStatus.setPadding(5, 5, 5, 0);
        textViewStatus.setWidth(100);
        textViewStatus.setHeight(30);
        tableRow.addView(textViewStatus);

        // Price Column
        TextView textViewProximity = new TextView(this);

        textViewProximity.setWidth(100);
        textViewProximity.setHeight(30);
        textViewProximity.setText("Proximity");
        textViewProximity.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewProximity.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewProximity);


        TextView textViewPIR = new TextView(this);

        textViewPIR.setWidth(100);
        textViewPIR.setHeight(30);
        textViewPIR.setText("Accelerometer");
        textViewPIR.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewPIR.setPadding(5, 5, 5, 0);
        tableRow.addView(textViewPIR);



        // Photo Column
        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Add Divider
        tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        // Id Column
        textViewModule = new TextView(this);
        textViewModule.setText("-----------");
        textViewModule.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewModule.setPadding(5, 5, 5, 0);
        textViewModule.setWidth(70);
        textViewModule.setHeight(30);
        tableRow.addView(textViewModule);


        // Name Column
        textViewStatus = new TextView(this);
        textViewStatus.setText("-----------");
        textViewStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewStatus.setPadding(5, 5, 5, 0);
        textViewStatus.setWidth(70);
        textViewStatus.setHeight(30);
        tableRow.addView(textViewStatus);


        // Price Column
        textViewProximity = new TextView(this);
        textViewProximity.setText("-----------");
        textViewProximity.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewProximity.setPadding(5, 5, 5, 0);
        textViewProximity.setWidth(70);
        textViewProximity.setHeight(30);
        tableRow.addView(textViewProximity);


        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.FILL_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private void InitColumns() {
        for (int i =0; i<NumberOfModuless;i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.MATCH_PARENT));

            views[i][0] = new TextView(this);
            views[i][0].setText(String.valueOf(i+1));
            views[i][0].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            views[i][0].setPadding(5, 5, 5, 0);
            views[i][0].setWidth(70);
            views[i][0].setHeight(30);
            views[i][0].setGravity(10);
            tableRow.addView(views[i][0]);

            views[i][1] = new TextView(this);
            views[i][1].setText("UnKnown");
            views[i][1].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            views[i][1].setPadding(5, 5, 5, 0);
            views[i][1].setWidth(70);
            views[i][1].setHeight(30);
            tableRow.addView(views[i][1]);

            views[i][2] = new TextView(this);
            views[i][2].setText("________");
            views[i][2].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            views[i][2].setPadding(5, 5, 5, 0);
            views[i][2].setWidth(70);
            views[i][2].setHeight(30);
            tableRow.addView(views[i][2]);

            views[i][3] = new TextView(this);
            views[i][3].setText("_______");
            views[i][3].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            views[i][3].setPadding(5, 5, 5, 0);
            views[i][3].setWidth(70);
            views[i][3].setHeight(30);
            tableRow.addView(views[i][3]);


            tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        }
    }


    public void initModules(){
        for(int i = 0; i<NumberOfModuless;i++){
            modules[i] = new Modules();
            modules[i].setModule(-1);
        }
        reset();
    }

    public void reset(){
        for (int i =0; i<NumberOfModuless;i++) {
            views[i][1].setText("UnKnown");
            views[i][2].setText("________");
            views[i][3].setText("_______");
        }
    }
}
