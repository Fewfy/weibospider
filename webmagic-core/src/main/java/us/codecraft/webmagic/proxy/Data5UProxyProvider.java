package us.codecraft.webmagic.proxy;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Task;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 　　　　　　　　┏┓　　　┏┓+ +
 * 　　　　　　　┏┛┻━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 ████━████ ┃+
 * 　　　　　　　┃　　　　　　　┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃ + +
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃ + + + +
 * 　　　　　　　　　┃　　　┃　　　　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ + 　　　　神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　　┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛+ + + +
 * Created by Imgaojp on 2017/8/10.
 */
public class Data5UProxyProvider implements ProxyProvider, Runnable {
    private static CopyOnWriteArrayList<Proxy> proxies = new CopyOnWriteArrayList<Proxy>();

    public int getProxyCount() {
        return proxies.size();
    }

    @Override
    public void returnProxy(Proxy proxy, Page page, Task task) {

    }

    @Override
    public Proxy getProxy(Task task) {
        System.out.println("一共" + proxies.size() + "个IP");
        return proxies.get((int) (Math.random() * proxies.size()));
    }

    public static void main(String[] args) {
        new Thread(new Data5UProxyProvider()).start();
    }

    @Override
    public void removeProxy(Proxy proxy) {
        proxies.remove(proxy);
    }

    @Override
    public void run() {
        while (true) {
            try {
                String order = "c878cfd7c445c6f61ee4ba9c454ce2a2";
                java.net.URL url = new java.net.URL("http://api.ip.data5u.com/dynamic/get.html?order=" + order + "&ttl");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection = (HttpURLConnection) url.openConnection();

                InputStream raw = connection.getInputStream();
                InputStream in = new BufferedInputStream(raw);
                byte[] data = new byte[in.available()];
                int bytesRead = 0;
                int offset = 0;
                while (offset < data.length) {
                    bytesRead = in.read(data, offset, data.length - offset);
                    if (bytesRead == -1) {
                        break;
                    }
                    offset += bytesRead;
                }
                in.close();
                raw.close();
                String[] res = new String(data, "UTF-8").split("\n");
                for (String ip : res) {
                    try {
                        String[] parts = ip.split(",");
                        if (Integer.parseInt(parts[1]) > 0) {
                            Proxy p = new Proxy(parts[0].split(":")[0], Integer.parseInt(parts[0].split(":")[1].trim()));
                            if (!proxies.contains(p)) {
                                proxies.add(p);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(">>>>>>>>>>>>>>获取IP出错");
            }
            try {
                long sleepMs = 5 * 1000;
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
