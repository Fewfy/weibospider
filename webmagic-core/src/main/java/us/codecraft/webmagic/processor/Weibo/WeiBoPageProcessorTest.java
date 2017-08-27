package us.codecraft.webmagic.processor.Weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Created by Imgaojp on 2017/6/30.
 */
public class WeiBoPageProcessorTest extends WeiBoPageProcessor implements PageProcessor {
//    static String s2 = "_T_WM=b0221597c0f5fa1a75e30291f14f2cc5; H5_INDEX=0_all; H5_INDEX_TITLE=%E9%A3%8E%E6%B5%81%E5%80%9C%E5%82%A5scheme; ALF=1501365934; SCF=AgZnTTBT3WPf9Zx4hqbZ1IuhuDpEWrALGULTITNRRw4iv0LzkOc306dk5Jsb-fCAnNTtAkz3p_7b4PwqDHV8yas.; SUB=_2A250UQn-DeRhGeBN61IZ8CvNzTyIHXVXvZe2rDV6PUJbktANLWrEkW0HonFPFXGb0VdAtrqSBcNNW6a2qQ..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WFiWyF9uL8MUpmMjGg9-rCZ5JpX5o2p5NHD95Qce0571h5feKq7Ws4Dqcj3i--4i-20iKy8i--ci-zfiKnpi--fiKn7iKLhi--fiKnEi-2fMcSQ9gfL; SUHB=0gHi31zdWOb5uO; SSOLoginState=1498773934";
    //   static String s1="_T_WM=b0221597c0f5fa1a75e30291f14f2cc5; H5_INDEX=0_all; H5_INDEX_TITLE=%E9%A3%8E%E6%B5%81%E5%80%9C%E5%82%A5scheme; ALF=1501365706; SCF=AgZnTTBT3WPf9Zx4hqbZ1IuhuDpEWrALGULTITNRRw4itJEqtEDN22KYmYXIGCZC-w3q0GGwxfEiYNw-bQMtdxs.; SUB=_2A250UQiaDeRhGeBN61IZ8CvNzTyIHXVXvajSrDV6PUJbktANLRGnkW12ri1M-BLMnxTeXJQJk14RW6u8xw..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WFiWyF9uL8MUpmMjGg9-rCZ5JpX5o2p5NHD95Qce0571h5feKq7Ws4Dqcj3i--4i-20iKy8i--ci-zfiKnpi--fiKn7iKLhi--fiKnEi-2fMcSQ9gfL; SUHB=0auAddHGwt3fNn; SSOLoginState=1498773706; M_WEIBOCN_PARAMS=featurecode%3D20000180%26oid%3D4124185150295373%26luicode%3D20000061%26lfid%3D4124185150295373";
//    static String string = "_T_WM=b0221597c0f5fa1a75e30291f14f2cc5; SUB=_2A250URq2DeRhGeBN61IZ8CvNzTyIHXVXvab-rDV6PUJbkdBeLUvnkW06RBbYnhSJuPIVJYoKNxNc7imDhw..; SUHB=0auAddHGx0iY4E; SCF=AvfhTspfQT-AdJtUE9okp8o8RYiTRQgac7MovhZrOPQSpLC1lwbZ5iKsuOhlsm3bE67BBVNqyPKaC4cNe5F2a-k.; SSOLoginState=1498770150; M_WEIBOCN_PARAMS=luicode%3D20000174%26uicode%3D20000174%26featurecode%3D20000320%26fid%3Dhotword";
//    static String s3 = "_T_WM=b0221597c0f5fa1a75e30291f14f2cc5; ALF=1501365934; SCF=AgZnTTBT3WPf9Zx4hqbZ1IuhuDpEWrALGULTITNRRw4iv0LzkOc306dk5Jsb-fCAnNTtAkz3p_7b4PwqDHV8yas.; SUB=_2A250UQn-DeRhGeBN61IZ8CvNzTyIHXVXvZe2rDV6PUJbktANLWrEkW0HonFPFXGb0VdAtrqSBcNNW6a2qQ..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WFiWyF9uL8MUpmMjGg9-rCZ5JpX5o2p5NHD95Qce0571h5feKq7Ws4Dqcj3i--4i-20iKy8i--ci-zfiKnpi--fiKn7iKLhi--fiKnEi-2fMcSQ9gfL; SUHB=0gHi31zdWOb5uO; SSOLoginState=1498773934; H5_INDEX=0_all; H5_INDEX_TITLE=%E9%A3%8E%E6%B5%81%E5%80%9C%E5%82%A5scheme; M_WEIBOCN_PARAMS=featurecode%3D20000320%26luicode%3D10000011%26lfid%3D231051_-_followers_-_1646174132%26fid%3D231051_-_followers_-_1646174132%26uicode%3D10000011";

//    static String[] ss = s3.split(";");
//    static HashMap<String, String> hashMap = new HashMap<String, String>();

//    static {
//        for (String s : ss
//                ) {
//            s = s.trim();
//            hashMap.put(s.split("=")[0], s.split("=")[1]);
//        }
//    }

