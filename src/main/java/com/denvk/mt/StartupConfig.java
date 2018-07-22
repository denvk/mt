package com.denvk.mt;

/**
 * @author Denis Voroshchuk
 */
public class StartupConfig {

    private int port = 8080;

    public int getPort() {
        return port;
    }

    public static StartupConfig load(String[] args) {
        StartupConfig sc = new StartupConfig();
        for(int i = 0;i<args.length;i++) {
            if("-help".equals(args[i]) || "/?".equals(args[i])) {
                return null;
            }
            if("-port".equals(args[i])) {
                if(i+1 >= args.length || !args[i+1].matches("[0-9]+")) {
                    return null;
                }
                sc.port = Integer.parseInt(args[i+1]);
            }
        }
        return sc;
    }
    
    
}
