package com.example.myapplication;

import java.net.InetAddress;

public class Modules {

    private int module; // private = restricted access
    private InetAddress address ;
    private int port ;


    // Getter


    public InetAddress getIP() {
        return address;
    }

    public int getModule() {
        return module;
    }

    public int getPort() {
        return port;
    }

    // Setter
    // Setter
    public void setIP(InetAddress newName) {
        this.address = newName;
    }
    public void setModule(int newName) {
        this.module = newName;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