    private Site site = Site.me().setDomain("m.weibo.cn")
            //=; =;=; =; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWLsKfTbbdmS1i5Ag4a93lk5JpX5o2p5NHD95Q0eKzEeh.0eK-pWs4DqcjTBX2_x.-_B.2_B.-t; SUHB=0ftp1PPAefWZCh; SSOLoginState=1498764165
// 20170630//_T_WM=b0221597c0f5fa1a75e30291f14f2cc5; SUB=_2A250URq2DeRhGeBN61IZ8CvNzTyIHXVXvab-rDV6PUJbkdBeLUvnkW06RBbYnhSJuPIVJYoKNxNc7imDhw..; SUHB=0auAddHGx0iY4E; SCF=AvfhTspfQT-AdJtUE9okp8o8RYiTRQgac7MovhZrOPQSpLC1lwbZ5iKsuOhlsm3bE67BBVNqyPKaC4cNe5F2a-k.; SSOLoginState=1498770150; M_WEIBOCN_PARAMS=luicode%3D20000174%26uicode%3D20000174%26featurecode%3D20000320%26fid%3Dhotword
            //原来买的  //  ,"_T_WM=3561f9ef0afe89fc0dfad0909bf294dc; SUB=_2A250OlnbDeRhGeBM7FET8C3PzziIHXVXxWeTrDV6PUJbkdANLW_ukW1f3iR0033sPOe_-p-whSA5N2QYEQ..; SUHB=0xnCRNjdIwxnMa; SCF=ArVUJE6KcADC8LLj-H1RRxM-_ujSH4t0oXIlRSkRA0ivcHDoBDvL9bnmCNJcCmp8XnjkFg3moH6NjLZBZsfeMr8.; SSOLoginState=1497246091; H5_INDEX=0_all; H5_INDEX_TITLE=%E9%93%B2%E6%81%AD%E7%A5%A5%E8%AF%A5%E6%9F%93; M_WEIBOCN_PARAMS=featurecode%3D20000180%26luicode%3D20000174%26lfid%3Dhotword%26uicode%3D20000061%26fid%3D4117810924431972%26oid%3D4117810924431972"
//    ,"TC-V5-G0=666db167df2946fecd1ccee47498a93b; login_sid_t=f849a0ecac5ec24908aa6fa275995f36; TC-Ugrow-G0=5e22903358df63c5e3fd2c757419b456; WBStorage=5ea47215d42b077f|undefined; _s_tentry=-; Apache=2792676099697.073.1497245952933; SINAGLOBAL=2792676099697.073.1497245952933; ULV=1497245952937:1:1:1:2792676099697.073.1497245952933:; YF-V5-G0=73b58b9e32dedf309da5103c77c3af4f; YF-Ugrow-G0=56862bac2f6bf97368b95873bc687eef; SUB=_2A250OloSDeRhGeBM7FET8C3PzziIHXVXTszarDV8PUJbmtANLUPDkW-XjXAXT_M9ZxVe7aJGcJTfcLDd3w..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9W5BIE.-0A9EUZayHEQbJVWq5JpX5o2p5NHD95QceoM0eo50e0BXWs4Dqcj_i--4iK.0i-zEi--ciKnpi-82i--Ni-2fi-2fi--Ri-8si-2fi--ciKLsiK.0; SUHB=0gHi2YD4bXOuK9; SSOLoginState=1497246274; wvr=6; YF-Page-G0=f017d20b1081f0a1606831bba19e407b"

