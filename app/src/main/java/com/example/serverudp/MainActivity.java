package com.example.serverudp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class MainActivity extends Activity {
    public static TextView label, content;
    Socket socket = null;
    private String selfIp = "";
    static DatagramSocket udpSocket = null;
    static DatagramPacket udpPacket = null;

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                content.append("client: " + msg.obj + "\n");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        content = (TextView) findViewById(R.id.content);
        label = (TextView) findViewById(R.id.label);
        label.append("\n");
        new Thread(new Runnable() {

            @Override
            public void run() {
                byte[] data = new byte[256];
                try {
                    udpSocket = new DatagramSocket(43708);
                    udpPacket = new DatagramPacket(data, data.length);
                } catch (SocketException e1) {
                    e1.printStackTrace();
                }
                while (true) {

                    try {
                        udpSocket.receive(udpPacket);
                    } catch (Exception e) {
                    }
                    if (null != udpPacket.getAddress()) {
                        final String quest_ip = udpPacket.getAddress().toString();
                        final String codeString = new String(data, 0, udpPacket.getLength());
                        label.post(new Runnable() {

                            @Override
                            public void run() {
                                label.append("收到来自：" + quest_ip + "UDP请求。。。\n");
                                label.append("请求内容：" + codeString + "\n\n");

                            }
                        });
                        try {
                            final String ip = udpPacket.getAddress().toString().substring(1);
                            label.post(new Runnable() {

                                @Override
                                public void run() {
                                    label.append("发送socket请求到：" + ip + "\n");

                                }
                            });
                            socket = new Socket(ip, 8080);


                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (null != socket) {
                                    socket.close();
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }).start();


        new Thread() {
            public void run() {
                OutputStream output;
                String serverContent = "服务器收到消息";
                try {
                    ServerSocket serverSocket = new ServerSocket(30000);
                    while (true) {
                        Message msg = new Message();
                        msg.what = 1;
                        try {
                            Socket socketTCP = serverSocket.accept();
                            //向client发送消息
                            output = socketTCP.getOutputStream();
                            output.write(serverContent.getBytes("utf-8"));
                            output.flush();
                            socketTCP.shutdownOutput();

                            //获取输入信息
                            BufferedReader bff = new BufferedReader(new InputStreamReader

                                    (socketTCP.getInputStream()));
                            //读取信息
                            String result = "";
                            String buffer = "";
                            while ((buffer = bff.readLine()) != null) {
                                result = result + buffer;
                            }
                            msg.obj = result.toString();
                            handler.sendMessage(msg);
                            bff.close();
                            output.close();
                            socketTCP.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            ;
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        udpSocket.close();
        super.onBackPressed();
    }
}
