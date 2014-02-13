package com.juliencolle.model.article;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces
 */
public class ArticleContent {

	/**
	 * An array of sample (Article) items.
	 */
	public static List<Article> ITEMS = new ArrayList<Article>();

	/**
	 * A map of sample (Article) items, by ID.
	 */
	public static Map<String, Article> ITEMS_MAP = new HashMap<String, Article>();
	
	static {
		ITEMS.add(new Article());
	}

	public static void addItem(Article article) {
		ITEMS.add(article);
		ITEMS_MAP.put(article.getGuid(), article);
	}
	
}
