package com.antverdovsky.wikideg.sep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import org.junit.Test;

import com.antverdovsky.wikideg.linkfetch.JSONLinksFetcher;
import com.antverdovsky.wikideg.util.DataParse;
import com.antverdovsky.wikideg.util.URLFetch;
import com.antverdovsky.wikideg.util.Utilities;

/**
 * Basic Testing of the Separation class.
 */
public class SeparationTest {
	@Test
	/**
	 * Tests the path generated by the Separation method for any two random
	 * Wikipedia articles.
	 */
	public void testPath() {
		///
		/// Get the path using the Separation Class.
		///
		
		// Get any two random articles
		String article1 = "";
		String article2 = "";
		try {
			article1 = DataParse.parseRandomArticle(
					URLFetch.getData(URLFetch.getRandomURL()));
			article2 = DataParse.parseRandomArticle(
					URLFetch.getData(URLFetch.getRandomURL()));
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("Unable to get random articles!");
		}

		System.out.println("Article A: " + article1);
		System.out.println("Article B: " + article2);
		
		// Build a path between the two articles
		Separation separation = null;
		double start = System.currentTimeMillis();
		try {
			 separation = new Separation(article1, article2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Separation Threw Exception!");
		}
		double stop = System.currentTimeMillis();
		
		// Stop test if no path exists
		if (separation == null) System.exit(0);
		
		//
		// Now let's check that we can trace the path ourselves.
		//
		
		// Get the values of path and number of degrees from separation return
		Stack<String> sepPath = separation.getPath();
		Stack<String> sepEmbPath = separation.getEmbeddedPath();
		int sepNumDegrees = separation.getNumDegrees();
		boolean sepValid = separation.getPathExists();
		
		System.out.println("Separation Valid? " + sepValid);
		System.out.println("Separation Path: " + sepPath);
		System.out.println("Separation Embedded Path: " + sepEmbPath);
		System.out.println("Separation Number of Degrees: " + sepNumDegrees);
		System.out.println("Time to Compute (ms): " + (stop - start));
		
		if (!sepValid) {
			System.out.println("Path was not found! Aborted Test.");
			return;
		}
		
		// The current and next article names 
		String current = "";
		String next = "";
		
		int i = 0;
		for (; i < sepPath.size() - 1; ++i) {
			// Get the current and next article.
			current = sepPath.get(i);
			next = sepPath.get(i + 1);
			
			// Fetch all of the links that we can go to from the current 
			// article.
			ArrayList<String> links = new ArrayList<String>();
			try {
				ArrayList<String> targets = new ArrayList<String>(1);
				targets.add(next);
				links = new JSONLinksFetcher().getLinks(current, targets);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Failed to fetch articles!");
			}
			
			// Assert that the export data contains the next article in
			// the path.
			boolean canHop = Utilities.containsIgnoreCase(links, next);
			System.out.print("Path exists from " + current + " to " + next +
					"? ");
			System.out.println(canHop ? "YES" : "NO");
			assertTrue(canHop);
		}
		
		System.out.println("Traced Path with " + i + " degrees.");
		assertEquals(i, sepNumDegrees);
		
		System.out.println("Finished Test.");
	}
}
