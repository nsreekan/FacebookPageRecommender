/**
 * 
 */
package fbrecommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This is the main class where it parses the JSOn feed back from Facebook 
 * API and stores into java objects for processing
 * @author SatNam621
 *
 */
public class FBJsonFeedParser {


	LinkedHashMap<Long, String> userIdName = new LinkedHashMap<Long,String>(); 
	LinkedHashMap<Long,String> pageIdName = new LinkedHashMap<Long,String>();
	LinkedHashMap<Long,ArrayList<Long>> userPageIds = new LinkedHashMap<Long,ArrayList<Long>>();
	LinkedHashMap<Long,ArrayList<Long>> pageLikedBy = new LinkedHashMap<Long,ArrayList<Long>>();
	LinkedHashMap<Long,Page> pages = new LinkedHashMap<Long,Page>();
	HashMap<Long,String> pageDescData = new HashMap<Long,String>();
	HashMap<Long,String> userPageLikeDescData = new HashMap<Long,String>();
	ArrayList<String> categories = new ArrayList<String>();
	Long userId;
	public static void main(String args[]){

		FBJsonFeedParser parser = new FBJsonFeedParser();

	}

	protected void parseFiles(String fileName){
		readJSONFilesFromDir(fileName);
		convertToRatingFile(fileName+"\\Rating.csv");
		convertToItemFile(fileName+"\\Item.csv");
		convertToUserInfoFile(fileName+"\\Info.csv");
		convertToCategoryFile(fileName+"\\Info.genre");
		convertToItemLimitedFile(fileName+"\\ItemLimited.csv");
		convertToPagesUserHasLiked(fileName+"\\UserHasLiked.csv");
		convertToPagesAllUserHaveLiked(fileName+"\\AllUsersHaveLiked.csv");
		convertToPageDescData(fileName+"\\PageDescData.info",false);
		convertToPageDescData(fileName+"\\UserPageLikeDescData.info",true);
	}





	protected LinkedHashMap<Long, ArrayList<Long>> getUserPageIds(){
		return userPageIds;
	}
	protected LinkedHashMap<Long,Page> getPages(){
		return pages;
	}
	protected LinkedHashMap<Long,String> getUsers(){
		return userIdName;
	}
	protected Long getUserId(){
		return userId;
	}
	protected List<Long> getUserItems(){
		return userPageIds.get(userId);
	}
	protected LinkedHashMap<Long, ArrayList<Long>> getPageLikedBy(){
		return pageLikedBy;
	}

