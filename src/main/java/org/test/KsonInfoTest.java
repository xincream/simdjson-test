package org.test;

import java.io.IOException;

import org.simdjson.JsonValue;
import org.simdjson.SimdJsonParser;
import org.simdjson.SimdKsonParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.protojson.runner.JsonRowConverter;

public class KsonInfoTest {

    private static final int LOOP = 1000000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String json =
                "{\"sdk_ver\":\"4.7.1.0\",\"config\":{\"max_retry_count\":1,\"bitmap_type\":\"RGB_565\","
                        + "\"max_decoded_mem_cache_size\":74658610,\"max_encoded_mem_cache_size\":4194304,"
                        + "\"max_disk_cache_size\":52428800},\"options\":{\"url\":\"http://p4.a.yximgs"
                        + ".com/uhead/AB/2022/05/15/06/BMjAyMjA1MTUwNjAwMDRfNDAzMDAxNDFfOTUyOTEyNDEwNV9sdg==.kpg\","
                        + "\"urls\":[],\"view_exists\":true,\"view_width\":413,\"view_height\":538,\"ratio\":1},"
                        + "\"meta\":{\"format\":\"KPG\",\"size\":1808640,\"width\":720,\"height\":1256,"
                        + "\"frame_count\":1},\"stat\":{\"status\":\"success\",\"data_source\":\"network\","
                        + "\"error_message\":\"\",\"first_screen\":208,\"stay_duration\":515,\"error_code\":123},"
                        + "\"cache\":{\"cost\":27,\"decoded_mem_cached_count\":51,"
                        + "\"decoded_mem_cached_size\":73667368,\"encoded_mem_cached_count\":100,"
                        + "\"encoded_mem_cached_size\":4126594,\"disk_cached_count\":503,"
                        + "\"disk_cached_size\":23406114},\"network\":{\"status\":\"success\",\"cost\":66,"
                        + "\"url\":\"http://p4.a.yximgs"
                        + ".com/uhead/AB/2022/05/15/06/BMjAyMjA1MTUwNjAwMDRfNDAzMDAxNDFfOTUyOTEyNDEwNV9sdg==.kpg\","
                        + "\"server_ip\":\"121.17.124.106\",\"host\":\"p4.a.yximgs.com\",\"retry_count\":0,"
                        + "\"http_code\":200,\"kimg_proxy\":false,\"url_origin\":\"imaginary\","
                        + "\"image_origin\":\"kwaishop\",\"error_message\":\"xx\","
                        + "\"requests\":[{\"status\":\"success\",\"error_message\":\"\",\"url\":\"http://p4.a.yximgs"
                        + ".com/uhead/AB/2022/05/15/06/BMjAyMjA1MTUwNjAwMDRfNDAzMDAxNDFfOTUyOTEyNDEwNV9sdg==.kpg\","
                        + "\"http_code\":200,\"server_ip\":\"121.17.124.106\",\"protocol\":\"http/1.1\","
                        + "\"received_bytes\":51889,\"cost\":57,\"dns_cost\":-1,\"connect_cost\":0,"
                        + "\"waiting_response_cost\":31,\"response_cost\":17}]},\"decode\":{\"status\":\"success\","
                        + "\"cost\":64,\"width\":1280,\"height\":720,\"bitmap_type\":\"RGB_565\"},"
                        + "\"bs_info\":{\"biz_ft\":\"video\",\"biz_type\":\"xxxx\","
                        + "\"biz_extra\":{\"sub_solution\":\"KRN\",\"bundle_id\":\"xxxxxxxxxxx\","
                        + "\"up_biz_ft\":\"FT_Feed\",\"krn_session_id\":\"string\",\"js_executor\":\"stringV8_JIT\","
                        + "\"bundle_version_code\":123456,\"component_name\":\"string\",\"scheme\":\"string\"},"
                        + "\"scene\":\"feed_cover\"},\"sys_prof\":{\"in_background\":false,\"mem_usage\":12345},"
                        + "\"extra_message\":{\"controller_id\":\"163\",\"request_id\":\"162\","
                        + "\"photo_id\":\"183825002\",\"caller_class\":\"a.b.c:method(12)\","
                        + "\"custom_message\":\"custom_message\"}}";
        System.out.println(json);
        String[] params =
                {"sdk_ver", "extra_message.controller_id", "extra_message.request_id", "extra_message.caller_class",
                        "extra_message.photo_id", "extra_message.custom_message",
                        "config.max_retry_count", "config.bitmap_type", "config.max_decoded_mem_cache_size",
                        "config.max_encoded_mem_cache_size", "config.max_disk_cache_size",
                        "options.ratio", "options.url", "options.urls",
                        "options.view_width", "options.view_height", "options.view_exists",
                        "meta.format", "meta.size", "meta.width", "meta.height", "meta.frame_count",
                        "stat.status", "stat.data_source", "stat.error_message", "stat.first_screen",
                        "stat.stay_duration", "stat.error_code",
                        "cache.cost", "cache.decoded_mem_cached_count", "cache.decoded_mem_cached_size",
                        "cache.encoded_mem_cached_count", "cache.encoded_mem_cached_size", "cache.disk_cached_count",
                        "cache.disk_cached_size",
                        "network.status", "network.cost", "network.url", "network.server_ip", "network.host",
                        "network.retry_count", "network.http_code", "network.error_message",
                        "network.kimg_proxy", "network.image_origin", "network.url_origin",
                        "decode.status", "decode.cost", "decode.width", "decode.height", "decode.bitmap_type",
                        "bs_info.biz_ft", "bs_info.biz_extra", "bs_info.scene", "bs_info.biz_type",
                        "sys_prof.in_background", "sys_prof.mem_usage"};
        testJackson(json);
        testProtoJson(json, params);
        testSimdJson(json);
        testSimdKson(json, params);
    }

    private static void testProtoJson(String json, String[] params) throws IOException {
        long start = System.currentTimeMillis();
        JsonRowConverter converter = new JsonRowConverter(params);
        for (int i = 0; i < LOOP; i++) {
            converter.process(json);
        }
        long end = System.currentTimeMillis();
        System.out.println("json length: " + json.length() + ", ProtoJson cost:" + (end - start));
    }

    private static void testSimdKson(String json, String[] params) {
        long start = System.currentTimeMillis();
        SimdKsonParser parser = new SimdKsonParser(params);
        for (int i = 0; i < LOOP; i++) {
            parser.parse(json.getBytes(), json.length());
        }
        long end = System.currentTimeMillis();
        System.out.println("json length: " + json.length() + ", SimdKson cost:" + (end - start));
    }

    private static void testSimdJson(String json) {
        long start = System.currentTimeMillis();
        SimdJsonParser parser = new SimdJsonParser();
        for (int i = 0; i < LOOP; i++) {
            JsonValue value = parser.parse(json.getBytes(), json.length());
            value.get("sdk_ver").toString();
            value.get("extra_message").get("controller_id").toString();
            value.get("extra_message").get("request_id").toString();
            value.get("extra_message").get("caller_class").toString();
            value.get("extra_message").get("photo_id").toString();
            value.get("extra_message").get("custom_message").toString();
            value.get("config").get("max_retry_count").toString();
            value.get("config").get("bitmap_type").toString();
            value.get("config").get("max_decoded_mem_cache_size").toString();
            value.get("config").get("max_encoded_mem_cache_size").toString();
            value.get("config").get("max_disk_cache_size").toString();
            value.get("options").get("ratio").toString();
            value.get("options").get("url").toString();
            value.get("options").get("urls").toString();
            value.get("options").get("view_width").toString();
            value.get("options").get("view_height").toString();
            value.get("options").get("view_exists").toString();
            value.get("meta").get("format").toString();
            value.get("meta").get("size").toString();
            value.get("meta").get("width").toString();
            value.get("meta").get("height").toString();
            value.get("meta").get("frame_count").toString();
            value.get("stat").get("status").toString();
            value.get("stat").get("data_source").toString();
            value.get("stat").get("error_message").toString();
            value.get("stat").get("first_screen").toString();
            value.get("stat").get("stay_duration").toString();
            value.get("stat").get("error_code").toString();
            value.get("cache").get("cost").toString();
            value.get("cache").get("decoded_mem_cached_count").toString();
            value.get("cache").get("decoded_mem_cached_size").toString();
            value.get("cache").get("encoded_mem_cached_count").toString();
            value.get("cache").get("encoded_mem_cached_size").toString();
            value.get("cache").get("disk_cached_count").toString();
            value.get("cache").get("disk_cached_size").toString();
            value.get("network").get("status").toString();
            value.get("network").get("cost").toString();
            value.get("network").get("url").toString();
            value.get("network").get("server_ip").toString();
            value.get("network").get("host").toString();
            value.get("network").get("retry_count").toString();
            value.get("network").get("http_code").toString();
            value.get("network").get("error_message").toString();
            value.get("network").get("kimg_proxy").toString();
            value.get("network").get("image_origin").toString();
            value.get("network").get("url_origin").toString();
            value.get("decode").get("status").toString();
            value.get("decode").get("cost").toString();
            value.get("decode").get("width").toString();
            value.get("decode").get("height").toString();
            value.get("decode").get("bitmap_type").toString();
            value.get("bs_info").get("biz_ft").toString();
            value.get("bs_info").get("biz_extra").toString();
            value.get("bs_info").get("scene").toString();
            value.get("bs_info").get("biz_type").toString();
            value.get("sys_prof").get("in_background").toString();
            value.get("sys_prof").get("mem_usage").toString();
        }
        long end = System.currentTimeMillis();
        System.out.println("json length: " + json.length() + ", SimdJson cost:" + (end - start));

    }

    private static void testJackson(String json) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < LOOP; i++) {
            JsonNode node = MAPPER.readTree(json);
            node.path("sdk_ver").asText();
            node.path("extra_message").path("controller_id").asText();
            node.path("extra_message").path("request_id").asText();
            node.path("extra_message").path("caller_class").asText();
            node.path("extra_message").path("photo_id").asText();
            node.path("extra_message").path("custom_message").asText();
            node.path("config").path("max_retry_count").asText();
            node.path("config").path("bitmap_type").asText();
            node.path("config").path("max_decoded_mem_cache_size").asText();
            node.path("config").path("max_encoded_mem_cache_size").asText();
            node.path("config").path("max_disk_cache_size").asText();
            node.path("options").path("ratio").asText();
            node.path("options").path("url").asText();
            node.path("options").path("urls").asText();
            node.path("options").path("view_width").asText();
            node.path("options").path("view_height").asText();
            node.path("options").path("view_exists").asText();
            node.path("meta").path("format").asText();
            node.path("meta").path("size").asText();
            node.path("meta").path("width").asText();
            node.path("meta").path("height").asText();
            node.path("meta").path("frame_count").asText();
            node.path("stat").path("status").asText();
            node.path("stat").path("data_source").asText();
            node.path("stat").path("error_message").asText();
            node.path("stat").path("first_screen").asText();
            node.path("stat").path("stay_duration").asText();
            node.path("stat").path("error_code").asText();
            node.path("cache").path("cost").asText();
            node.path("cache").path("decoded_mem_cached_count").asText();
            node.path("cache").path("decoded_mem_cached_size").asText();
            node.path("cache").path("encoded_mem_cached_count").asText();
            node.path("cache").path("encoded_mem_cached_size").asText();
            node.path("cache").path("disk_cached_count").asText();
            node.path("cache").path("disk_cached_size").asText();
            node.path("network").path("status").asText();
            node.path("network").path("cost").asText();
            node.path("network").path("url").asText();
            node.path("network").path("server_ip").asText();
            node.path("network").path("host").asText();
            node.path("network").path("retry_count").asText();
            node.path("network").path("http_code").asText();
            node.path("network").path("error_message").asText();
            node.path("network").path("kimg_proxy").asText();
            node.path("network").path("image_origin").asText();
            node.path("network").path("url_origin").asText();
            node.path("decode").path("status").asText();
            node.path("decode").path("cost").asText();
            node.path("decode").path("width").asText();
            node.path("decode").path("height").asText();
            node.path("decode").path("bitmap_type").asText();
            node.path("bs_info").path("biz_ft").asText();
            node.path("bs_info").path("biz_extra").asText();
            node.path("bs_info").path("scene").asText();
            node.path("bs_info").path("biz_type").asText();
            node.path("sys_prof").path("in_background").asText();
            node.path("sys_prof").path("mem_usage").asText();
        }
        long end = System.currentTimeMillis();
        System.out.println("json length: " + json.length() + ", jackson cost:" + (end - start));
    }
}
