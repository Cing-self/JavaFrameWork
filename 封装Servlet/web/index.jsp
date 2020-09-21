<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
    <script type="text/javascript">
      //异步请求
    </script>
  </head>
  <body>
    <!-- 发送请求的时候 携带两个信息 类名AtmController 方法名login  -->
    <!--    AtmController.do?method=login    -->
    <!-- 发送请求的时候 遵循HTTP协议 . ? : 多个 -->
    <%--<a href="AtmController.do?method=login&name=zzt&pass=123">模拟ATM系统功能点1(登录)</a><br>--%>
    <%--<a href="AtmController.do?method=query&name=zzt&pass=123">模拟ATM系统功能点2(查询)</a><br>--%>
    <%--<a href="ShoppingController.do?method=kindQuery&name=zzt&pass=123">模拟Shopping系统功能点1(查询种类)</a><br>--%>
    <%--<a href="ShoppingController.do?method=kindInsert&name=zzt&pass=123">模拟Shopping系统功能点2(种类添加)</a><br>--%>

    <%--<hr>--%>
    ${requestScope.result}
    <form action="AtmController.do?method=login" method="post">
      account:<input type="text" name="name" value=""><br>
      password:<input type="password" name="pass" value=""><br>
      <input type="submit" value="login"><br>
    </form>

  </body>
</html>
