package us.codecraft.webmagic.processor.Weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.sun.xml.internal.bind.v2.model.core.ID;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Data5UProxyProvider;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

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
public class WeiBoUserPageProcessor extends WeiBoPageProcessor {

    protected static Statement stmt = null;


    public List<String> getAllUser() {
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<String> users = new ArrayList<String>(300000);
        String sql = "SELECT id FROM `weibo`.`user`";
        try {
            ResultSet rs = stmt.executeQuery(sql);
//            int col = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                users.add(rs.getString(1));
//                for (int i = 1; i <= col; i++) {
//                    System.out.print(rs.getString(i) + "\t");
//                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    public WeiBoUserPageProcessor() {
        super();
        List<String> IDs = getAllUser();
        bloomFilter.initFromSQL(IDs);
        IDs = null;
    }

    private final UserBloomFilterDuplicateRemover bloomFilter = new UserBloomFilterDuplicateRemover(300000000);

    //    https://m.weibo.cn/api/container/getSecond?containerid=1005055680343342_-_FOLLOWERS&page=14         Followers         better than  //    https://m.weibo.cn/api/container/getIndex?containerid=231051_-_followers_-_5902470197&page=1        Followers
    //    https://m.weibo.cn/api/container/getSecond?containerid=1005055680343342_-_FANS&page=100             FANS              better than  //    https://m.weibo.cn/api/container/getIndex?containerid=231051_-_fans_-_5902470197&page=1             FANS
    //在上面两个接口包括了    https://m.weibo.cn/api/container/getIndex?containerid=1005051922196194                              userInfoDetail    better than  //    https://m.weibo.cn/api/container/getIndex?containerid=2302835069880687                              userInfo

    @Override
    public void process(Page page) {
        super.process(page);

        JSONObject jsonObject = JSON.parseObject(decodeUnicode(page.getRawText().replace("\\/", "/")));
        int flag = jsonObject.getInteger("ok");

        if (flag == 1) {
            int maxPage = jsonObject.getInteger("maxPage");
            if (page.getRequest().getUrl().endsWith("page=1") && page.getRequest().getUrl().contains("FANS")) {
                for (int i = 2; i <= maxPage; i++) {
                    page.addTargetRequest(page.getRequest().getUrl().replace("page=1", "page=" + String.valueOf(i)));
                }
            }
            if (page.getRequest().getUrl().endsWith("page=1") && page.getRequest().getUrl().contains("FOLLOWERS")) {
                for (int i = 2; i <= maxPage; i++) {
                    page.addTargetRequest(page.getRequest().getUrl().replace("page=1", "page=" + String.valueOf(i)));
                }
            }
            JSONArray jsonArray = jsonObject.getJSONArray("cards");
            for (Object object : jsonArray
                    ) {
                JSONObject jo = (JSONObject) object;
                JSONObject userJO = jo.getJSONObject("user");
                if (userJO.getBoolean("verified")) {
                    User user = new User(jo.getString("desc1"), jo.getString("desc2"), userJO.getString("cover_image_phone"), userJO.getString("description"), userJO.getString("gender"), userJO.getString("profile_image_url"), userJO.getString("profile_url"), userJO.getString("screen_name"), userJO.getString("verified_reason"), userJO.getInteger("follow_count"), userJO.getInteger("followers_count"), userJO.getInteger("mbrank"), userJO.getInteger("mbtype"), userJO.getInteger("statuses_count"), userJO.getInteger("urank"), userJO.getInteger("verified_type"), userJO.getInteger("verified_type_ext"), userJO.getLong("id"), userJO.getBoolean("verified"));
//                    System.out.println(user);
                    if (!bloomFilter.isDuplicate(user)) {
                        add(user);
                        if (spider != null) {
                            spider.addUrl(String.format("https://m.weibo.cn/api/container/getIndex?containerid=230413%d&page=1", user.id));
                        }
                        page.addTargetRequest(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FOLLOWERS&page=1", user.id));
                        page.addTargetRequest(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FANS&page=1", user.id));
                        user = null;
                    }
                } else {
                    User user = new User(jo.getString("desc1"), jo.getString("desc2"), userJO.getString("cover_image_phone"), userJO.getString("description"), userJO.getString("gender"), userJO.getString("profile_image_url"), userJO.getString("profile_url"), userJO.getString("screen_name"), userJO.getInteger("follow_count"), userJO.getInteger("followers_count"), userJO.getInteger("mbrank"), userJO.getInteger("mbtype"), userJO.getInteger("statuses_count"), userJO.getInteger("urank"), userJO.getInteger("verified_type"), userJO.getLong("id"), userJO.getBoolean("verified"));
//                    System.out.println(user);
                    if (!bloomFilter.isDuplicate(user)) {
                        add(user);
                        if (spider != null) {
                            spider.addUrl(String.format("https://m.weibo.cn/api/container/getIndex?containerid=230413%d&page=1", user.id));
                        }
                        page.addTargetRequest(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FOLLOWERS&page=1", user.id));
                        page.addTargetRequest(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FANS&page=1", user.id));
                        user = null;
                    }
//                    "https://m.weibo.cn/api/container/getIndex?containerid=2304133720838047&page=1"
                }
            }
        }
    }
}

class User {
    String desc1, desc2, cover_image_phone, description, gender, profile_image_url, profile_url, screen_name, verifiedReason;
    int followCount, followersCount, mbrank, mbtype, statusesCount, urank, verifiedType, verifiedTypeExt;
    long id;
    boolean verified;

    public User(String desc1, String desc2, String cover_image_phone, String description, String gender, String profile_image_url, String profile_url, String screen_name, String verifiedReason, int followCount, int followersCount, int mbrank, int mbtype, int statusesCount, int urank, int verifiedType, int verifiedTypeExt, long id, boolean verified) {
        this.desc1 = desc1;
        this.desc2 = desc2;
        this.cover_image_phone = cover_image_phone;
        this.description = description;
        this.gender = gender;
        this.profile_image_url = profile_image_url;
        this.profile_url = profile_url;
        this.screen_name = screen_name;
        this.verifiedReason = verifiedReason;
        this.followCount = followCount;
        this.followersCount = followersCount;
        this.mbrank = mbrank;
        this.mbtype = mbtype;
        this.statusesCount = statusesCount;
        this.urank = urank;
        this.verifiedType = verifiedType;
        this.verifiedTypeExt = verifiedTypeExt;
        this.id = id;
        this.verified = verified;
    }

    public User(String desc1, String desc2, String cover_image_phone, String description, String gender, String profile_image_url, String profile_url, String screen_name, int followCount, int followersCount, int mbrank, int mbtype, int statusesCount, int urank, int verifiedType, long id, boolean verified) {
        this.desc1 = desc1;
        this.desc2 = desc2;
        this.cover_image_phone = cover_image_phone;
        this.description = description;
        this.gender = gender;
        this.profile_image_url = profile_image_url;
        this.profile_url = profile_url;
        this.screen_name = screen_name;
        this.followCount = followCount;
        this.followersCount = followersCount;
        this.mbrank = mbrank;
        this.mbtype = mbtype;
        this.statusesCount = statusesCount;
        this.urank = urank;
        this.verifiedType = verifiedType;
        this.id = id;
        this.verified = verified;
        this.verifiedReason = "";
        this.verifiedTypeExt = 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "desc1='" + desc1 + '\'' +
                ", desc2='" + desc2 + '\'' +
                ", cover_image_phone='" + cover_image_phone + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", profile_image_url='" + profile_image_url + '\'' +
                ", profile_url='" + profile_url + '\'' +
                ", screen_name='" + screen_name + '\'' +
                ", verifiedReason='" + verifiedReason + '\'' +
                ", followCount=" + followCount +
                ", followersCount=" + followersCount +
                ", mbrank=" + mbrank +
                ", mbtype=" + mbtype +
                ", statusesCount=" + statusesCount +
                ", urank=" + urank +
                ", verifiedType=" + verifiedType +
                ", verifiedTypeExt=" + verifiedTypeExt +
                ", id=" + id +
                ", verified=" + verified +
                '}';
    }

    public String getDesc1() {
        return desc1;
    }

    public String getDesc2() {
        return desc2;
    }

    public String getCover_image_phone() {
        return cover_image_phone;
    }

    public String getDescription() {
        return description;
    }

    public String getGender() {
        return gender;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public String getVerifiedReason() {
        return verifiedReason;
    }

    public int getFollowCount() {
        return followCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getMbrank() {
        return mbrank;
    }

    public int getMbtype() {
        return mbtype;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public int getUrank() {
        return urank;
    }

    public int getVerifiedType() {
        return verifiedType;
    }

    public int getVerifiedTypeExt() {
        return verifiedTypeExt;
    }

    public long getId() {
        return id;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (getFollowCount() != user.getFollowCount()) return false;
        if (getFollowersCount() != user.getFollowersCount()) return false;
        if (getMbrank() != user.getMbrank()) return false;
        if (getMbtype() != user.getMbtype()) return false;
        if (getStatusesCount() != user.getStatusesCount()) return false;
        if (getUrank() != user.getUrank()) return false;
        if (getVerifiedType() != user.getVerifiedType()) return false;
        if (getVerifiedTypeExt() != user.getVerifiedTypeExt()) return false;
        if (getId() != user.getId()) return false;
        if (isVerified() != user.isVerified()) return false;
        if (getDesc1() != null ? !getDesc1().equals(user.getDesc1()) : user.getDesc1() != null) return false;
        if (getDesc2() != null ? !getDesc2().equals(user.getDesc2()) : user.getDesc2() != null) return false;
        if (getCover_image_phone() != null ? !getCover_image_phone().equals(user.getCover_image_phone()) : user.getCover_image_phone() != null)
            return false;
        if (getDescription() != null ? !getDescription().equals(user.getDescription()) : user.getDescription() != null)
            return false;
        if (getGender() != null ? !getGender().equals(user.getGender()) : user.getGender() != null) return false;
        if (getProfile_image_url() != null ? !getProfile_image_url().equals(user.getProfile_image_url()) : user.getProfile_image_url() != null)
            return false;
        if (getProfile_url() != null ? !getProfile_url().equals(user.getProfile_url()) : user.getProfile_url() != null)
            return false;
        if (getScreen_name() != null ? !getScreen_name().equals(user.getScreen_name()) : user.getScreen_name() != null)
            return false;
        return getVerifiedReason() != null ? getVerifiedReason().equals(user.getVerifiedReason()) : user.getVerifiedReason() == null;
    }

    @Override
    public int hashCode() {
        int result = getDesc1() != null ? getDesc1().hashCode() : 0;
        result = 31 * result + (getDesc2() != null ? getDesc2().hashCode() : 0);
        result = 31 * result + (getCover_image_phone() != null ? getCover_image_phone().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getGender() != null ? getGender().hashCode() : 0);
        result = 31 * result + (getProfile_image_url() != null ? getProfile_image_url().hashCode() : 0);
        result = 31 * result + (getProfile_url() != null ? getProfile_url().hashCode() : 0);
        result = 31 * result + (getScreen_name() != null ? getScreen_name().hashCode() : 0);
        result = 31 * result + (getVerifiedReason() != null ? getVerifiedReason().hashCode() : 0);
        result = 31 * result + getFollowCount();
        result = 31 * result + getFollowersCount();
        result = 31 * result + getMbrank();
        result = 31 * result + getMbtype();
        result = 31 * result + getStatusesCount();
        result = 31 * result + getUrank();
        result = 31 * result + getVerifiedType();
        result = 31 * result + getVerifiedTypeExt();
        result = 31 * result + (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (isVerified() ? 1 : 0);
        return result;
    }
}


class UserBloomFilterDuplicateRemover {

    private int expectedInsertions;

    private double fpp;

    protected AtomicInteger counter;

    public UserBloomFilterDuplicateRemover(int expectedInsertions) {
        this(expectedInsertions, 0.01);
    }

    /**
     * @param expectedInsertions the number of expected insertions to the constructed
     * @param fpp                the desired false positive probability (must be positive and less than 1.0)
     */
    public UserBloomFilterDuplicateRemover(int expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.bloomFilter = rebuildBloomFilter();
    }

    protected BloomFilter<CharSequence> rebuildBloomFilter() {
        counter = new AtomicInteger(0);
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);
    }

    protected final BloomFilter<CharSequence> bloomFilter;

    public boolean isDuplicate(User user) {
        boolean isDuplicate = bloomFilter.mightContain(String.valueOf(user.getId()));
        if (!isDuplicate) {
            bloomFilter.put(String.valueOf(user.getId()));
            counter.incrementAndGet();
        }
        return isDuplicate;
    }

    public void initFromSQL(List<String> IDs) {
        for (String s : IDs
                ) {
            bloomFilter.put(s);
        }
    }

}