            .setCharset("utf-8")
//            .addCookie("_T_WM", hashMap.get("_T_WM"))
//            .addCookie("ALF", hashMap.get("ALF"))
//            .addCookie("SCF", hashMap.get("SCF"))
//            .addCookie("SUB", hashMap.get("SUB"))
//            .addCookie("H5_INDEX", hashMap.get("H5_INDEX"))
//            .addCookie("H5_INDEX_TITLE", hashMap.get("H5_INDEX_TITLE"))
//            .addCookie("M_WEIBOCN_PARAMS", hashMap.get("M_WEIBOCN_PARAMS"))
//            .addCookie("SUHB", hashMap.get("SUHB"))
//            .addCookie("SSOLoginState", hashMap.get("SSOLoginState"))
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Connection", "keep-alive")
            .addHeader("DNT", "1")
            .addHeader("Host", "m.weibo.cn")
            .addHeader("Pragma", "no-cache")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("commentUser-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36")
            .setRetryTimes(5).setSleepTime(100);


//    https://m.weibo.cn/api/container/getIndex?containerid=1005051922196194                              userInfoDetail
//    https://m.weibo.cn/api/container/getIndex?containerid=1076035069880687                              Weibo
//    https://m.weibo.cn/api/comments/show?id=4098963315988630&page=1                                     Comments & commentUser & CommentSource
//    https://m.weibo.cn/api/container/getIndex?containerid=2302835069880687                              userInfo
//    https://m.weibo.cn/api/container/getIndex?containerid=2304135861422056                              AllWeibo & WeiboCount
//    https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_5902470197&page=1        Followers
//    https://m.weibo.cn/api/container/getSecond?containerid=1005055680343342_-_FOLLOWERS&page=14         Followers
//    https://m.weibo.cn/api/container/getSecond?containerid=1005055680343342_-_FANS&page=100             FANS
//    https://m.weibo.cn/status/D0bthtoLM                                                                 WeiBoDetail


    @Override
    public void process(Page page) {
//        page.addTargetRequests();
//        String html = page.getRawText();
//        String[] ids = null;
//        Pattern p = Pattern.compile("name=\"uidList\" value=\"(.*?)\" /><input type=\"submit\"");
//        Matcher matcher = p.matcher(html);
//        if (matcher.find()) {
//            System.out.println(matcher.group(1));
//            ids = matcher.group(1).split(",");
//        }
//        System.out.println(page.getRequest().getUrl());
        String s6 = page.getRawText().replace("\\/", "/");
        JSONObject jsonObject = JSON.parseObject(decodeUnicode(s6));
        int flag = jsonObject.getInteger("ok");

        Pattern pattern = Pattern.compile("containerid=231051_-_followers_-_(.*?)&page");
        Matcher matcher = pattern.matcher(page.getUrl().get());
        if (matcher.find()) {
            if (page.getUrl().get().contains("page=1")) {
                int i = jsonObject.getJSONObject("cardlistInfo").getInteger("total");
                for (int j = 2; j <= i / 20 + 1; j++) {
                    page.addTargetRequest("https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_" + matcher.group(1) + "&page=");
                }
            }
        }

        if (flag == 1) {
            JSONArray jsonArray = jsonObject.getJSONArray("cards");
            for (Object object : jsonArray
                    ) {
                for (Object jo : ((JSONObject) object).getJSONArray("card_group")
                        ) {
                    if (((JSONObject) jo).containsKey("commentUser")) {
                        long id = ((JSONObject) jo).getJSONObject("commentUser").getLong("id");
                        page.putField("id", id);
                        page.addTargetRequest("https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_" + id + "&page=1");
                    }
                }
            }
        }
//        if (page.getRawText().contains("name=\"uidList\" value=\"")) {
//            // 添加所有文章页
//            String html = page.getRawText();
//            Pattern p = Pattern.compile("name=\"uidList\" value=\"(.*?)\" /><input type=\"submit\"");
//            Matcher matcher = p.matcher(html);
//            String[] ss = null;
//            if (matcher.find()) {
//                ss = matcher.group(1).split(",");
//            }
//            for (String s : ss
//                    ) {
//                page.addTargetRequest("https://weibo.cn/" + s + "/follow ");
//                page.addTargetRequest("https://weibo.cn/" + s + "/fans ");
//                page.putField("userID", s);
//            }
//        }


    }

//    private static String decodeUnicode(String str) {
//        Charset set = Charset.forName("UTF-16");
//        Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
//        Matcher m = p.matcher(str);
//        int start = 0;
//        int start2 = 0;
//        StringBuffer sb = new StringBuffer();
//        while (m.find(start)) {
//            start2 = m.start();
//            if (start2 > start) {
//                String seg = str.substring(start, start2);
//                sb.append(seg);
//            }
//            String code = m.group(1);
//            int i = Integer.valueOf(code, 16);
//            byte[] bb = new byte[4];
//            bb[0] = (byte) ((i >> 8) & 0xFF);
//            bb[1] = (byte) (i & 0xFF);
//            ByteBuffer b = ByteBuffer.wrap(bb);
//            sb.append(String.valueOf(set.decode(b)).trim());
//            start = m.end();
//        }
//        start2 = str.length();
//        if (start2 > start) {
//            String seg = str.substring(start, start2);
//            sb.append(seg);
//        }
//        return sb.toString();
//    }


