package client.src;

import javafx.scene.web.WebView;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class MarkdownParser
{

    // Added private WebView field
    private final WebView wv;

    /**
     * Constructor for injecting
     * @param wv the injected webview
     */
    public MarkdownParser(WebView wv)
    {
        this.wv = wv;
    }

    /**
     * Method for parsing markdown
     * @param markdownContent the string of markdown
     * @return string containing parsed markdown as html
     */
    public String parseMarkdown(String markdownContent)
    {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(markdownContent);
        return renderer.render(document);
    }

    /**
     * Method to parse and render markdown content
     * @param markdownContent the string of markdown
     */
    public void renderMarkdownToWebView(String markdownContent)
    {
        String htmlContent = parseMarkdown(markdownContent);
        wv.getEngine().setUserStyleSheetLocation(getClass()
                .getResource("/client/styles/WebView.css").toExternalForm());
        wv.getEngine().loadContent(htmlContent);
    }
}