	private void convertToCategoryFile(String fileName) {
		try{
			File f = new File(fileName);
			StringBuilder writer = new StringBuilder();
			for(String category : categories){

				writer.append(category);
				writer.append("\n");
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void convertToPageDescData(String fileName,boolean mainUser) {//pageDescData
		try{
			File f = new File(fileName);
			StringBuilder writer = new StringBuilder();
			Set<Long> pageIds = null;
			if(mainUser){
				pageIds = userPageLikeDescData.keySet();
			}else {
				pageIds = pageDescData.keySet();
			}
			
			for(Long pageId:pageIds){
				writer.append("<DOCID>");
				writer.append("\n");
				writer.append(pageId);
				writer.append("\n");
				writer.append("<DESC>");
				writer.append("\n");
				writer.append(pageDescData.get(pageId));
				writer.append("\n");
				writer.append("</DOCID>");				
				writer.append("\n");
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);

		}
		catch(Exception e){
			e.printStackTrace();
		}


	}

	private void convertToUserInfoFile(String fileName) {
		try{
			File f = new File(fileName);
			Set<Entry<Long, String>> entries = userIdName.entrySet();
			Iterator<Entry<Long, String>> it = entries.iterator();
			StringBuilder writer = new StringBuilder();
			while(it.hasNext()){
				Entry<Long, String> entry = it.next();
				writer.append(entry.getKey());
				writer.append(",");
				writer.append(entry.getValue());
				writer.append("\n");
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	private void convertToPagesUserHasLiked(String fileName) {
		File f = new File(fileName);
		ArrayList<Long> pages = userPageIds.get(userId);
		StringBuffer writer = new StringBuffer();
		for(Long page: pages){
			writer.append(page);
			writer.append(",");
			writer.append(pageIdName.get(page));

			writer.append("\n");
		}
		try {
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void convertToPagesAllUserHaveLiked(String fileName) {
		File f = new File(fileName);

		Set<Entry<Long, ArrayList<Long>>> entries = userPageIds.entrySet();
		Iterator<Entry<Long, ArrayList<Long>>> it = entries.iterator();
		StringBuffer writer = new StringBuffer();
		while(it.hasNext()){
			Entry<Long, ArrayList<Long>>  entry = it.next();
			Long userId = entry.getKey();
			ArrayList<Long> pages = userPageIds.get(userId);

			for(Long page: pages){
				writer.append(userId);
				writer.append(",");
				writer.append(page);
				writer.append(",");
				writer.append(pageIdName.get(page));

				writer.append("\n");
			}			
		}
		try {
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void convertToItemFile(String fileName) {
		try{
			File f = new File(fileName);
			Set<Entry<Long, Page>> entries = pages.entrySet();
			Iterator<Entry<Long, Page>> it = entries.iterator();
			StringBuffer writer = new StringBuffer();
			while(it.hasNext()){
				Entry<Long, Page> entry = it.next();
				writer.append(entry.getKey());
				writer.append(",");
				writer.append(entry.getValue().getName().replace(",", ""));
				writer.append(",");
				writer.append(buildCategoryBinaryList(entry.getValue()));
				writer.append("\n");
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	private void convertToItemLimitedFile(String fileName) {
		try{
			File f = new File(fileName);
			Set<Entry<Long, String>> entries = pageIdName.entrySet();
			Iterator<Entry<Long, String>> it = entries.iterator();
			StringBuffer writer = new StringBuffer();
			while(it.hasNext()){
				Entry<Long, String> entry = it.next();
				writer.append(entry.getKey());
				writer.append(",");
				writer.append(entry.getValue().replace(",", ""));				
				writer.append("\n");
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_16);

		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	private String buildCategoryBinaryList(Page page) {
		int a[] = new int[categories.size()];
		int index = categories.indexOf(page.getCategory());
		a[index] = 1;
		StringBuilder builder = new StringBuilder();
		for (int i=0;i <a.length;i++){
			builder.append(a[i]);
			if(i != a.length-1)
				builder.append(",");			
		}		
		return builder.toString();
	}

	private void convertToRatingFile(String fileName) {
		try{
			File f = new File(fileName);
			Set<Entry<Long, ArrayList<Long>>> entries = userPageIds.entrySet();
			Iterator<Entry<Long,ArrayList<Long>>> it = entries.iterator();
			StringBuffer writer = new StringBuffer();
			while(it.hasNext()){
				Entry<Long,ArrayList<Long>> entry = it.next();
				for(Long everyPage: entry.getValue())
				{
					writer.append(entry.getKey());
					writer.append(",");
					writer.append(everyPage);
					writer.append(",");
					writer.append("1");
					writer.append("\n");
				}
			}
			FileUtils.write(f, writer.toString(),StandardCharsets.UTF_8);

		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private  void readJSONFilesFromDir(String dirName) {
		File folder = new File(dirName);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(".txt")) {
				try {
					//Charset charset = Charset.forName("UTF-16");
					String content = FileUtils.readFileToString(file,StandardCharsets.UTF_16);
					parseJSONText(content);
				} catch (IOException e) {

					e.printStackTrace();
				}

			} 
		}

	}

	private  void parseJSONText(String content) {
		try{
			JSONObject userData = new JSONObject(content);

			Long user_id = (Long)userData.getLong("id");
			String user_name = userData.getString("name");			
			userId = user_id;
			userIdName.put(user_id, user_name);
			getLikes(userData,user_id, true);
			JSONObject userFriends = userData.getJSONObject("friends");
			JSONArray friendsData = userFriends.getJSONArray("data");
			for(int i=0;i <friendsData.length();i++){
				JSONObject friend = (JSONObject) friendsData.get(i);				
				String friendName = friend.getString("name");
				Long friendId = friend.getLong("id");				
				userIdName.put(friendId, friendName);
				getLikes(friend,friendId, false);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	private void getLikes(JSONObject person, Long id, boolean mainUser){
		if(person.isNull("likes")) return;
		JSONObject friendData = person.getJSONObject("likes");
		JSONArray friendLikes = friendData.getJSONArray("data");
		ArrayList<Long> pagesUserHasLiked = new ArrayList<Long>();

		for (int j=0; j < friendLikes.length();j++){

			JSONObject pageData = (JSONObject) friendLikes.get(j);
			String category = null;
			String pageAbout = null;
			String pageDesc = null;
			String pageName = pageData.getString("name");
			if(pageData.isNull("category") || pageData.getString("category").trim().equals("")) {
				category = "General";
			}else {
				category = pageData.getString("category");
			}
			if(pageData.isNull("about") || pageData.getString("about").trim().equals("")) {
				pageAbout = " ";
			}else {
				pageAbout = pageData.getString("about");
			}	
			if(pageData.isNull("description") || pageData.getString("description").trim().equals("")) {
				pageDesc = " ";
			}else {
				pageDesc = pageData.getString("description");
			}				

			Long pageId = pageData.getLong("id");
			pageIdName.put(pageId,pageName);
			
			if(!categories.contains(category))
			 {
				categories.add(category);
			 }
			if(mainUser){
				userPageLikeDescData.put(pageId, pageAbout+" "+pageDesc);
			} else {
				pageDescData.put(pageId, pageAbout+" "+pageDesc);
			}
			if(!pagesUserHasLiked.contains(pageId))
			{
				pagesUserHasLiked.add(pageId);
			}
			pages.put(pageId,new Page(pageId,pageName,category,pageAbout+" "+pageDesc));
			if(pageLikedBy.get(pageId) == null){
				ArrayList<Long> usersWhoLikeAPage = new ArrayList<Long>();
				usersWhoLikeAPage.add(id);
				pageLikedBy.put(pageId, usersWhoLikeAPage);
			}else {
				ArrayList<Long> usersWhoLikeAPage = pageLikedBy.get(pageId);
				usersWhoLikeAPage.add(id);
			}

		}
		userPageIds.put(id,pagesUserHasLiked);
	}
	private String readFileNameFromArgs(String[] args) {
		String filePath = "";
		try{
			if(args != null){
				filePath = args[0];
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return filePath;
	}

}
