package com.runnirr.aaotdfetcher;

/**
 * Created by Adam on 5/29/13.
 *
 * Static class for javascript calls
 */
public class JavascriptCalls {

    public static String getJsSignInButton(){
        return
            "javascript:" +
                    "var button=document.getElementById('signInButton');" +
                    "if(button!=null){" +
                        "window.location=button.href;" +
                    "}else{" +
                        "document.getElementById('handleBuy').submit();" +
                    "}";
    }

    public static String getJsSignInForm(String username, String password){
        return
            "javascript:" +
                    "document.getElementById('ap_email').value='" + username + "';" +
                    "document.getElementById('ap_password').value='" + password + "';" +
                    "document.getElementById('signInSubmit').click();";

    }

    public static String getJsFetchComplete(){
        return
            "javascript:" +
                    "details=document.getElementById('mas-order-details');" +
                    "if(details!=null){" +
                        "detailsRowElement=details.children[1].children[0].children[0];" +

                        "detailsLinkElement=detailsRowElement.children[0].children[0];" +
                        "detailsImageElement=detailsLinkElement.children[0];" +
                        "detailsResultElement=detailsRowElement.children[1];" +
                        "detailsTitle=detailsResultElement.children[2].children[0].innerHTML;" +
                        "isNewPurchase=(detailsResultElement.innerHTML.indexOf('You now own:') >= 0);" +

                        "if(isNewPurchase){" +
                            "alert('image:' + detailsImageElement.src);" +
                            "alert('title:' + detailsTitle);" +
                            "alert('link:' + detailsLinkElement.href);" +
                        "}" +
                    "}";
    }

}
