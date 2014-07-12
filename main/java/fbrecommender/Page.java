/**
 * 
 */
package fbrecommender;

/**
 * This class represents a wrapper for the page data
 * @author SatNam621
 *
 */
public class Page {

	String pageName;
	String category;
	Long pageId;
	String pageDesc;
	
	Page(Long id, String name, String category,String pageDesc){
		this.pageName = name;
		this.pageId =id;
		this.category = category;
		this.pageDesc = pageDesc;
	}
	protected String getName(){
		return pageName;
	}
	protected Long getID(){
		return pageId;
	}
	protected String getCategory(){
		return category;
	}
	
}
