package net.translives.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import net.translives.app.media.ImageGalleryActivity;
import net.translives.app.news.NewsDetailActivity;
import net.translives.app.question.QuestionDetailActivity;
import net.translives.app.util.AppHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析URL链接, 如果是站内链接, 用相应的activity打开, 否则用浏览器打开
 * Created by thanatos on 16/9/27.
 */

public class URLUtils {

    public static final Pattern PATTERN_URL = Pattern.compile(
            "(?:http|https)://([^/]+)(.+)"
    );

    public static final Pattern PATTERN_PATH_NEWS = Pattern.compile(
            "/news/([0-9]+).*"
    );

    public static final Pattern PATTERN_PATH_QUESTION = Pattern.compile(
            "/question/(\\w+)"
    );

    public static final Pattern PATTERN_IMAGE = Pattern.compile(
            ".*?(gif|jpeg|png|jpg|bmp)"
    );

    private static final String PREFIX_IMAGE = "ima-api:action=showImage&data=";


    /**
     * 解析跳转链接, 使用对应应用打开
     *
     * @param context Context
     * @param uri     give me a uri
     */
    public static void parseUrl(Context context, String uri) {
        if (TextUtils.isEmpty(uri)) return;

        String url = uri;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // do nothing
        }

        Matcher matcher;

        // image url ?
        matcher = PATTERN_IMAGE.matcher(url);
        if (matcher.matches()) {
            ImageGalleryActivity.show(context, url);
            return;
        }

        matcher = PATTERN_URL.matcher(url);
        if (!matcher.find()) {
            // other ?
            parseNonstandardUrl(context, uri);
            return;
        }

        // own ?
        String host = matcher.group(1);
        String path = matcher.group(2);

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) return;

        long oid = 0;
        switch (host) {
            case "www.translives.net":
                matcher = PATTERN_PATH_NEWS.matcher(path);
                if (matcher.find()) {
                    oid = StringUtils.toLong(matcher.group(1));
                    NewsDetailActivity.show(context, oid);
                    break;
                }

                matcher = PATTERN_PATH_QUESTION.matcher(path);
                if (matcher.find()) {
                    oid = StringUtils.toLong(matcher.group(1).split("_")[1]);
                    QuestionDetailActivity.show(context, oid);
                    break;
                }

                AppHelper.openExternalBrowser(context, url);
                break;
            default:
                // pass
                AppHelper.openExternalBrowser(context, url);
        }

    }

    public static void parseNonstandardUrl(Context context, String url) {

        if (url.startsWith("mailto:")) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            context.startActivity(Intent.createChooser(intent, "选择发送应用"));
            return;
        }

        // image, 我不懂老代码的思路...所以直接copy过来
        if (url.startsWith(PREFIX_IMAGE)) {
            String jos = url.substring(PREFIX_IMAGE.length());
            try {
                JSONObject json = new JSONObject(jos);
                String[] urls = json.getString("urls").split(",");
                ImageGalleryActivity.show(context, urls[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
