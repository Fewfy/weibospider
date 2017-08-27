package us.codecraft.webmagic.processor.Weibo;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.NowherePipeline;
import us.codecraft.webmagic.proxy.Data5UProxyProvider;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.scheduler.QueueSchedulerWeiboUser;
import us.codecraft.webmagic.scheduler.component.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.component.NothingDuplicateRemovedScheduler;

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
public class WeiBoSpider {

    public static void main(String[] args) {
        Data5UProxyProvider proxyProvider = new Data5UProxyProvider();
        new Thread(proxyProvider).start();
        long seed = 2759239943L;
        int threadCount = 1;
//        System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
//        System.getProperties().setProperty("socksProxyPort", "1080");

        Spider mBlogSpider = new Spider(new WeiBoMblogPageProcessor()).addPipeline(new NowherePipeline());
        mBlogSpider.setExitWhenComplete(false).setDownloader(new HttpClientDownloader("WeiBoMblogSpider")).setHttpClientDownloaderProxy(proxyProvider);
        mBlogSpider.setSpiderName("WeiBoMblogSpider").thread(threadCount).setDuplicateRemover(new NothingDuplicateRemovedScheduler());
        ;//.addPipeline(new FilePipeline("result.txt"))
//        mBlogSpider.addUrl("https://m.weibo.cn/api/container/getIndex?containerid=2304131706649781&page=1");


        Spider commentSpider = new Spider(new WeiBoCommentPageProcessor()).addPipeline(new NowherePipeline());
        commentSpider.setExitWhenComplete(false).setDownloader(new HttpClientDownloader("WeiBoCommentSpider")).setHttpClientDownloaderProxy(proxyProvider);
        commentSpider.setSpiderName("WeiBoCommentSpider").thread(threadCount).setDuplicateRemover(new NothingDuplicateRemovedScheduler());
//        commentSpider.addUrl("https://m.weibo.cn/api/comments/show?id=4133142027981468&page=1");


        Spider userSpider = new Spider(new WeiBoUserPageProcessor()).addPipeline(new NowherePipeline());
        userSpider.setExitWhenComplete(false).setDownloader(new HttpClientDownloader("WeiBoUserSpider")).setHttpClientDownloaderProxy(proxyProvider);
        userSpider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FOLLOWERS&page=1", seed));
        userSpider.addUrl(String.format("https://m.weibo.cn/api/container/getSecond?containerid=100505%d_-_FANS&page=1", seed));
        userSpider.setSpiderName("WeiBoUserSpider").thread(100).setScheduler(new QueueSchedulerWeiboUser()).setDuplicateRemover(new NothingDuplicateRemovedScheduler());//.addPipeline(new FilePipeline("result.txt"))


        mBlogSpider.getPageProcessor().setSpider(commentSpider);
        commentSpider.getPageProcessor().setSpider(userSpider);
        userSpider.getPageProcessor().setSpider(mBlogSpider);


        mBlogSpider.start();
        commentSpider.start();
        userSpider.start();
    }
}