    @Override
    public Site getSite() {
        return site;
    }

//    public static void main(String[] args) {
////        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
////        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(
////                new Proxy("101.101.101.101", 8888, "", "")
////                , new Proxy("102.102.102.102", 8888)));
////        System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
////        System.getProperties().setProperty("socksProxyPort", "1080");
////        Spider.create(new WeiBoPageProcessor()).addPipeline(new FilePipeline("result")).addUrl("https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_5902470197&page=1").thread(1).run();//.addPipeline(new FilePipeline("result.txt"))
//        Spider spider = new Spider(new WeiBoPageProcessorTest());
//        spider.setSpiderName("weibo");
//        spider.addPipeline(new FilePipeline("result")).addUrl("https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_5902470197&page=1").thread(1);//.addPipeline(new FilePipeline("result.txt"))
////        spider.run();
//        spider.start();
//        System.out.println("asdfasd");
//    }


//    public static final Object parse(String text); // 把JSON文本parse为JSONObject或者JSONArray
//    public static final JSONObject parseObject(String text)； // 把JSON文本parse成JSONObject
//    public static final <T> T parseObject(String text, Class<T> clazz); // 把JSON文本parse为JavaBean
//    public static final JSONArray parseArray(String text); // 把JSON文本parse成JSONArray
//    public static final <T> List<T> parseArray(String text, Class<T> clazz); //把JSON文本parse成JavaBean集合
//    public static final String toJSONString(Object object); // 将JavaBean序列化为JSON文本
//    public static final String toJSONString(Object object, boolean prettyFormat); // 将JavaBean序列化为带格式的JSON文本
//    public static final Object toJSON(Object javaObject); 将JavaBean转换为JSONObject或者JSONArray。

    private void parseFollowers(String jsonResult) {
        JSONObject followers = JSON.parseObject(jsonResult);

    }
    //https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_1250748474&page=0
//    private List<String>  parseFans(String jsonResult){
//
//    }
//https://m.weibo.cn/api/container/getIndex?containerid=231051_-_fans_-_5342071420&page=2
//    private List<> parseUserWeibo(String jsonResult){
//
//    }
//https://m.weibo.cn/container/getIndex?containerid=1076031677969704
//    private List<> parseUserInfo(String jsonResult){
//
//    }
//https://m.weibo.cn/api/container/getIndex?containerid=1005055069880687
//    private List<> parseComments(String jsonResult){
//
//    }
//https://m.weibo.cn/api/comments/show?id=4098963315988630&page=1
//    private List<> parseHomePage(String jsonResult){
//
//    }
//    https://m.weibo.cn/feed/friends?version=v4
}
