package crawler;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.Params;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	public static boolean extractQuestions = false;
	
	public static void getRangeLinks() throws Exception {
		FileUtils.write(new File(Params.linksFile), "");
		String crawlStorageFolder = "data/crawl/";
        int numberOfCrawlers = 1;
        List<String> urls = Arrays.asList(
        		"http://www.algebra.com/algebra/homework/Percentage-and-ratio-word-problems/",
        		"http://www.algebra.com/algebra/homework/Rate-of-work-word-problems/",
        		"http://www.algebra.com/algebra/homework/word/finance/",
        		"http://www.algebra.com/algebra/homework/word/age/",
        		"http://www.algebra.com/algebra/homework/word/travel/",
        		"http://www.algebra.com/algebra/homework/word/numbers/",
        		"http://www.algebra.com/algebra/homework/Problems-with-consecutive-odd-even-integers/",
        		"http://www.algebra.com/algebra/homework/word/mixtures/",
        		"http://www.algebra.com/algebra/homework/coordinate/word/",
        		"http://www.algebra.com/algebra/homework/word/misc/",
        		"http://www.algebra.com/algebra/homework/word/coins/",
        		"http://www.algebra.com/algebra/homework/Human-and-algebraic-language/",
        		"http://www.algebra.com/algebra/homework/word/evaluation/",
        		"http://www.algebra.com/algebra/homework/word/unit_conversion/",
        		"http://www.algebra.com/algebra/homework/word/geometry/");

        CrawlConfig config = new CrawlConfig();
	//        config.setMaxOutgoingLinksToFollow(1000);
        config.setMaxDepthOfCrawling(1);
        config.setCrawlStorageFolder(crawlStorageFolder);
     
        for(String url : urls) {
    		System.out.println("Starting crawler for : "+url);
        	PageFetcher pageFetcher = new PageFetcher(config);
        	RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        	RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        	CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
	        controller.addSeed(url);
	        controller.start(Crawler.class, numberOfCrawlers);
	        System.out.println("Done");
	    }
        System.out.println("Done crawling all links");
	}
	
	public static void getQuestions() throws Exception {
		String crawlStorageFolder = "data/crawl/";
        int numberOfCrawlers = 1;

        CrawlConfig config = new CrawlConfig();
        config.setMaxOutgoingLinksToFollow(100);
        config.setMaxDepthOfCrawling(1);
        config.setCrawlStorageFolder(crawlStorageFolder);

        for(String url : FileUtils.readLines(new File(Params.linksFile))) {
    		System.out.println("Starting crawler for : "+url);
        	PageFetcher pageFetcher = new PageFetcher(config);
        	RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        	RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        	CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
	        controller.addSeed(url);
	        controller.start(Crawler.class, numberOfCrawlers);
	        System.out.println("Done");
        }
	}
	
    public static void main(String[] args) throws Exception {
    		if(args.length < 1) {
    			System.out.println("1 parameter : (extractLinks / extractQuestions)");
    			System.exit(0);
    		}
    		if(args[0].equals("extractLinks")) {
    			extractQuestions = false;
    			getRangeLinks();
    		}
    		if(args[0].equals("extractQuestions")) {
    			extractQuestions = true;
    			getQuestions();
    		}
    }
}