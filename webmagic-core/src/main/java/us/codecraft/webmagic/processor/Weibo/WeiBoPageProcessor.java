package us.codecraft.webmagic.processor.Weibo;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Data5UProxyProvider;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.List;
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
public abstract class WeiBoPageProcessor implements PageProcessor {
    protected Spider spider;
    private String userSQL = "INSERT INTO `weibo`.`user` (`id`, `desc1`, `desc2`, `cover_image_phone`, `description`, `gender`, `profile_image_url`, `profile_url`,`screen_name`,`verified_reason`,`follow_count`,`followers_count`,`mbrank`,`mbtype`,`statuses_count`,`urank`,`verified_type`,`verified_type_ext`,`verified`) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?,?,?);";
    private String mblogSQL = "INSERT INTO `weibo`.`mblog` (`bid`, `attitudes_count`, `comments_count`, `created_at`, `id`, `idstr`, `isLongText`, `mid`,`raw_text`,`reposts_count`,`retweeted_ID`,`source`,`text`,`title`,`userID`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private String commentSQL = "INSERT INTO weibo.comment (created_at, id, like_count, reply_id, source, text, userID, is_hot,blogID)VALUES (?,?, ?, ?, ?, ?, ?, ?, ?);";

    protected static Connection conn = null;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/weibo?user=root&password=root";
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setSpider(Spider spider) {
        this.spider = spider;
    }

    private Site site = Site.me().setDomain("m.weibo.cn")
            .setCharset("utf-8")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Accept-Encoding", "")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Connection", "keep-alive")
            .addHeader("DNT", "1")
            .addHeader("Host", "m.weibo.cn")
            .addHeader("Pragma", "no-cache")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36")
            .setRetryTimes(5).setSleepTime(30);

    static String decodeUnicode(String str) {
        Charset set = Charset.forName("UTF-16");
        Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher m = p.matcher(str);
        int start = 0;
        int start2 = 0;
        StringBuffer sb = new StringBuffer();
        while (m.find(start)) {
            start2 = m.start();
            if (start2 > start) {
                String seg = str.substring(start, start2);
                sb.append(seg);
            }
            String code = m.group(1);
            int i = Integer.valueOf(code, 16);
            byte[] bb = new byte[4];
            bb[0] = (byte) ((i >> 8) & 0xFF);
            bb[1] = (byte) (i & 0xFF);
            ByteBuffer b = ByteBuffer.wrap(bb);
            sb.append(String.valueOf(set.decode(b)).trim());
            start = m.end();
        }
        start2 = str.length();
        if (start2 > start) {
            String seg = str.substring(start, start2);
            sb.append(seg);
        }
        return sb.toString();
    }


    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        if (((Data5UProxyProvider) ((HttpClientDownloader) (spider.getDownloader())).getProxyProvider()).getProxyCount() <= 2) {
//            this.site.setSleepTime(2000);
            try {
                long sleepMs = 5 * 1000;
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        } else if (((Data5UProxyProvider) ((HttpClientDownloader) (spider.getDownloader())).getProxyProvider()).getProxyCount() >= 8) {
//            this.site.setSleepTime(300);
//        }
    }


    public int add(User user) {
        try {
            PreparedStatement ps = conn.prepareStatement(userSQL);
            ps.setLong(1, user.getId());
            ps.setString(2, user.getDesc1());
            ps.setString(3, user.getDesc2());
            ps.setString(4, user.getCover_image_phone());
            ps.setString(5, user.getDescription());
            ps.setString(6, user.getGender());
            ps.setString(7, user.getProfile_image_url());
            ps.setString(8, user.getProfile_url());
            ps.setString(9, user.getScreen_name());
            ps.setString(10, user.getVerifiedReason());
            ps.setInt(11, user.getFollowCount());
            ps.setInt(12, user.getFollowersCount());
            ps.setInt(13, user.getMbrank());
            ps.setInt(14, user.getMbtype());
            ps.setInt(15, user.getStatusesCount());
            ps.setInt(16, user.getUrank());
            ps.setInt(17, user.getVerifiedType());
            ps.setInt(18, user.getVerifiedTypeExt());
            ps.setBoolean(19, user.isVerified());
            int a = ps.executeUpdate();
            ps.close();
            ps = null;
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int add(Mblog mblog) {

        try {
            PreparedStatement ps = conn.prepareStatement(mblogSQL);
            ps.setString(1, mblog.getBid());
            ps.setInt(2, mblog.getAttitudesCount());
            ps.setInt(3, mblog.getCommentsCount());
            ps.setLong(4, mblog.getCreatedAt());
            ps.setLong(5, mblog.getmBlogID());
            ps.setString(6, mblog.getmBlogIDStr());
            ps.setBoolean(7, mblog.isLongText());
            ps.setString(8, mblog.getMid());
            ps.setString(9, mblog.getRaw_text());
            ps.setInt(10, mblog.getRepostsCount());
            ps.setLong(11, mblog.getRetweetedID());
            ps.setString(12, mblog.getSource());
            ps.setString(13, mblog.getText());
            ps.setString(14, mblog.getTitle());
            ps.setLong(15, mblog.getUserID());
            int a = ps.executeUpdate();
            ps.close();
            ps = null;
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int add(Comment comment) {

        try {
            PreparedStatement ps = conn.prepareStatement(commentSQL);
            ps.setString(1, comment.getCreated_at());
            ps.setLong(2, comment.getId());
            ps.setLong(3, comment.getLike_count());
            ps.setLong(4, comment.getReply_id());
            ps.setString(5, comment.getSource());
            ps.setString(6, comment.getText());
            ps.setLong(7, comment.getCommentUser().getId());
            ps.setBoolean(8, comment.isHot());
            ps.setLong(9, comment.getBlogID());
            int a = ps.executeUpdate();
            ps.close();
            ps = null;
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void addAll(List<Comment> commentList) {
        for (Comment c : commentList
                ) {
            add(c);
        }
    }

}
