package com.nova.BrowserUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class MyHostnameVerifier implements HostnameVerifier
{
    public boolean verify(String hostname, SSLSession session)
    {
        return true;
    }
}