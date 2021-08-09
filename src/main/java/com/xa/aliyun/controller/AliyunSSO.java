package com.xa.aliyun.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Author: hejian
 * @DateTime: 2021/8/6 10:30
 * @Description:
 */
@RestController
public class AliyunSSO {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/sso")
    public AssumeRoleResponse sso() throws ClientException {
        //构建一个阿里云客户端，用于发起请求。
        //构建阿里云客户端时需要设置AccessKey ID和AccessKey Secret。
        IAcsClient client = getClient();

        //构造请求，设置参数。关于参数含义和设置方法，请参见API参考。
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setRoleArn("acs:ram::1212:role/test");
        request.setRoleSessionName("alice");

        //发起请求，并得到响应。
        return client.getAcsResponse(request);
    }

    private IAcsClient getClient() {
        DefaultProfile profile
                = DefaultProfile.getProfile("cn-shenzhen", "aqwq", "qwqwqwqwqwqwq");
        return new DefaultAcsClient(profile);
    }

    @GetMapping("/token")
    public Map<String, String> token() throws ClientException {
        AssumeRoleResponse response = sso();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>(4);
        map.add("AccessKeyId", response.getCredentials().getAccessKeyId());
        map.add("AccessKeySecret", response.getCredentials().getAccessKeySecret());
        map.add("SecurityToken", response.getCredentials().getSecurityToken());
        map.add("TicketType", "mini");
        String url = "https://signin.aliyun.com/federation?" +
                "Action=GetSigninToken";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        return restTemplate.postForObject(url, request, Map.class);
    }

    @GetMapping("/login")
    public String login(@RequestParam String LoginUrl) throws ClientException {
        Map<String, String> token = token();
        String url = "https://signin.aliyun.com/federation?Action=Login";
        url += "&LoginUrl=" + LoginUrl;
        url += "&Destination=https://homenew.console.aliyun.com/?spm=5176.19720258.J_8058803260.34.304f2c4amQWypB";
        url += "&SigninToken=" + token.get("SigninToken");
        return url;
    }
}