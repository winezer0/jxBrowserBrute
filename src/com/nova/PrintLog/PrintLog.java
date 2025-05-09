package com.nova.PrintLog;

public class PrintLog {

    public static final String scrub(String var0) {
        return var0 == null ? null : var0.replace('\u001b', '.');
    }

    public static final void print_debug(String var0) {
        System.out.println("[*] " + scrub(var0));
    }

    public static final void print_info(String var0) {
        System.out.println("[+] " + scrub(var0));
    }

    public static final void print_error(String var0) {
        System.err.println("[-] " + scrub(var0));
    }

}



