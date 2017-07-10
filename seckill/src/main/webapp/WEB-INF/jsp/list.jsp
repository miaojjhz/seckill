<%@page contentType="text/html; charset=UTF-8" language="java" %>
<%@include file="common/tag.jsp" %>
<!DOCTYPE html>
<html>
   <head>
      <title>秒杀列表</title>
      <!--这是 静态包含 包含的jsp会合并进来，作为一个整一个的jsp输出
      			和动态包含的区别：jsp会作为独立的servlet先去运行，然后再和这个静态的jsp做合并-->
      <%@include file="common/head.jsp" %>
       </head>
   <body>
 	<!--页面显示部分 -->
	<div class="panel panel-default">
		<div class="penel-heading  text-center"><!-- 内容区中 -->
			<h2>秒杀列表</h2>
		</div>
		<div class="panel-body">
			<table class="table table-hover table-striped">
				<thead>
	                <tr>
	                    <th>名称</th>
	                    <th>库存</th>
	                    <th>开始时间</th>
	                    <th>结束时间</th>
	                    <th>创建时间</th>
	                    <th>详情页</th>
	                </tr>
                </thead>
                 <tbody>
                 	<c:forEach items="${list}" var="sk">
                    <tr>
                        <td>${sk.name}</td>
                        <td>${sk.number}</td>
                      <td>
                 	 <fmt:formatDate value="${sk.startTime}" pattern="yyyy-MM-dd HH:mm:ss" />
                 </td>
                  <td>
                     <fmt:formatDate value="${sk.endTime}" pattern="yyyy-MM-dd HH:mm:ss" />
                 </td>
                 <td>
                     <fmt:formatDate value="${sk.createTime}" pattern="yyyy-MM-dd HH:mm:ss" />
                 </td>  
                      
                        <td>
                        	<a class="btn btn-default" href="/seckill/${sk.seckillId}/detail" target="_blank">link</a>
                        </td>
                      </tr>
                     </c:forEach>
                 </tbody>
			</table>
		</div>
	</div>
	
	<!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->
<script src="http://cdn.static.runoob.com/libs/jquery/2.1.1/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="http://cdn.static.runoob.com/libs/bootstrap/3.3.7/js/bootstrap.min.js"></script>
   </body>

</html>