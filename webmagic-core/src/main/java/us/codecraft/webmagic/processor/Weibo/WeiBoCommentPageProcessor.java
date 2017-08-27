package us.codecraft.webmagic.processor.Weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;

import java.nio.charset.Charset;
import java.util.ArrayList;
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
public class WeiBoCommentPageProcessor extends WeiBoPageProcessor {


    //    https://m.weibo.cn/api/comments/show?id=4098963315988630&page=1                                     Comments & commentUser & CommentSource
    Pattern pattern = Pattern.compile("id=(.*?)&page=");

    @Override
    public void process(Page page) {
        JSONObject jsonObject = JSON.parseObject(decodeUnicode(page.getRawText().replace("\\/", "/")));
        int flag = jsonObject.getInteger("ok");

        if (flag == 1) {
            int max = jsonObject.getInteger("max");
            if (page.getRequest().getUrl().contains("page=1")) {
                for (int i = 2; i <= max; i++) {
                    page.addTargetRequest(page.getRequest().getUrl().replace("page=1", "page=" + String.valueOf(i)));
                }
                //TODO: save to mysql
//                new WeiBoDao().addAll(parseJson(jsonObject, "hot_data"));

//                System.out.println(parseJson(jsonObject, "hot_data"));
            }
            //TODO: save to mysql
            //TODO: find the latest comments in database and stop when find it (use bloomFilter)
//            addAll(parseJson(jsonObject, "data"));

            JSONArray jsonArray = jsonObject.getJSONArray("data");
            //https://m.weibo.cn/api/comments/show?id=4098963315988630&page=1
            Matcher matcher = pattern.matcher(page.getRequest().getUrl());
            long blogID;
            if (matcher.find()) {
                blogID = Long.valueOf(matcher.group(1));
            } else {
                blogID = 0;
            }
            for (Object object : jsonArray
                    ) {
                JSONObject jo = (JSONObject) object;
                JSONObject userJO = jo.getJSONObject("user");
                commentUser user = new commentUser(userJO.getLong("id"), userJO.getInteger("mbtype"), userJO.getString("profile_image_url"), userJO.getString("profile_url"), userJO.getString("remark"), userJO.getString("screen_name"), userJO.getBoolean("verified"), userJO.getInteger("verified_type"));
                //TODO:add commentUser url
//            https://m.weibo.cn/api/container/getSecond?containerid=1005053720838047_-_FANS&page=1
//            https://m.weibo.cn/api/container/getSecond?containerid=1005053720838047_-_FOLLOWERS&page=1
                spider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FOLLOWERS&page=1", user.id));
                spider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FANS&page=1", user.id));
                if (jo.containsKey("reply_id")) {
                    add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getLong("reply_id"), jo.getString("reply_text"), jo.getString("source"), jo.getString("text"), user, false, blogID));
                } else {
                    add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getString("source"), jo.getString("text"), user, true,blogID));
                }
            }

//            System.out.println(parseJson(jsonObject, "data"));
        }
    }

//    public List<Comment> parseJson(JSONObject jsonObject, String key) {
//        JSONArray jsonArray = jsonObject.getJSONArray(key);
//        List<Comment> comments = new ArrayList<Comment>();
//        //https://m.weibo.cn/api/comments/show?id=4098963315988630&page=1
//
//        for (Object object : jsonArray
//                ) {
//            JSONObject jo = (JSONObject) object;
//            JSONObject userJO = jo.getJSONObject("user");
//            commentUser user = new commentUser(userJO.getLong("id"), userJO.getInteger("mbtype"), userJO.getString("profile_image_url"), userJO.getString("profile_url"), userJO.getString("remark"), userJO.getString("screen_name"), userJO.getBoolean("verified"), userJO.getInteger("verified_type"));
//            //TODO:add commentUser url
////            https://m.weibo.cn/api/container/getSecond?containerid=1005053720838047_-_FANS&page=1
////            https://m.weibo.cn/api/container/getSecond?containerid=1005053720838047_-_FOLLOWERS&page=1
//            spider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FOLLOWERS&page=1", user.id));
//            spider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FANS&page=1", user.id));
//            if (jo.containsKey("reply_id")) {
//                if (key.equals("data")) {
//                    comments.add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getLong("reply_id"), jo.getString("reply_text"), jo.getString("source"), jo.getString("text"), user, false));
//                } else {
//                    comments.add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getLong("reply_id"), jo.getString("reply_text"), jo.getString("source"), jo.getString("text"), user, true));
//                }
//            } else {
//                if (key.equals("data")) {
//                    comments.add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getString("source"), jo.getString("text"), user, false));
//                } else {
//                    comments.add(new Comment(jo.getString("created_at"), jo.getLong("id"), jo.getLong("like_counts"), jo.getBoolean("liked"), jo.getString("source"), jo.getString("text"), user, true));
//                }
//            }
//        }
//        return comments;
//    }

}


