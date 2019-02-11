package com.epochconsulting.motoinventory.vehicletracker.util;

import android.net.Uri;


import java.util.Map;

/**
 * Created by pragnya on 21/6/17.
 */

public class Utility {
    public static Utility utility;
    public static Utility getInstance(){
       if(utility==null){
           utility = new Utility();
       }
       return utility;

    }
    public  String buildParams(String firstparam,String secondParam){

        return firstparam+Constants.SINGLE_SPACE+secondParam;
    }
    public String buildUrl( String methodORresource,  Map<String,String> queryparameters, String... dottedpath){
        //building my URi,
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(Url.URL_SCHEME);

        builder.encodedAuthority(Url.getServerAddress());
        builder.appendPath(Url.API);
        builder.appendPath(methodORresource);
        for(String path:dottedpath)
            builder.appendPath(path);

        //add the parameters to the urlstring
        if(queryparameters!=null)

        {
            for (Map.Entry<String, String> entry : queryparameters.entrySet()){
                builder.appendQueryParameter(entry.getKey(),entry.getValue());
            }

        }


      return  builder.build().toString();
    }

}
