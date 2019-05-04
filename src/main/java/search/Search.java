package search;

import org.ansj.domain.Term;
import org.ansj.lucene6.AnsjAnalyzer;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 关键词搜索处理
 * @author xinyifeng
 * @date 2019/05/04
 */

/**
 * Servlet implementation class Search
 */
@WebServlet("/Search")
public class Search extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int pageSize = 10;
    private static final String field = "body";
    private static final String indexDir = "H:/Index/indexSogou";       //索引包路径
    private static final Analyzer analyzer = new AnsjAnalyzer(AnsjAnalyzer.TYPE.nlp_ansj);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //response.getWriter().append("Served at: ").append(request.getContextPath());
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        PrintWriter out = response.getWriter();

        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        String keyword = request.getParameter("keyword");
        int currentPage = Integer.parseInt(request.getParameter("curpage"));

        Set<String> set = analyze(keyword);

        Query query = createQuery(set);
        TopDocs hit = searcher.search(query, reader.maxDoc());
        ScoreDoc[] hits = hit.scoreDocs;

        int resultNum = hits.length;
        int begin = pageSize * (currentPage - 1);
        int end = Math.min(resultNum, begin + pageSize);
        int offset = end - begin;

        out.println("<head><style type=text/css>");
        out.println(".h1{text-align:left;font-family:Times New Roman;font-size:20;color:blue;position:relative;}");
        out.println(".p1{text-align:left;font-family:Times New Roman;font-size:15;position:relative;max-height:50px;line-height:25px;overflow:hidden;}");
        out.println(".p2{text-align:left;font-family:Times New Roman;font-size:15;}");
        out.println(".page{text-align:center;margin-top:50px;}");
        out.println(".page a,.page span{cursor:pointer;text-decoration:none;border:1px; solid #f9d52b;padding:5px 7px;color:#767675;}");
        out.println(".page a:hover,.page span:hover{color:red;}");
        out.println("</style></head><body>");
        out.println("<p class=p2><b>Result number: <span style=\"background-color:red;color:white;\">" + resultNum + "</span></b></p><hr>");

        Scorer fragmentScore = new QueryScorer(query);
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("", "");
        Highlighter highlighter = new Highlighter(formatter, fragmentScore);
        Fragmenter fragmenter = new SimpleFragmenter(450);
        highlighter.setTextFragmenter(fragmenter);

        if (offset != 0) {
            out.println("<table style=\"word-break:break-all;word-wrap:break-all;table-layout:fixed\" align=\"left\" width=\"100%\" cellpadding=\"10\">");
            for (int i = begin; i < end; i++) {
                int doc = hits[i].doc;
                String url = reader.document(doc).get("url");
                //String html = reader.document(doc).get("html");
                float score = hits[i].score;
                String body = reader.document(doc).get(field);
                TokenStream tokenStream = analyzer.tokenStream("",new StringReader(body));
                String hlbody = "";
                try {
                    hlbody = highlighter.getBestFragment(tokenStream, body);
                } catch (InvalidTokenOffsetsException e) {
                    e.printStackTrace();
                }
                String localUrl = doc + ".html";
                out.println("<tr align=\"left\"><td>");
                out.println("<div><p class=h1><b>URL: </b>" + "<a target=\"_blank\" href=\"" + url + "\">" + url + "</a></p></div>");
                out.print("<div><p class=p2><b>LocalURL: </b>" + localUrl);
                out.println("&nbsp;<a href=\"./Download?filename=" + localUrl + "\">下载</a></p></div>");
                out.println("<div><p class=p2><b>Score: </b>" + score + "</p></div>");
                //out.println("<div><p class=p2><b>Body: </b>" + reader.document(doc).get(field) + "</p></div>");
                out.println("<div><p class=p1><b>Body: </b>" + hlbody + "</p></div>");
                out.println("</td></tr>");
            }
            out.println("</table>");
        }

        int num = resultNum % pageSize == 0? resultNum / pageSize : resultNum / pageSize + 1;
        if (num > 0) {
            if (num <= 10) {
                out.println("<div class=\"page\">");
                out.println("<a href=\"./Search?curpage=1&keyword=" + keyword + "\"><span>首页</span></a>");
                if (currentPage != 1)
                    out.println("<a href=\"./Search?curpage=" + (currentPage - 1) + "&keyword=" + keyword + "\"><span>上一页</span></a>");
                for (int i = 1; i <= num; i++)
                    out.println("<a href=\"./Search?curpage=" + i + "&keyword=" + keyword + "\"><span>" + i + "</span></a>");
                if (currentPage != num)
                    out.println("<a href=\"./Search?curpage=" + (currentPage + 1) + "&keyword=" + keyword + "\"><span>下一页</span></a>");
                out.println("<a href=\"./Search?curpage=" + num + "&keyword=" + keyword + "\"><span>尾页</span></a>");
                out.println("</div>");
            } else if (currentPage < 10) {
                out.println("<div class=\"page\">");
                out.println("<a href=\"./Search?curpage=1&keyword=" + keyword + "\"><span>首页</span></a>");
                if (currentPage != 1)
                    out.println("<a href=\"./Search?curpage=" + (currentPage - 1) + "&keyword=" + keyword + "\"><span>上一页</span></a>");
                for (int i = 1; i <= 10; i++)
                    out.println("<a href=\"./Search?curpage=" + i + "&keyword=" + keyword + "\"><span>" + i + "</span></a>");
                out.println("<a>&hellip;</a>");
                out.println("<a href=\"./Search?curpage=" + (currentPage + 1) + "&keyword=" + keyword + "\"><span>下一页</span></a>");
                out.println("<a href=\"./Search?curpage=" + num + "&keyword=" + keyword + "\"><span>尾页</span></a>");
                out.println("</div>");
            } else if (currentPage >= 10) {
                out.println("<div class=\"page\">");
                out.println("<a href=\"./Search?curpage=1&keyword=" + keyword + "\"><span>首页</span></a>");
                out.println("<a href=\"./Search?curpage=" + (currentPage - 1) + "&keyword=" + keyword + "\"><span>上一页</span></a>");
                int last = Math.min(num, (currentPage + 10));
                int first = currentPage / 10 * 10;
                for (int i = first; i <= last; i++)
                    out.println("<a href=\"./Search?curpage=" + i + "&keyword=" + keyword + "\"><span>" + i + "</span></a>");
                if (last != num)
                    out.println("<a>&hellip;</a>");
                if (currentPage != num)
                    out.println("<a href=\"./Search?curpage=" + (currentPage + 1) + "&keyword=" + keyword + "\"><span>尾页</span></a>");
                out.println("<a href=\"./Search?curpage=" + num + "&keyword=" + keyword + "\"><span>尾页</span></a>");
                out.println("</div>");
            }
        }

        out.println("</body>");

        reader.close();
        out.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);

    }

    /**
     * 创建查询
     * @param set
     * @return
     * @throws Exception
     */
    public static Query createQuery(Set<String> set) {
        int m = 0;
        String[] querys = new String[set.size()];
        for (String word : set)
            querys[++m] = word;
        String[] fields = new String[set.size()];
        Arrays.fill(fields, field);
        BooleanClause.Occur[] flags = new BooleanClause.Occur[set.size()];
        Arrays.fill(flags, BooleanClause.Occur.SHOULD);

        Query query = null;
        try {
            query = MultiFieldQueryParser.parse(querys, fields, flags, analyzer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * 查询关键词分词
     * @param text
     * @return
     */
    public static Set<String> analyze(String text) {
        Set<String> set = new HashSet<>();
        for (Term t : NlpAnalysis.parse(text).getTerms()) {
            set.add(t.getRealName());
        }

        return set;
    }

}