class Comment {
    String created_at;
    long id, blogID;
    long like_count;
    boolean liked;
    long reply_id;
    String reply_text;
    String source;
    String text;
    commentUser commentUser;
    boolean isHot;


    public Comment(String created_at, long id, long like_count, boolean liked, String source, String text, commentUser user, boolean isHot, long blogID) {
        this(created_at, id, like_count, liked, 0, "", source, text, user, isHot, blogID);
    }

    public Comment(String created_at, long id, long like_count, boolean liked, long reply_id, String reply_text, String source, String text, commentUser commentUser, boolean isHot, long blogID) {
        this.created_at = created_at;
        this.id = id;
        this.like_count = like_count;
        this.liked = liked;
        this.reply_id = reply_id;
        this.reply_text = reply_text;
        this.source = source;
        this.text = text;
        this.commentUser = commentUser;
        this.isHot = isHot;
        this.blogID = blogID;
    }

    public long getBlogID() {
        return blogID;
    }

    public void setBlogID(long blogID) {
        this.blogID = blogID;
    }

    public String getCreated_at() {
        return created_at;
    }

    public long getId() {
        return id;
    }

    public long getLike_count() {
        return like_count;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getReply_id() {
        return reply_id;
    }

    public String getReply_text() {
        return reply_text;
    }

    public String getSource() {
        return source;
    }

    public String getText() {
        return text;
    }

    public us.codecraft.webmagic.processor.Weibo.commentUser getCommentUser() {
        return commentUser;
    }

    public boolean isHot() {
        return isHot;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "created_at='" + created_at + '\'' +
                ", id=" + id +
                ", blogID=" + blogID +
                ", like_count=" + like_count +
                ", liked=" + liked +
                ", reply_id=" + reply_id +
                ", reply_text='" + reply_text + '\'' +
                ", source='" + source + '\'' +
                ", text='" + text + '\'' +
                ", commentUser=" + commentUser +
                ", isHot=" + isHot +
                '}';
    }
}

class commentUser {
    long id;
    int mbtype;
    String profile_image_url;
    String profile_url;
    String remark;
    String screen_name;
    boolean verified;
    int verifiedType;

    public commentUser(long id, int mbtype, String profile_image_url, String profile_url, String remark, String screen_name, boolean verified, int verifiedType) {
        this.id = id;
        this.mbtype = mbtype;
        this.profile_image_url = profile_image_url;
        this.profile_url = profile_url;
        this.remark = remark;
        this.screen_name = screen_name;
        this.verified = verified;
        this.verifiedType = verifiedType;
    }

    public long getId() {
        return id;
    }

    public int getMbtype() {
        return mbtype;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public String getRemark() {
        return remark;
    }

    public String getScreen_name() {
        return screen_name;
    }

    public boolean isVerified() {
        return verified;
    }

    public int getVerifiedType() {
        return verifiedType;
    }

    @Override
    public String toString() {
        return "commentUser{" +
                "id=" + id +
                ", mbtype=" + mbtype +
                ", profile_image_url='" + profile_image_url + '\'' +
                ", profile_url='" + profile_url + '\'' +
                ", remark='" + remark + '\'' +
                ", screen_name='" + screen_name + '\'' +
                ", verified=" + verified +
                ", verifiedType=" + verifiedType +
                '}';
    }
}