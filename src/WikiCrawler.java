// LEAVE THIS FILE IN THE DEFAULT PACKAGE
//  (i.e., DO NOT add 'package cs311.pa1;' or similar)

// DO NOT MODIFY THE EXISTING METHOD SIGNATURES
//  (you may, however, add member fields and additional methods)

// DO NOT INCLUDE LIBRARIES OUTSIDE OF THE JAVA STANDARD LIBRARY
//  (i.e., you may only include libraries of the form java.*)

/**
 * @author Jeff, Kevin, Tyler
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.*;

public class WikiCrawler {
	static final String BASE_URL = "https://en.wikipedia.org";
	String fileWriteName, seed;
	int numPages;
	ArrayList<String> topic;
	Graph graph;
	int sleepCount = 0;

	public WikiCrawler(String seedUrl, int max, ArrayList<String> topics,
			String fileName) {
		seed = seedUrl;
		numPages = max;
		fileWriteName = fileName;
		topic = topics;
		graph = new Graph();
	}

	public void crawl() {
		Queue<Vertex> Q = new LinkedList<Vertex>();
		ArrayList<Vertex> visited = new ArrayList<Vertex>();
		ArrayList<String> urls = new ArrayList<String>();

		Vertex root = new Vertex(seed);
		Q.add(root);
		visited.add(root);

		while (!Q.isEmpty() && graph.vertices.size() <= numPages) {
			Vertex currentPage = Q.poll();
			if(sleepCount % 25 == 0) { try {Thread.sleep(3000);} catch(Exception e) { System.out.println("Sleep function failed.");}}
			urls = urlList(currentPage.data);
			sleepCount++;
			if (urls.size() != 0) {
				graph.vertices.add(currentPage);
				for (String link : urls) {
					Vertex u = new Vertex(link, currentPage);
					if (!visited.contains(u)) {
						visited.add(u);
						Q.add(u);
					}
				}
			}
		}

		for (Vertex v : graph.vertices) {
			if (v.parent != null) {
				graph.edges.add(new Edge(v.parent, v));
			}
		}

		if (!fileWriteName.contains(".txt")) {
			fileWriteName += ".txt";
		}
		try {
			PrintWriter writer = new PrintWriter(fileWriteName, "UTF-8");
			writer.print(numPages);
			for (Edge e : graph.edges) {
				writer.print("\n" + e.from.data + " " + e.to.data);
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("File writing has failed.");
		}
	}


//Version 3 uses regex	
	public ArrayList<String> extractLinks(String doc) 
	{ // TODO
		ArrayList<String> result = new ArrayList<String>();
		String[] l = doc.split("\r");
		for(int i=0;i<l.length;i++)
		{
			String regex = "href=\"/wiki/.*?\"";
			Pattern string = Pattern.compile(regex);
			Matcher m = string.matcher(l[i]);
			while(m.find()) 
			{
				String hrefLink = m.group().substring(m.group().indexOf('"')+1, m.group().length()-1);
						    	
				if(!hrefLink.contains("#") && !hrefLink.contains(":") && !result.contains(hrefLink))
				{
					  result.add(hrefLink);
				}
			}
		}
		return result;
	}
	
	
	private ArrayList<String> urlList(String link) {
		// array of duplicate topics
		ArrayList<String> duplicates = new ArrayList<String>(topic);
		ArrayList<String> validUrls = new ArrayList<String>();
		String line;
		boolean pSection = false;

		try {
			URL url = new URL(BASE_URL + link);
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null)// while the line isnt null
			{
				if (line.contains("<p>") || pSection)// if the line is one we look at
				{
					// check if this line contains an ending p tag so we stop looking through 
					pSection = line.contains("</p>");
					
					// traverse through each topic
					for (int i = 0; i < topic.size(); i++) {
						// if the topic is in the line
						if (line.toLowerCase().contains(topic.get(i).toLowerCase())) {
							// remove it from duplicate array
							duplicates.remove(topic.get(i));
						}
					}
					
					ArrayList<String> extractedLinks = extractLinks(line);
					for(String str : extractedLinks){
						if(!validUrls.contains(link)){
							validUrls.add(str);
						}
					}
				}
			}

			// if nothing in duplicates, that means that we found the topic
			if (duplicates.size() != 0) {
				return new ArrayList<String>();
			} else {
				return validUrls;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return new ArrayList<String>();
		}
	}
}
