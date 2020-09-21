package controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * 这个控制层是专门负责管理shopping种类相关的
 */
public class ShoppingController {

    public String kindQuery(HashMap<String,Object> map){
        System.out.println("shoppingController的kindQuery方法");
        System.out.println(map);
        return "";
    }

    public String kindInsert(HttpServletRequest request, HttpServletResponse response){
        System.out.println("shoppingController的kindInsert方法");
        System.out.println(request.getParameter("name"));
        System.out.println(request.getParameter("pass"));
        return "xxx";
    }

}
