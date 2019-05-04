<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Search</title>
<base target="Center">
</head>
<body>
<br><br>
<form action="/SogouT/Search" method="get" target="Center" accept-charset="UTF-8">
<table border="0" align="center">
  <tr align="center">
    <td>
        <input type="hidden" name="curpage" value="1">
        <input type="text" size="50" name="keyword" align="middle" style="width:500px;height:38px;font-family:'Times New Roman';font-size:20px">
        <input type="submit" name="Submit" value="Search" style="width:100px;height:40px;font-family:'Times New Roman';font-size:20px">
    </td>
  </tr>
</table>
</form>
</body>
</html>