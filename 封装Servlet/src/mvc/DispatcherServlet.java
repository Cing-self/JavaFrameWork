package mvc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 这个类就是原来的Servlet
 * 是遵循原来Servlet对象生命周期机制  init  service  destroy
 */
public class DispatcherServlet extends HttpServlet {

    private Handler handler = new Handler();

    //init方法：执行顺序先于service
    public void init(ServletConfig config){
        //加载配置文件
        handler.loadPropertiesFile();
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //0.通过request获取请求的类名  uri      request.getRequestURI();
            String uri = request.getRequestURI();
            //  通过request获取请求方法名  method       request.getParameter();
            String methodName = request.getParameter("method");
            //通过handler去做事
            //1.解析urlName得到一个请求名
            String requestContent = handler.parseURI(uri);
            //2.找到obj对象
            Object obj = handler.findObject(requestContent);
            //3.通过obj对象找到对象里面的方法
            Method method = handler.findMethod(obj,methodName);
            //4.处理方法上面的参数
            Object[] finalParamValue = handler.injectionParameters(method,request,response);
            //5.执行方法
            Object methodResult = method.invoke(obj,finalParamValue);
            //6.处理方法执行完毕后的返回结果(响应 转发路径 重定向路径 返回对象JSON)
            handler.finalResolver(obj,method,methodResult,request,response);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
