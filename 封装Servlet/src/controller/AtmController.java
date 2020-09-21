package controller;

import domain.User;
import mvc.ModelAndView;
import mvc.annotation.ResponseBody;
import mvc.annotation.SessionAttributes;
import service.AtmService;

import java.util.List;

//松耦合
//  继承父类                现在不用继承
//  方法重写                现在方法不用重写了
//  方法必须有两个参数        现在方法参数可以随意    原生类型String int float Float HttpServletRequest(不推荐) 数组不行 接口不行 List/Set不行
//  方法必须有两个异常        现在方法是没有异常的
//  方法没有返回值           现在方法是有返回值  String 表示viewName 视图的路径(转发、重定向redirect:)
//  Servlet对象的生命周期问题-----管理机制
//  单例 延迟加载           底层的Controller对象单例机制 延迟加载的机制还保留

@SessionAttributes("name")
public class AtmController {//需要管理这个Controller的单例机制

    private AtmService service = new AtmService();

    //去做刚才LoginController里面该做的事情
    public ModelAndView login(User user){
        ModelAndView mv = new ModelAndView();
        //1.接收请求参数----->以前通过request来接的 现在有参数列表啦
        //2.找寻业务层做登录逻辑判断
        String result = service.login(user);
        //3.根据result做响应转发
        if("success".equals(result)){
            mv.addObject("name",user.getName());//如果存在session中 先放在mv容器里
            mv.setViewName("welcome.jsp");
        }else{
            mv.addObject("result",result);
            mv.setViewName("index.jsp");
        }
        return mv;


    }

    //去做刚才QueryController里面该做的事情
    @ResponseBody
    public List<User> query(){
        System.out.println("我是query的Controller");

        return null;//这个返回值表示什么含义???  转发/重定向 路径 资源名   ||  仅仅表示一个响应数据
    }

}
