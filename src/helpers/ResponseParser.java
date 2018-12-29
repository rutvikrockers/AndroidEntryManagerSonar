package helpers;

import com.google.gson.Gson;

import org.json.JSONObject;

import Pojo.ResponseUserLogin;

/**
 * Created by rockers on 16/3/17.
 */

public class ResponseParser {
    public static ResponseUserLogin parseLoginJson(String jsonResp) {
        Gson gson = new Gson();
        ResponseUserLogin loginPojo = new ResponseUserLogin();
        try {
         loginPojo = gson.fromJson(jsonResp, ResponseUserLogin.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return loginPojo;
    }



}
