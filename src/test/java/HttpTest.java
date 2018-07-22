
import com.denvk.mt.StartupConfig;
import com.denvk.mt.endpoint.Runner;
import com.denvk.mt.response.StatusCode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;

/**
 * @author Denis Voroshchuk
 */
public class HttpTest {

    public HttpTest() {
    }

    @Test
    public void httpTest() throws Throwable {
        System.out.println("Start server for tests.");
        Server server = null;
        try {
            Runner r = new Runner();
            StartupConfig sc = new StartupConfig();
            Assert.assertNotNull(sc);
            server = r.createServer(sc);
            Assert.assertNotNull("server creation fails", server);
            server.start();
            httpInputTest(sc);
            svTest(sc);
        } catch (Throwable t) {
            throw t;
        } finally {
            //stop server
            if (server != null) {
                server.stop();
            }
        }
        System.out.println("Server tests are finished");
    }

    private void httpInputTest(StartupConfig sc) throws Throwable {
        String rootURL = buildRootURL(sc);
        //normal operating
        String id1 = "test1";
        String id2 = "test2";
        String value = "100.0";
        String response;
        StatusCode code;
        for (String id : new String[]{id1, id2}) {
            response = createAccount(rootURL, id, value);
            code = getStatus(response);
            Assert.assertEquals("invalid response on create request:" + response,
                    code, StatusCode.OK);

            response = getAccount(rootURL, id);
            code = getStatus(response);
            Assert.assertEquals("invalid response on get request:" + response,
                    code, StatusCode.OK);
        }
        response = transfer(rootURL, id1, id2, value);
        code = getStatus(response);
        Assert.assertEquals("invalid response on transfer request:" + response,
                code, StatusCode.OK);
        //test invalid input.
        String id3 = "test3";
        value = "100,20";
        try {
            response = createAccount(rootURL, id3, value);
            Assert.fail("incorrect value was parsed:" + value);
        } catch (Throwable t) {
        }
    }

    private static String buildRootURL(StartupConfig sc) {
        return "http://localhost:" + sc.getPort() + "/accounts/";
    }

    private static String createAccount(String rootURL, String id, String value) throws Throwable {
        String response = sendRequest(rootURL + "create/" + id + "/" + value);
        return response;
    }

    private static String getAccount(String rootURL, String id) throws Throwable {
        String response = sendRequest(rootURL + "/" + id);
        return response;
    }

    private static String transfer(String rootURL, String id1, String id2, String amount) throws Throwable {
        String response = sendRequest(rootURL + "/" + id1 + "/transfer/" + id2 + "/" + amount);
        return response;
    }

    private static final String STATUS_BEGIN = "\"code\":\"";
    private static final String STATUS_END = "\"";

    private static StatusCode getStatus(String response) {
        //fastest way to get status:
        int begin = response.indexOf(STATUS_BEGIN);
        if (begin == -1) {
            return null;
        }
        begin += STATUS_BEGIN.length();
        int end = response.indexOf(STATUS_END, begin + 1);
        if (end == -1) {
            return null;
        }
        response = response.substring(begin, end);
        StatusCode code = StatusCode.valueOf(response);
        return code;
    }

    private static String sendRequest(String urlStr) throws Throwable {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.connect();
        Assert.assertEquals("http response code is not 200", 200, conn.getResponseCode());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return br.lines().parallel().collect(Collectors.joining("\n"));
        }
    }

    private void svTest(StartupConfig sc) 
            throws InterruptedException {
        int clients = 10;
        int hackers = 2;
        System.out.println("Begin SVT for "+clients+" clients and "+hackers+" \"hackers\"");
        int operationsPerUser = 10000;
        int operationsPerHacker = -1;//infinite.
        double initPerUser = 1000;

        Thread[] clientThreads = new Thread[clients];
        Thread[] hackerThreads = new Thread[hackers];
        for (int i = 0; i < clientThreads.length; i++) {
            clientThreads[i] = new Thread(new Client(i, initPerUser, operationsPerUser, sc));
        }
        //start hackers
        for (int i = 0; i < hackerThreads.length; i++) {
            hackerThreads[i] = new Thread(new Hacker(i+1,operationsPerHacker, sc,0));
        }
        for (Thread hackerThread : hackerThreads) {
            hackerThread.start();
        }
        //start and wait clients
        for (Thread clientThread : clientThreads) {
            clientThread.start();
        }
        for (Thread clientThread : clientThreads) {
            clientThread.join();
        }
        //stop hackers
        for (Thread hackerThread : hackerThreads) {
            hackerThread.interrupt();
        }
        for (Thread hackerThread : hackerThreads) {
            hackerThread.join();
        }
        System.out.println("SVT finished");
    }

    private abstract static class User implements Runnable {

        protected final int requestCount;
        protected final StartupConfig sc;
        protected final String rootURL;
        protected final String id;

        public User(int id,int requestCount, StartupConfig sc) {
            this.requestCount = requestCount;
            this.sc = sc;
            this.rootURL = buildRootURL(sc);
            this.id = "client" + id;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            int fails = 0;
            int i = 0;
            for (; requestCount == -1 || i < requestCount; i++) {
                try {
                    if (!sendRequest(i)) {
                        //interrupted or task is finished
                        break;
                    }
                } catch(ConnectException e) {
                    fails++;
                } catch (Throwable t) {
                    t.printStackTrace();
                    Assert.fail(this.getClass().getSimpleName() + " test fail. Request number " + i);
                }
            }
            start = System.currentTimeMillis() - start;
            System.out.println(this.getClass().getSimpleName() + " report: execution time:" + start + "(ms),requests count:" + i+",connection refused times:"+fails);
        }

        public abstract boolean sendRequest(int i) throws Throwable;
    }

    private static class Hacker extends User {
        
        private final String partnerId;
        
        public Hacker(int id,int requestCount, StartupConfig sc,int badGuysAccountId) {
            super(id,requestCount, sc);
            partnerId = "client"+badGuysAccountId;
        }

        @Override
        public boolean sendRequest(int i) throws Throwable {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }
            String r = transfer(rootURL, id, partnerId, String.valueOf(1000000));
            StatusCode c = getStatus(r);
            Assert.assertNotEquals("Hack error.", StatusCode.OK,c);
            Assert.assertTrue("Hacker get invalid response:"+r,StatusCode.NOT_ENOUGH_FUNDS.equals(c) 
                    || StatusCode.NOT_FOUND.equals(c) || StatusCode.SAME_ACCOUNT_TRANSFER.equals(c));
            return true;
        }
    }

    private static class Client extends User {
        private final String init;
        private final String partnerId;

        public Client(int id, double init, int requestCount, StartupConfig sc) {
            super(id,requestCount, sc);
            this.init = String.valueOf(init);
            this.partnerId = "partner" +id;
        }

        @Override
        public boolean sendRequest(int i) throws Throwable {
            StatusCode c;
            String r;
            if (i == 0) {
                r = createAccount(rootURL, id, init);
                c = getStatus(r);
                Assert.assertEquals("Account creation error.", StatusCode.OK,c);
                r = createAccount(rootURL, partnerId, "0");
                c = getStatus(r);
                Assert.assertEquals("Account creation error.", StatusCode.OK,c);
            }
            r = transfer(rootURL, id, partnerId, init);
            c = getStatus(r);
            Assert.assertEquals("Transfer error.", StatusCode.OK,c);
            r = transfer(rootURL, partnerId, id, init);
            c = getStatus(r);
            Assert.assertEquals("Transfer error.", StatusCode.OK,c);
            return true;
        }
    }
}
