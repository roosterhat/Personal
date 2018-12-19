/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semesterproject2;

/**
 *
 * @author eriko
 */
public class NewsCommand extends Command{
    String APIKey = "65d8099be7c64a1ab1d10d2e5bf90070";
    public NewsCommand() {
        super("News");
        addKeyword("news");
        addKeyword("headlines");
        addKeyword("search");
        addKeyword("sources");
        setDetector(new NewsDetector(keywords));
        setFunction(new NewsFunction(APIKey));
        relavent.add("NewsSource");
        usage = "When getting News or Headline you can specify a news provider (providers are listed under 'sources') otherwise defaults to headlines for US. When searching, querry consits of all words following keyword 'search'";
    }
    
}
