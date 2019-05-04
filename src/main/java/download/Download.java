package download;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * html文件下载处理
 * @author xinyifeng
 * @date 2019/05/04
 */

/**
 * Servlet implementation class Download
 */
@WebServlet("/Download")
public class Download extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String fileDir = "F:/file/";        //html文件路径

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Download() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //response.getWriter().append("Served at: ").append(request.getContextPath());
        String filename = request.getParameter("filename");

        response.setContentType("text/html");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);

        //String fullFileName = getServletContext().getRealPath("/file/" + filename);
        String fullFileName = fileDir + filename;
        InputStreamReader input = new InputStreamReader(new FileInputStream(fullFileName), "UTF-8");
        BufferedReader read = new BufferedReader(input);
        ServletOutputStream out = response.getOutputStream();
        PrintWriter output = new PrintWriter(out);

        String s = null;
        while ((s = read.readLine()) != null)
            output.println(s);

        input.close();
        output.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //doGet(request, response);
    }

}