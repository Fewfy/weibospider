package us.codecraft.webmagic.processor.Weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.utils.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
 * Created by Imgaojp on 2017/8/10.
 */
public class WeiBoMblogPageProcessor extends WeiBoPageProcessor {


    //    https://m.weibo.cn/api/container/getIndex?containerid=2304135861422056                              AllWeibo & WeiboCount    better than    //    https://m.weibo.cn/api/container/getIndex?containerid=1076035069880687                              Weibo
    //    https://m.weibo.cn/status/D0bthtoLM                                                                 WeiBoDetail


    Pattern p = Pattern.compile("\"text\": \"(.*?)\",        ");
    Pattern p1 = Pattern.compile("\"created_at\": \"(.*/?)\",");

    @Override
    public void process(Page page) {
        JSONObject jsonObject = JSON.parseObject(decodeUnicode(page.getRawText().replace("\\/", "/")));
        int flag = jsonObject.getInteger("ok");

        if (flag == 1) {
            int total = jsonObject.getJSONObject("cardlistInfo").getInteger("total");
            int maxPage = total / 10 + 1;
            if (page.getRequest().getUrl().contains("page=1")) {
                for (int i = 2; i <= maxPage; i++) {
                    page.addTargetRequest(page.getRequest().getUrl().replace("page=1", "page=" + String.valueOf(i)));
                }
            }
            JSONArray mblogs = jsonObject.getJSONArray("cards");
            for (Object obj : mblogs
                    ) {
                JSONObject jo = ((JSONObject) obj);
                JSONObject mblogJO = jo.getJSONObject("mblog");

                long createdAt = 0;
                String text = "";

                String url = String.format("https://m.weibo.cn/status/%s", mblogJO.getString("bid"));
                String html = HttpRequest.sendGet(url, ((HttpClientDownloader) spider.getDownloader()).getProxyProvider().getProxy(null));
                Matcher m = p.matcher(html);
                if (m.find()) {
                    text = m.group(1).trim();
//                    text = text.substring(0, text.length() - 2);
                }
                Matcher m1 = p1.matcher(html);
                if (m1.find()) {
                    createdAt = dateToStamp(m1.group(1));
                } else {
                    page.setDownloadSuccess(false);
                }

                text = text.equals("") ? mblogJO.getString("text") : text;

                Mblog mblog;
                long userID = mblogJO.getJSONObject("user").getLong("id");
                if (mblogJO.containsKey("retweeted_status")) {
                    long retweetedID = Long.valueOf(mblogJO.getJSONObject("retweeted_status").getString("id"));
                    mblog = new Mblog(mblogJO.getString("bid"), mblogJO.getString("idstr"), mblogJO.getString("mid"), mblogJO.getString("source"), mblogJO.getString("raw_text"), text, jo.getString("title"), mblogJO.getInteger("attitudes_count"), mblogJO.getInteger("comments_count"), mblogJO.getInteger("reposts_count"), mblogJO.getBoolean("isLongText"), Long.valueOf(mblogJO.getString("id")), createdAt, retweetedID, userID);
                } else {
                    mblog = new Mblog(mblogJO.getString("bid"), mblogJO.getString("idstr"), mblogJO.getString("mid"), mblogJO.getString("source"), mblogJO.getString("raw_text"), text, jo.getString("title"), mblogJO.getInteger("attitudes_count"), mblogJO.getInteger("comments_count"), mblogJO.getInteger("reposts_count"), mblogJO.getBoolean("isLongText"), Long.valueOf(mblogJO.getString("id")), createdAt, userID);
                }
                spider.addUrl(String.format("https://m.weibo.cn/api/comments/show?id=%s&page=1", mblog.mBlogIDStr));
                if (createdAt != 0) {
                    add(mblog);
                }
//                System.out.println(mblog);
            }
        }
    }


    private static long dateToStamp(String s) {
        //Tue Aug 08 19:42:33 +0800 2017
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
        Date date;
        long ts;
        try {
            date = simpleDateFormat.parse(s);
            ts = date.getTime() / 1000;
        } catch (ParseException e) {
            ts = 0;
        }
        return ts;
    }
}

class Mblog {
    String bid, mBlogIDStr, mid, source, raw_text, text, title;
    int attitudesCount, commentsCount, repostsCount;
    boolean isLongText;
    long mBlogID, createdAt, retweetedID, userID;

    public Mblog(String bid, String mBlogIDStr, String mid, String source, String raw_text, String text, String title, int attitudesCount, int commentsCount, int repostsCount, boolean isLongText, long mBlogID, long createdAt, long userID) {
        this.bid = bid;
        this.mBlogIDStr = mBlogIDStr;
        this.mid = mid;
        this.source = source;
        this.raw_text = raw_text;
        this.text = text;
        this.title = title;
        this.attitudesCount = attitudesCount;
        this.commentsCount = commentsCount;
        this.repostsCount = repostsCount;
        this.isLongText = isLongText;
        this.mBlogID = mBlogID;
        this.createdAt = createdAt;
        this.userID = userID;
        this.retweetedID = 0;
    }

    public Mblog(String bid, String mBlogIDStr, String mid, String source, String raw_text, String text, String title, int attitudesCount, int commentsCount, int repostsCount, boolean isLongText, long mBlogID, long createdAt, long retweetedID, long userID) {
        this.bid = bid;
        this.mBlogIDStr = mBlogIDStr;
        this.mid = mid;
        this.source = source;
        this.raw_text = raw_text;
        this.text = text;
        this.title = title;
        this.attitudesCount = attitudesCount;
        this.commentsCount = commentsCount;
        this.repostsCount = repostsCount;
        this.isLongText = isLongText;
        this.mBlogID = mBlogID;
        this.createdAt = createdAt;
        this.retweetedID = retweetedID;
        this.userID = userID;
    }

    public String getBid() {
        return bid;
    }

    public String getmBlogIDStr() {
        return mBlogIDStr;
    }

    public String getMid() {
        return mid;
    }

    public String getSource() {
        return source;
    }

    public String getRaw_text() {
        return raw_text;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public int getAttitudesCount() {
        return attitudesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public int getRepostsCount() {
        return repostsCount;
    }

    public boolean isLongText() {
        return isLongText;
    }

    public long getmBlogID() {
        return mBlogID;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getRetweetedID() {
        return retweetedID;
    }

    public long getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        return "Mblog{" +
                "bid='" + bid + '\'' +
                ", mBlogIDStr='" + mBlogIDStr + '\'' +
                ", mid='" + mid + '\'' +
                ", source='" + source + '\'' +
                ", raw_text='" + raw_text + '\'' +
                ", text='" + text + '\'' +
                ", title='" + title + '\'' +
                ", attitudesCount=" + attitudesCount +
                ", commentsCount=" + commentsCount +
                ", repostsCount=" + repostsCount +
                ", isLongText=" + isLongText +
                ", mBlogID=" + mBlogID +
                ", createdAt=" + createdAt +
                ", retweetedID=" + retweetedID +
                ", userID=" + userID +
                '}';
    }
}