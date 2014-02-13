package com.juliencolle.rssreader.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.juliencolle.model.article.Article;
import com.juliencolle.model.article.ArticleContent;


public class RssHandler extends DefaultHandler {

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 15;

	//Current characters being accumulated
	//Why not StringBuilder???
	StringBuffer chars = new StringBuffer();


	public List<Article> getArticleList() {
		return ArticleContent.ITEMS;
	}
	
	public Map<String, Article> getArticleMap() {
		return ArticleContent.ITEMS_MAP;
	}

	/* 
	 * This method is called everytime a start element is found (an opening XML marker)
	 * here we always reset the characters StringBuffer as we are only currently interested
	 * in the the text values stored at leaf nodes
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		chars = new StringBuffer();
	}

	/* 
	 * This method is called everytime an end element is found (a closing XML marker)
	 * here we check what element is being closed, if it is a relevant leaf node that we are
	 * checking, such as Title, then we get the characters we have accumulated in the StringBuffer
	 * and set the current Article's title to the value
	 * 
	 * If this is closing the "entry", it means it is the end of the article, so we add that to the list
	 * and then reset our Article object for the next one on the stream
	 * 
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (localName.equalsIgnoreCase("guid")){
			currentArticle.setGuid(chars.toString());
		} else if (localName.equalsIgnoreCase("title")){
			currentArticle.setTitle(chars.toString());
		} else if (localName.equalsIgnoreCase("description")){
			currentArticle.setDescription(chars.toString());
		} else if (localName.equalsIgnoreCase("pubDate")){
			
			String strDate = chars.toString();
			DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
			Date pDate = new Date();
			try {
				pDate = formatter.parse(strDate);
			} catch (ParseException e) {
				Log.e("DATE PARSING", "Error parsing date..");
				try {
					pDate = formatter.parse("Sat, 01 Jan 2000 00:00:00 GMT");
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			
			currentArticle.setPubDate(pDate);
		} else if (localName.equalsIgnoreCase("link")){
			try {
				currentArticle.setUrl(new URL(chars.toString()));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else if (localName.equalsIgnoreCase("author")){
			currentArticle.setAuthor(chars.toString());
		} else if (localName.equalsIgnoreCase("content")){
			currentArticle.setEncodedContent(chars.toString());
		} else if (localName.equalsIgnoreCase("item")){
			
		} 


		// Check if looking for article, and if article is complete
		if (localName.equalsIgnoreCase("item")) {
				 
	        MessageDigest md = null;
	        StringBuffer sb = new StringBuffer();
			try {
				md = MessageDigest.getInstance("MD5");
				
				md.update(currentArticle.getTitle().getBytes());
		        byte byteData[] = md.digest();
		        for (int i = 0; i < byteData.length; i++) {
		        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		        }
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			currentArticle.setGuid(sb.toString());
			
			ArticleContent.addItem(currentArticle);

			currentArticle = new Article();

			articlesAdded++;
			
			// Lets check if we've hit our limit on number of articles
//			if (articlesAdded >= ARTICLES_LIMIT){
//				throw new EnoughDataSAXTerminatorException();
//			}
		}
	}
	
	public class EnoughDataSAXTerminatorException extends SAXException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}

	/* 
	 * This method is called when characters are found in between XML markers, however, there is no
	 * guarante that this will be called at the end of the node, or that it will be called only once
	 * , so we just accumulate these and then deal with them in endElement() to be sure we have all the
	 * text
	 * 
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char ch[], int start, int length) {
		chars.append(new String(ch, start, length));
	}
	
}