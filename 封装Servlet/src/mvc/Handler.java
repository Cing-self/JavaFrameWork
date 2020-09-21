package mvc;

import com.alibaba.fastjson.JSONObject;
import mvc.annotation.ParameterTypeException;
import mvc.annotation.RequestParam;
import mvc.annotation.ResponseBody;
import mvc.annotation.SessionAttributes;
import mvc.exception.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;

/**
 * 这是一个新的类
 * 这个类没有其他特殊含义
 * 只是将那些小弟方法单独拆分出来
 * 让原来那个DispatcherServlet类更整洁一点
 */
public class Handler {

    //属性---用来存储  请求名==真实类全名的对应关系   读取文件的缓存机制
    private Map<String,String> realClassNameMap = new HashMap();// 类名+类全名
    //属性---用来存储Controller类的对象  因为当前对象是单例的  这个集合只要不new新的肯定单例
    private Map<String,Object> objectMap = new HashMap();// 类名+对象  对象延迟机制
    //属性---用来存储某一个Controller对象和他里面的全部方法
    private Map<Object,Map<String,Method>> objectMethodMap = new HashMap();


    //1、读取配置文件的方法
    void loadPropertiesFile(){
        try {
            //2.读取总管专属的配置文件----缓存Map
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ApplicationContext.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            while(en.hasMoreElements()){
                String key = (String)en.nextElement();
                String value = properties.getProperty(key);
                realClassNameMap.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //2、负责解析请求名
    String parseURI(String uri){
        return uri.substring(uri.lastIndexOf("/")+1,uri.indexOf("."));
    }
    //3、负责通过类名 找到obj对象
    Object findObject(String requestContent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //找对象
        Object obj = objectMap.get(requestContent);
        //如果obj没有 证明从来没有创建使用过
        if(obj==null){
            String fullClassName = realClassNameMap.get(requestContent);
            if(fullClassName==null){
                //请求就有问题了 类都不存在
                //自定义异常
                throw new ControllerNotFoundException(requestContent+"不存在");
            }
            Class clazz = Class.forName(fullClassName);
            obj = clazz.newInstance();
            objectMap.put(requestContent,obj);
            //-------------------------->>>>>>对象懒加载之后 马上解析对象中的全部方法
            //  Map<AtmController,Map<methodName,method>>
            //获取当前类中全部的方法
            Method[] methods = clazz.getDeclaredMethods();
            Map<String,Method> methodMap = new HashMap<>();//用来存储这个对象中的所有方法
            for(Method method : methods){
                //讲一个方法名字和方法对象存入methodMap集合里
                methodMap.put(method.getName(),method);
            }
            objectMethodMap.put(obj,methodMap);
        }
        return obj;
    }
    //4、负责通过obj对象 找到某个方法
    Method findMethod(Object obj,String methodName){
        Map<String,Method> methodMap = objectMethodMap.get(obj);
        return methodMap.get(methodName);//重载情况没有做考虑
    }
    //5、负责分析找到的那个method 做参数的自动注入
    //  参数----->>>>>method request response
    //  返回值--->>>>>  方法执行的时候需要的具体参数值
    Object[] injectionParameters(Method method,HttpServletRequest request,HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //解析method  拿到方法参数列表中的那些key
        //      参数类型
        //      基础类型String int float double
        //      对象类型domain
        //      集合类型map
        //1.获取method方法中所有参数对象
        Parameter[] parameters = method.getParameters();
        //2.严谨的判断
        if(parameters==null || parameters.length==0){
            return null;
        }
        //3.做一个返回值准备好
        Object[] finalParamValue = new Object[parameters.length];
        //4.解析每一个参数---获取key---request取值了---存入返回值数组中
        for(int i=0;i<parameters.length;i++){
            //每一次获取一个参数对象
            Parameter parameter = parameters[i];
            //先获取当前参数前面的注解
            RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
            //判断是否含有注解
            if(paramAnnotation!=null){//有注解 散装的值 String int float
                //获取注解中的value值
                String key = paramAnnotation.value();
                String value = request.getParameter(key);
                if(value!=null){
                    //获取当前参数的数据类型
                    Class paramClazz = parameter.getType();
                    //分析参数类型做分支判断
                    if(paramClazz==String.class){
                        finalParamValue[i] = value;
                    }else if(paramClazz==Integer.class || paramClazz==int.class){
                        finalParamValue[i] = new Integer(value);
                    }else if(paramClazz==Float.class || paramClazz==float.class){
                        finalParamValue[i] = new Float(value);
                    }else if(paramClazz==Double.class || paramClazz==double.class){
                        finalParamValue[i] = new Double(value);
                    }//多个else 自己写完
                }
            }else{//原生request response，对象，map
                //获取参数类型
                Class paramClazz = parameter.getType();
                if(paramClazz.isArray()){
                    //数组 对不起 不处理 异常
                    throw new ParameterTypeException("方法内数组参数无法处理");
                }else{
                    if(paramClazz==HttpServletRequest.class){
                        finalParamValue[i] = request;continue;
                    }
                    if(paramClazz==HttpServletResponse.class){
                        finalParamValue[i] = response;continue;
                    }
                    if(paramClazz==Map.class || paramClazz== List.class){
                        //传递的是接口 处理不了 异常
                        throw new ParameterTypeException("方法内集合不能传递接口 请提供具体类型");
                    }
                    //普通具体对象
                    Object paramObj = paramClazz.newInstance();
                    if(paramObj instanceof Map){
                        //造型成map  存值
                        Map<String,Object> paramMap = (Map<String,Object>)paramObj;
                        //获取全部请求 用请求的key来作为最终map的key
                        Enumeration en = request.getParameterNames();
                        while(en.hasMoreElements()){
                            String key = (String)en.nextElement();
                            String value = request.getParameter(key);
                            paramMap.put(key,value);
                        }
                        finalParamValue[i] = paramMap;
                    }else if(paramObj instanceof Object){
                        //解析对象中的全部属性  属性名key
                        Field[] fields = paramClazz.getDeclaredFields();
                        for(Field field : fields){
                            field.setAccessible(true);//操作私有熟悉
                            String key = field.getName();
                            String value = request.getParameter(key);
                            //将这个value存入属性对象里
                            //对象中属性类型的构造方法  比如Integer  Float
                            Class fieldType = field.getType();
                            Constructor fieldContructor = fieldType.getConstructor(String.class);
                            field.set(paramObj,fieldContructor.newInstance(value));
                            //对象中处理不了Character类型   对象中处理不了对象属性(递归)
                        }
                        finalParamValue[i] = paramObj;
                    }else{
                        throw new ParameterTypeException("未知类型 我处理不了啦 太累啦");
                    }
                }
            }
        }
        return finalParamValue;
    }
    //6、负责处理方法返回值
    //  参数 刚才真正Controller方法执行完毕的返回值字符串----> 视图的名字
    //  转发 重定向  request response
    //  返回值 void
    private void parseResponseContent(String viewName,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        if(!"".equals(viewName) && !"null".equals(viewName)){
            //  redirect:xxx.jsp
            String[] value = viewName.split(":");
            if(value.length == 1){//应该是一个正常的转发
                request.getRequestDispatcher(viewName).forward(request,response);
            }else{//认为是一个重定向
                if("redirect".equals(value[0])){
                    response.sendRedirect(value[1]);
                }
            }
        }else{
            System.out.println("不好好玩儿 我也不处理");
            //自定义异常   ViewNameFormatException
            throw new ViewNameFormatException("Controller响应的ViewName不能为空");
        }
    }
    private void parseModelAndView(Object obj,ModelAndView mv,HttpServletRequest request){
        //从mv对象中把那个map集合获取出来
        HashMap<String,Object> mvMap = mv.getAttributeMap();
        //遍历集合中的元素  拿出来 存入request作用于中
        Set<String> keys = mvMap.keySet();
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            String key = it.next();
            Object value = mvMap.get(key);
            //存入request作用于中
            request.setAttribute(key,value);
        }
        //再分析一下注解看是否需要存入session
        SessionAttributes sattr = obj.getClass().getAnnotation(SessionAttributes.class);
        if(sattr!=null){
            String[] attributeNames = sattr.value();
            if(attributeNames.length!=0){
                HttpSession session = request.getSession();
                for(String attributeName : attributeNames){
                    session.setAttribute(attributeName,mvMap.get(attributeName));
                }
            }
        }

    }
    //7、负责处理方法返回值 不一定是String 也有可能是ModelAndView
    //  参数 方法执行的返回值先给我 Object类型   request  response
    //  返回值没有的
    void finalResolver(Object obj,Method method,Object methodResult,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {//解析器
        if(methodResult==null){
            return ;//证明这个方法不需要框架帮我们做响应处理  那就算啦
        }
        if(methodResult instanceof ModelAndView){
            //由于传递过来的方法返回值是Object类型的
            //强制类型还原
            ModelAndView mv = (ModelAndView)methodResult;
            //找那个弟中弟解析mv对象
            this.parseModelAndView(obj,mv,request);
            //再找另一个弟中弟解析mv中的那个viewName字符串
            this.parseResponseContent(mv.getViewName(),request,response);
        }else if(methodResult instanceof String){
            //返回字符串 可能表示是一个viewName  也可能表示的是一个数据
            //可以获取方法上面的注解说明
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if(responseBody!=null){//有注解 证明返回值是一个数据
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write((String)methodResult);
            }else{//没有注解 证明返回值是一个路径
                this.parseResponseContent((String)methodResult,request,response);
            }
        }else{
            //返回值可能是一些对象  domain  List<domain>
            //AJAX+JSON
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if(responseBody!=null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("jsonObject",methodResult);
                response.getWriter().write(jsonObject.toJSONString());
            }else{
                //抛出一个自定义异常
                //返回值不认识 需要添加注解
            }
        }
    }

}
